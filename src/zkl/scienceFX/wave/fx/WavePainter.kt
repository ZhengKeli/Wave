package zkl.scienceFX.wave.fx

import javafx.geometry.Rectangle2D
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import zkl.scienceFX.wave.conf.Conf
import zkl.scienceFX.wave.conf.RectDrafter
import zkl.scienceFX.wave.conf.VisualConf
import zkl.scienceFX.wave.conf.VisualConf2D
import zkl.scienceFX.wave.physics.abstracts.WaveLink
import zkl.scienceFX.wave.physics.abstracts.WaveUnit
import zkl.scienceFX.wave.physics.abstracts.WaveWorld
import zkl.tools.math.geometry.pointOf
import zkl.tools.math.geometry.zeroPoint2D

//abstracts

abstract class WavePainter {
	
	lateinit var conf: Conf
		private set
	lateinit var world: WaveWorld
		private set
	
	fun initialize(conf: Conf, world: WaveWorld) {
		this.conf = conf
		this.world = world
		onInitialized()
	}
	
	open fun onInitialized() {}
	
	fun release() = onRelease()
	
	open fun onRelease() {}
	
	val visualConf: VisualConf get() = conf.visualConf
	val drawPort by lazy {
		visualConf.drawArea ?: Rectangle2D(0.0, 0.0, visualConf.canvasWidth, visualConf.canvasHeight)
	}
	
	fun paint(graphicsContext: GraphicsContext) {
		onPaint(graphicsContext)
		paintBackground(graphicsContext)
		refresh(graphicsContext)
	}
	
	open fun onPaint(graphicsContext: GraphicsContext) {}
	
	open val backgroundFill: Paint get() = Color.BLACK
	fun paintBackground(graphicsContext: GraphicsContext) = onPaintBackground(graphicsContext)
	open fun onPaintBackground(graphicsContext: GraphicsContext) {
		graphicsContext.run {
			fill = backgroundFill
			fillRect(0.0, 0.0, drawPort.width, drawPort.height)
		}
	}
	
	fun refresh(graphicsContext: GraphicsContext) = onRefresh(graphicsContext)
	abstract fun onRefresh(graphicsContext: GraphicsContext)
}

abstract class WavePainter2D : WavePainter() {
	
	val visualConf2D: VisualConf2D get() = visualConf as VisualConf2D
	val drafter2D: RectDrafter get() = conf.physics.waveWorldDrafter as RectDrafter
	
	val samplingSize: Double get() = visualConf2D.samplingSize
	val columnCount: Int get() = drafter2D.columnCount
	val rowCount: Int get() = drafter2D.rowCount
	
	var startX = 0
	var startY = 0
	var paintWidth = 0
	var paintHeight = 0
	override fun onPaint(graphicsContext: GraphicsContext) {
		paintWidth = (columnCount / samplingSize).toInt()
		paintHeight = (rowCount / samplingSize).toInt()
		startX = (drawPort.minX + (drawPort.width - paintWidth) / 2.0).toInt()
		startY = (drawPort.minY + (drawPort.height - paintHeight) / 2.0).toInt()
	}
	
	override fun onRefresh(graphicsContext: GraphicsContext) {
		for (x in startX until startX + paintWidth) {
			for (y in startY until startY + paintHeight) {
				val column = Math.round((x - startX) * samplingSize).toInt()
				val row = Math.round((y - startY) * samplingSize).toInt()
				
				if (row !in 0 until rowCount) continue
				if (column !in 0 until columnCount) continue
				
				val unitId = drafter2D.getUnitId(row, column)
				val color = getUnitColor(world.units[unitId])
				graphicsContext.pixelWriter.setColor(x, y, color)
			}
		}
	}
	
	abstract fun getUnitColor(unit: WaveUnit): Color
	
}


//classes

/**
 * 一维线条的波动渲染
 */
class LinePainter(val offsetScale: Double = 1.0) : WavePainter() {
	override val backgroundFill: Paint get() = Color.BLACK
	
	var startPosition = zeroPoint2D()
	var interval: Double = 0.0
	var radius: Double = 0.0
	var lineWidth: Double = 0.0
	
	override fun onPaint(graphicsContext: GraphicsContext) {
		//先计算必要数据
		val padding = drawPort.width * 0.05
		startPosition = pointOf(drawPort.minX + padding, drawPort.minY + drawPort.height / 2.0)
		interval = (drawPort.width - padding * 2.0) / world.units.size
		radius = interval / 3.0
		lineWidth = radius / 1.5
		if (lineWidth < 3.0) {
			lineWidth = 5.0
			radius = 0.0
		}
	}
	
	override fun onRefresh(graphicsContext: GraphicsContext) {
		for (i in 0..world.links.size - 1) {
			graphicsContext.paintLink(world.links[i])
		}
		for (i in 0..world.units.size - 1) {
			graphicsContext.paintUnit(world.units[i])
		}
	}
	
