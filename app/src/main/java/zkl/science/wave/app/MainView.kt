package zkl.science.wave.app

import javafx.application.Application
import javafx.geometry.Pos
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.stage.Screen
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.javafx.JavaFx
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import tornadofx.*
import zkl.science.wave.conf.export.autoExport
import zkl.science.wave.exporter.FileExporter
import zkl.science.wave.exporter.SnapshotTaker
import zkl.science.wave.world.processUntil
import kotlin.math.min

fun main(args: Array<String>) {
	Application.launch(WaveApp::class.java, *args)
}

class WaveApp : App(MainView::class)

class MainView : View("Wave"), CoroutineScope by CoroutineScope(Job()) {

	//conf
	private val conf = CONF
	private val physicsConf = conf.physicsConf
	private val visualConf = conf.visualConf!!
	private val exportConf = conf.exportConf

	//views
	override val root: Pane = vbox {
		hbox {
			paddingAll = 10.0
			alignment = Pos.CENTER_LEFT

			button("start") {
				hboxConstraints {
					marginLeftRight(10.0)
				}
			}.let {
				this@MainView.startButton = it
			}
			button("interact") {
				hboxConstraints { marginLeftRight(10.0) }
			}.let {
				this@MainView.interactButton = it
			}
			button("export") {
				hboxConstraints { marginLeftRight(10.0) }
			}.let {
				this@MainView.exportButton = it
			}
			label {
				text = "nothing to show"
				hgrow = Priority.ALWAYS
				hboxConstraints {
					marginLeftRight(10.0)
				}
			}.let {
				this@MainView.mainLabel = it
			}
		}
		pane {
			val screenBounds = Screen.getPrimary().visualBounds
			prefWidth = screenBounds.width * 0.7
			prefHeight = screenBounds.height * 0.7
			canvas = canvas { }
		}.let {
			this@MainView.canvasPane = it
		}
	}
	private lateinit var startButton: Button
	private lateinit var interactButton: Button
	private lateinit var exportButton: Button
	private lateinit var mainLabel: Label
	private lateinit var canvasPane: Pane
	private lateinit var canvas: Canvas


	//life


	override fun onDock() {
		super.onDock()

		var mainJob: Job? = null
		startButton.run {
			isDisable = false
			setOnMouseClicked {
				mainJob = launch { doMain() }
			}
		}
		interactButton.run {
			isDisable = true
		}
		exportButton.run {
			if (exportConf.autoExport) {
				isDisable = true
				text = "(auto export)"
			} else {
				isDisable = false
				setOnMouseClicked { isExporting = !isExporting }
			}
		}

		currentStage!!.setOnCloseRequest { e ->
			if (mainJob?.isCompleted == false) {
				launch {
					mainJob?.cancelAndJoin()
					launch(Dispatchers.JavaFx) { primaryStage.close() }
				}
				e.consume()
			}
		}

	}

	override fun onDelete() {
		super.onDelete()
		cancel()
	}

	private suspend fun doMain() = coroutineScope {
		launch { doInit() }.join()
		val needOffset = physicsConf.timeOffset > 0f
		if (needOffset) launch { doOffset() }.join()
		val start = !needOffset || exportConf.autoExport
		launch { doProcess(start) }.join()
	}

	private suspend fun doInit() = coroutineScope {
		if (!isActive) return@coroutineScope
		launch(Dispatchers.JavaFx) { mainLabel.text = "world initializing" }
		this@MainView.world

		if (!isActive) return@coroutineScope
		launch(Dispatchers.JavaFx) { mainLabel.text = "painter initializing" }
		this@MainView.painter
		launch(Dispatchers.JavaFx) {
			canvas.width = visualConf.canvasWidth
			canvas.height = visualConf.canvasHeight
			canvas.layoutX = (canvasPane.width - canvas.width) / 2.0
			canvas.layoutY = (canvasPane.height - canvas.height) / 2.0
			val scale = min(canvasPane.width / canvas.width, canvasPane.height / canvas.height)
			canvas.scaleX = scale
			canvas.scaleY = scale
			canvasPane.background = Background(BackgroundFill(painter.backgroundColor, null, null))
			painter.paint(canvas.graphicsContext2D)
		}.join()

	}

