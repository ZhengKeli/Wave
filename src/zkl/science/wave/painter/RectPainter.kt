package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import zkl.science.wave.world.Node
import zkl.science.wave.world.rect.RectNodeId
import zkl.science.wave.world.rect.RectWorld
import zkl.tools.math.geometry.*


interface RectPainterDraft : PainterDraft {
	val samplingSize: Double
	val drawingSize: Double
}

abstract class RectPainter(draft: RectPainterDraft, val world: RectWorld) : Painter {
	
	val worldWidth: Int get() = world.nodeCountX
	val worldHeight: Int get() = world.nodeCountY
	
	val samplingSize = draft.samplingSize
	val drawingSize = draft.drawingSize
	val worldStart: Point2D
	val canvasStart: Point2D
	
	fun Point2D.canvasToWorld(): Point2D = (this - canvasStart) * (samplingSize / drawingSize) + worldStart
	fun Point2D.worldToCanvas(): Point2D = (this - worldStart) * (drawingSize / samplingSize) + canvasStart
	
	init {
		worldStart = zeroPoint2D()
		canvasStart = zeroPoint2D()
	}
	
	override fun paint(gc: GraphicsContext) {
		for (worldX in 0 until worldWidth) {
			for (worldY in 0 until worldHeight) {
				val color = getNodeColor(world.getNode(RectNodeId(worldX, worldY)))
				gc.pixelWriter.setColor(worldX, worldY, color)
			}
		}
	}
	
	abstract fun getNodeColor(node: Node): Color
	
}

/**
 * 位移渲染
 */
class OffsetRectPainter(draft: RectPainterDraft, world: RectWorld, val offsetScale: Double = 0.5) : RectPainter(draft, world) {
	private val backgroundFill: Color get() = Color.GRAY
	override fun getNodeColor(node: Node): Color {
		node.color?.let { return it }
		return node.run {
			var rate = Math.abs(offset * offsetScale)
			if (rate > 1.0) rate = 1.0
			val color = if (offset > 0) Color.WHITE else Color.BLACK
			return@run colorMix(backgroundFill, color, 1.0 - rate, rate)
		}
	}
}

/**
 * 波能量渲染
 */
open class EnergyRectPainter(draft: RectPainterDraft, world: RectWorld, val scale: Float = 10.0f) : RectPainter(draft, world) {
	val backgroundFill: Color get() = Color.BLACK
	val energyFill: Color = Color.WHITE
	override fun getNodeColor(node: Node): Color {
		node.color?.let { return it }
		return node.run {
			var rate = mass * velocity * velocity / 2.0f * scale
			if (rate > 1.0) rate = 1.0f
			return@run colorMix(backgroundFill, energyFill, 1.0 - rate, rate.toDouble())
		}
	}
}
