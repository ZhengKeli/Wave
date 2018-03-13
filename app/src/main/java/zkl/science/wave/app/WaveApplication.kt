package zkl.science.wave.app

import javafx.application.Application
import javafx.application.Platform
import javafx.embed.swing.SwingFXUtils
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.image.WritableImage
import javafx.scene.layout.Pane
import javafx.scene.transform.Scale
import javafx.scene.transform.Transform
import javafx.stage.Stage
import javafx.stage.Window
import javafx.stage.WindowEvent
import zkl.science.wave.CONF
import zkl.science.wave.conf.Conf
import zkl.science.wave.conf.visual.VisualConf
import zkl.science.wave.painter.Painter
import zkl.science.wave.world.World
import java.awt.image.BufferedImage
import java.io.File
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.FutureTask
import java.util.concurrent.locks.ReentrantLock
import javax.imageio.ImageIO
import kotlin.concurrent.thread
import kotlin.concurrent.withLock


fun main(args: Array<String>) {
	Application.launch(WaveApplication::class.java, *args)
}

class WaveApplication : Application() {
	override fun start(stage: Stage) {
		val root = FXMLLoader.load<Pane>(WaveController::class.java.getResource("wave.fxml"))
		stage.title = "wave"
		stage.scene = Scene(root, root.prefWidth, root.prefHeight)
		stage.show()
	}
}

class WaveController {
	
	//fx
	lateinit var stage: Window
	
	@FXML
	fun initialize() {
		root.sceneProperty().addListener { _, _, newScene ->
			newScene?.windowProperty()?.addListener { _, _, newWindow ->
				newWindow?.setOnCloseRequest { onRequestedClose(it) }
				stage = newWindow
			}
		}
		if (conf.exportConf == null) {
			b_export.isDisable = true
		}
	}
	
	//control
	
	@FXML
	private lateinit var root: Pane
	@FXML
	lateinit var b_start: Button
	@FXML
	lateinit var b_invoke: Button
	@FXML
	lateinit var b_export: Button
	
	@FXML
	fun onStartButtonClicked() {
		thread {
			stateLock.withLock {
				when (appState) {
					AppState.infant -> startInitialization()
					AppState.looping -> pauseLoop()
					AppState.paused -> startLoop()
					else -> {
					}
				}
			}
		}
	}
	
	@FXML
	fun onInvokeButtonClicked() {
		conf.physicsConf.interact(world)
	}
	
	@FXML
	fun onExportButtonCLicked() {
		isAutoModeOn = false
		if (exporting) {
			exporting = false
			openExportDir()
		} else {
			exporting = true
		}
	}
	
	fun onRequestedClose(windowEvent: WindowEvent) {
		windowEvent.consume()
		stateLock.withLock {
			if (appState !in arrayOf(AppState.initializing, AppState.timeOffsetting)) {
				thread {
					stopAll()
					Platform.exit()
				}
			}
		}
	}
	
	
	//configurations
	val conf: Conf = CONF
	
	//threads
	enum class AppState { infant, initializing, timeOffsetting, looping, pausing, paused, stopping, stopped }
	
	var appState: AppState = AppState.infant
	val stateLock = ReentrantLock(true)
	
	val initializeThread = object : Thread("thread_initialize") {
		override fun run() {
			Platform.runLater {
				b_start.isDisable = true
				b_invoke.isDisable = true
			}
			showWords("initializing ...")
			
			initPhysics()
			
			val future = FutureTask<Boolean>({
				initializePainter()
				paintWaveWorld()
				b_start.text = "resume"
			}, true)
			Platform.runLater { future.run() }
			future.get()
			
			showWords("initialization done")
			
			startTimeOffset()
		}
	}
	
	fun startInitialization() {
		stateLock.withLock {
			if (appState == AppState.stopping) return
			appState = AppState.initializing
			initializeThread.start()
		}
	}
	
	val timeOffsetThread = object : Thread("thread_timeOffset") {
		override fun run() {
			if (offsetTargetTime > 0f || isAutoModeOn) {
				conf.physicsConf.interact(world)
			}
			if (offsetTargetTime > 0f) {
				showWords("computing timeOffset ...")
				processPhysics(offsetTargetTime)
				showWords("computation done")
				Platform.runLater { paintWaveWorld() }
			}
			stateLock.withLock {
				startLoop()
				if (!isAutoModeOn && offsetTargetTime > 0f) pauseLoop()
			}
			Platform.runLater {
				b_start.isDisable = false
				b_invoke.isDisable = false
			}
		}
	}
	val timeOffsetTickThread = object : Thread("thread_timeOffsetTick") {
		override fun run() {
			while (appState == AppState.timeOffsetting) {
				showOffsetTime(offsetTargetTime)
				Thread.sleep(1000)
			}
		}
	}
	