	val WaveUnit.x: Double get() = startPosition.x + interval * id
	val WaveUnit.y: Double get() = startPosition.y - offset * offsetScale
	fun GraphicsContext.paintUnit(unit: WaveUnit) {
		if (radius <= 0.0) return
		fill = unit.color
		fillOval(
			unit.x - radius,
			unit.y - radius,
			radius * 2, radius * 2)
	}
	
	val WaveLink.unit1: WaveUnit get() = world.units[unitId1]
	val WaveLink.unit2: WaveUnit get() = world.units[unitId2]
	fun GraphicsContext.paintLink(link: WaveLink) {
		link.run {
			stroke = colorMix(unit1.color ?: Color.WHITE, unit2.color ?: Color.WHITE)
			lineWidth = this@LinePainter.lineWidth
			strokeLine(unit1.x, unit1.y, unit2.x, unit2.y)
		}
	}
}

/**
 * 位移渲染
 */
class ColorOffsetPainter(val offsetScale: Double = 0.5) : WavePainter2D() {
	override val backgroundFill: Color get() = Color.GRAY
	override fun getUnitColor(unit: WaveUnit): Color {
		unit.color?.let { return it }
		return unit.run {
			var rate = Math.abs(offset * offsetScale)
			if (rate > 1.0) rate = 1.0
			val color = if (offset > 0) Color.WHITE else Color.BLACK
			return@run colorMix(backgroundFill, color, 1.0 - rate, rate)
		}
	}
}

/**
 * 水波渲染
 */
class WaterSurfacePainter(val scale: Double = 3.0) : WavePainter2D() {
	override val backgroundFill: Color get() = Color.GRAY
	override fun getUnitColor(unit: WaveUnit): Color {
		unit.color?.let { return it }
		val row = unit.id / columnCount
		val column = unit.id % columnCount
		if (row < rowCount - 1 && column < columnCount - 1) {
			val dzx = unit.offset - world.units[(row + 1) * columnCount + column].offset
			val dzy = unit.offset - world.units[row * columnCount + column + 1].offset
			
			val s = (dzx * 0.6 + dzy * 0.8) * scale
			if (s < 0) {
				val rate = Math.atan(-s) / (Math.PI / 2)
				return colorMix(backgroundFill, Color.BLACK, 1.0 - rate, rate)
			} else {
				val rate = Math.atan(s) / (Math.PI / 2)
				return colorMix(backgroundFill, Color.WHITE, 1.0 - rate, rate)
			}
		} else {
			return backgroundFill
		}
	}
}

/**
 * 波能量渲染
 */
open class WaveEnergyPainter(val scale: Float = 10.0f) : WavePainter2D() {
	override val backgroundFill: Color get() = Color.BLACK
	val energyFill: Color = Color.WHITE
	override fun getUnitColor(unit: WaveUnit): Color {
		unit.color?.let { return it }
		return unit.run {
			var rate = mass * velocity * velocity / 2.0f * scale
			if (rate > 1.0) rate = 1.0f
			return@run colorMix(backgroundFill, energyFill, 1.0 - rate, rate.toDouble())
		}
	}
}

/**
 * 平滑的波能量渲染
 */
class SmoothedEnergyPainter(scale: Float = 50f) : WaveEnergyPainter(scale) {
	
	lateinit var colorMap: Array<Color>
	override fun onInitialized() {
		super.onInitialized()
		colorMap = Array(rowCount * columnCount) { unitId -> getUnitColor(world.units[unitId]) }
	}
	
	override fun onRefresh(graphicsContext: GraphicsContext) {
		for (x in startX until startX + paintWidth) {
			for (y in startY until startY + paintHeight) {
				val column = Math.round((x - startX) * samplingSize).toInt()
				val row = Math.round((y - startY) * samplingSize).toInt()
				
				if (row !in 0 until rowCount) continue
				if (column !in 0 until columnCount) continue
				
				val unitId = drafter2D.getUnitId(row, column)
				val thisColor = getUnitColor(world.units[unitId])
				val oldColor = colorMap[unitId]
				val newColor = colorMix(thisColor, oldColor, 0.01, 0.99)
				colorMap[unitId] = newColor
				graphicsContext.pixelWriter.setColor(x, y, newColor)
			}
		}
	}
	
}


//tools
internal fun colorMix(color1: Color, color2: Color, weight1: Double = 1.0, weight2: Double = 1.0): Color {
	val weightSum = weight1 + weight2
	return Color(
		(color1.red * weight1 + color2.red * weight2) / weightSum,
		(color1.green * weight1 + color2.green * weight2) / weightSum,
		(color1.blue * weight1 + color2.blue * weight2) / weightSum,
		(color1.opacity * weight1 + color2.opacity * weight2) / weightSum
	)
}