	private suspend fun doOffset() = coroutineScope {

		launch(Dispatchers.JavaFx) { startButton.isDisable = true }

		val tickJob = launch {
			while (isActive) {
				delay(1000)
				val message = worldTimeMessage("offsetting")
				launch(Dispatchers.JavaFx) { mainLabel.text = message }
			}
		}
		physicsConf.interact(world)
		world.processUntil(physicsConf.timeUnit, physicsConf.timeOffset)
		launch(Dispatchers.JavaFx) { painter.paint(canvas.graphicsContext2D) }.join()

		tickJob.cancelAndJoin()
	}

	private suspend fun doProcess(start: Boolean) = coroutineScope {
		launch(Dispatchers.JavaFx) {
			interactButton.isDisable = false
			interactButton.setOnMouseClicked { interactWorld() }
		}
		if (exportConf.autoExport) interactWorld()

		val startSignal = Channel<Unit>(1)
		if (start) startSignal.trySend(Unit)
		while (isActive) {
			if (startSignal.tryReceive().isFailure) {
				launch(Dispatchers.JavaFx) {
					startButton.run {
						text = "resume"
						isDisable = false
						setOnMouseClicked {
							isDisable = true
							startSignal.trySend(Unit)
						}
					}
					mainLabel.text = worldTimeMessage("paused")
				}
				startSignal.receive()
			}

			val drawSignal = produce {
				var lastDrawTime = System.currentTimeMillis()
				while (isActive) {
					val nextDrawTime = lastDrawTime + visualConf.framePeriod
					val delayTime = nextDrawTime - System.currentTimeMillis()
					if (delayTime > 0) delay(delayTime)
					send(delayTime)
					lastDrawTime = System.currentTimeMillis()
				}
			}
			val loopRoutines = Array(3) {
				launch {
					while (isActive) {
						doLoop(drawSignal)
					}
				}
			}

			launch(Dispatchers.JavaFx) {
				startButton.run {
					text = "pause"
					isDisable = false
					setOnMouseClicked { _ ->
						isDisable = true
						loopRoutines.forEach { it.cancel() }
					}
				}
			}
			loopRoutines.forEach { it.join() }
			drawSignal.cancel()
		}
		startSignal.cancel()
	}

	private suspend fun doLoop(drawSignal: ReceiveChannel<Long>) {
		withContext(NonCancellable) {
			//world process
			physicsMutex.lock()
			world.process(physicsConf.timeUnit, physicsConf.processCount)

			//paint
			val delayTime = drawSignal.receive()
			val message = worldTimeMessage(kotlin.run {
				when {
					isExporting -> "exporting"
					delayTime < 2L -> "hardworking"
					else -> null
				}
			})
			canvasMutex.lock()
			launch(Dispatchers.JavaFx) {
				painter.paint(canvas.graphicsContext2D)
				mainLabel.text = message
			}.join()
			physicsMutex.unlock()

			//check export
			exportConf?.exportTimeRange?.run {
				isExporting = contains(world.time)
			}
			if (!isExporting) {
				canvasMutex.unlock()
				return@withContext
			}
			if (exportConf == null) {
				isExporting = false
				canvasMutex.unlock()
				launch(Dispatchers.JavaFx) {
					dialog("Error!") {
						text = "The exportConf is null!"
						autosize()
					}
				}
				return@withContext
			}

			//export
			snapshotMutex.lock()
			launch(Dispatchers.JavaFx) { snapshotTaker.takeSnapshot(canvas) }.join()
			canvasMutex.unlock()
			exporterMutex.lock()
			fileExporter?.exportImage(snapshotTaker.bufferedImage!!)
			snapshotMutex.unlock()
			exporterMutex.unlock()
		}
	}

	private val physicsMutex = Mutex()
	private val canvasMutex = Mutex()
	private val snapshotMutex = Mutex()
	private val exporterMutex = Mutex()


	//business
	private val world by lazy { physicsConf.world() }
	private val painter by lazy { visualConf.painter(world) }
	private val snapshotTaker by lazy { SnapshotTaker() }
	private val fileExporter by lazy { exportConf?.run { FileExporter(exportDir, exportPrefix) } }

	private var isExporting = false
		set(value) {
			if (value == field) return
			field = value

			if (exportConf.autoExport) return
			launch(Dispatchers.JavaFx) {
				exportButton.text = if (value) "stop export" else "export"
			}

		}

	private fun interactWorld() {
		launch { physicsMutex.withLock { physicsConf.interact(world) } }
	}

	private fun worldTimeMessage(message: String? = null): String {
		val addition = message?.let { "($it)" } ?: ""
		return "time: ${String.format("%.2f", world.time)} $addition"
	}


}