	fun startTimeOffset() {
		stateLock.withLock {
			if (appState == AppState.stopping) return
			appState = AppState.timeOffsetting
			timeOffsetThread.start()
			timeOffsetTickThread.start()
		}
	}
	
	private fun newLoopThread(id: Int) = object : Thread("thread_loop$id") {
		override fun run() {
			while (true) {
				
				//compute
				if (appState != AppState.looping) break
				worldChan.take()
				doCompute()
				
				//draw
				if (appState != AppState.looping) {
					worldChan.put(true)
					break
				}
				val sleepTime = sleepUntilNextDraw()
				canvasChan.take()
				doDrawCanvas(sleepTime)
				worldChan.put(true)
				
				//check export
				if (!checkAutoExport()) {
					canvasChan.put(true)
					continue
				}
				
				//snapshot
				if (appState != AppState.looping) {
					canvasChan.put(true)
					break
				}
				imageChan.take()
				val bufferedImage = doSnapshot()
				canvasChan.put(true)
				
				//io
				if (appState != AppState.looping) {
					imageChan.put(true)
					break
				}
				doImageIO(bufferedImage)
				imageChan.put(true)
				
			}
		}
		
		fun doCompute() {
			processPhysics(conf.physicsConf.timeUnit * conf.physicsConf.processCount)
		}
		
		fun sleepUntilNextDraw(): Long {
			val sleepTime = lastDrawTime + visualConf.framePeriod - System.currentTimeMillis()
			if (sleepTime > 0) Thread.sleep(sleepTime, 0)
			lastDrawTime = System.currentTimeMillis()
			return sleepTime
		}
		
		fun doDrawCanvas(sleepTime: Long) {
			val message = when {
				appState == AppState.paused -> "paused"
				exporting -> "exporting"
				sleepTime < 2L -> "hardworking"
				else -> null
			}
			val drawTask = FutureTask {
				synchronized(world) {
					paintWaveWorld()
					showProcessedTime(message)
				}
			}
			Platform.runLater { drawTask.run() }
			drawTask.get()
		}
		
		fun doSnapshot(): BufferedImage {
			val snapshotTask = FutureTask {
				takeSnapshot()
			}
			Platform.runLater { snapshotTask.run() }
			return snapshotTask.get() //保证snapshot已经完成
		}
		
		fun doImageIO(bufferedImage: BufferedImage) {
			try {
				exportImage(bufferedImage)
			} catch (e: Exception) {
				println("imageIO occurred exception!")
				exporting = false
			}
		}
	}
	
	val loopThreads = ArrayList<Thread>()
	fun startLoop() {
		stateLock.withLock {
			if (appState == AppState.stopping) return
			appState = AppState.looping
			
			worldChan.put(true)
			canvasChan.put(true)
			imageChan.put(true)
			lastDrawTime = System.currentTimeMillis()
			
			repeat(3) { id ->
				newLoopThread(id)
					.also { loopThreads.add(it) }
					.start()
			}
			
		}
		Platform.runLater { b_start.text = "pause" }
	}
	
	fun pauseLoop() {
		stateLock.withLock {
			if (appState == AppState.stopping) return
			appState = AppState.pausing
			loopThreads.forEach { it.join() }
			loopThreads.clear()
			worldChan.clear()
			canvasChan.clear()
			imageChan.clear()
			appState = AppState.paused
		}
		Platform.runLater { b_start.text = "resume" }
	}
	
	fun stopLoop() {
		stateLock.withLock {
			loopThreads.forEach { it.join() }
			loopThreads.clear()
		}
		Platform.runLater { b_start.isDisable = true }
	}
	
	var lastDrawTime: Long = 0L
	val worldChan = ArrayBlockingQueue<Boolean>(1)
	val canvasChan = ArrayBlockingQueue<Boolean>(1)
	val imageChan = ArrayBlockingQueue<Boolean>(1)
	
	val exportingLock = ReentrantLock(true)
	var exporting = false
		set(value) {
			exportingLock.withLock {
				field = value
				if (value) {
					Platform.runLater { b_export.text = "StopExport" }
				} else {
					Platform.runLater { b_export.text = "Export" }
				}
			}
		}
	var isAutoModeOn = conf.exportConf?.let { it.exportTimeRange != null } == true
	fun checkAutoExport(): Boolean {
		exportingLock.withLock {
			if (isAutoModeOn) {
				val shouldExport = conf.exportConf?.exportTimeRange?.contains(world.time) == true
				if (shouldExport && !exporting) {
					exporting = true
				} else if (!shouldExport && exporting) {
					pauseLoop()
					exporting = false
					openExportDir()
				}
			}
			return exporting
		}
	}
	
	fun stopAll() {
		if (exporting) {
			exporting = false
			isAutoModeOn = false
			openExportDir()
		}
		val oldState = appState
		stateLock.withLock { appState = AppState.stopping }
		when (oldState) {
			AppState.initializing -> initializeThread.join()
			AppState.timeOffsetting -> timeOffsetThread.join()
			AppState.looping -> stopLoop()
			else -> {
			}
		}
		stateLock.withLock { appState = AppState.stopped }
	}
	
	
	//label & canvas
	val visualConf: VisualConf get() = conf.visualConf!!
	private val painter: Painter by lazy { visualConf.painter(world) }
	@FXML
	lateinit var canvas: Canvas
	@FXML
	lateinit var canvasPane: Pane
	private var paneScale: Scale? = null
	private fun initializePainter() {
		showWords("initializing painter ...")
		canvas.width = visualConf.canvasWidth
		canvas.height = visualConf.canvasHeight
		
		val scale = Math.min(canvasPane.width / canvas.width, canvasPane.height / canvas.height)
		Platform.runLater {
			if (paneScale == null) {
				paneScale = Transform.scale(scale, scale)
				canvasPane.transforms.add(paneScale!!)
			} else {
				canvasPane.widthProperty().addListener { _, _, newValue ->
					val newScale = Math.min(newValue.toDouble() / canvas.width, canvasPane.height / canvas.height)
					paneScale!!.x = newScale
					paneScale!!.y = newScale
				}
				canvasPane.heightProperty().addListener { _, _, newValue ->
					val newScale = Math.min(canvasPane.width / canvas.width, newValue.toDouble() / canvas.height)
					paneScale!!.x = newScale
					paneScale!!.y = newScale
				}
			}
			stage.sizeToScene()
		}
	}
	
	fun paintWaveWorld() {
		canvas.graphicsContext2D.run {
			painter.paint(this@run)
		}
	}
	
	@FXML
	lateinit var mainLabel: Label
	
	fun showWords(words: String) {
		println(words)
		Platform.runLater { mainLabel.text = words }
	}
	
	val offsetTargetTime = Math.max(conf.physicsConf.timeOffset, conf.exportConf?.exportTimeRange?.start ?: 0f)
	fun showOffsetTime(targetTime: Float) {
		val showingTime = String.format("%.2f", world.time)
		val words = "time: $showingTime/$targetTime  [computing...]"
		showWords(words)
	}
	
	fun showProcessedTime(message: String? = null) {
		var words = "time: " + String.format("%.2f", world.time)
		if (message != null) words += "  ($message)"
		showWords(words)
	}
	
	
	//image export
	var imageId: Int = 0
	var snapshotImage: WritableImage? = null
	val snapshotParameters = SnapshotParameters()
	fun takeSnapshot(): BufferedImage {
		val snapshotImage = canvas.snapshot(snapshotParameters, snapshotImage)
		val bufferedImage = SwingFXUtils.fromFXImage(snapshotImage!!, null)!!
		System.gc()
		return bufferedImage
	}
	
	fun exportImage(bufferedImage: BufferedImage) {
		conf.exportConf!!.run {
			if (imageId == 0) {
				exportDir.deleteRecursively()
				exportDir.mkdirs()
			}
			val imageFile = File(exportDir, "$exportPrefix$imageId.png")
			ImageIO.write(bufferedImage, "png", imageFile)
			imageId++
			System.gc()
			println("exported ${imageFile.name}")
		}
	}
	
	fun openExportDir() {
		try {
			conf.exportConf?.exportDir?.path?.let {
				Runtime.getRuntime().exec("explorer \"$it\"")
			}
		} catch (e: Exception) {
		}
	}
	
	
	//world
	val world: World<*, *> by lazy { conf.physicsConf.world() }
	
	fun initPhysics() {
		world
	}
	
	fun processPhysics(span: Float) {
		world.process(conf.physicsConf.timeUnit, (span / conf.physicsConf.timeUnit).toInt())
	}
	
}
