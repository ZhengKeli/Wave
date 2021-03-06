package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import zkl.science.wave.world.Node
import zkl.science.wave.world.rect.RectNodeId
import zkl.science.wave.world.rect.RectWorld
import kotlin.math.roundToInt

interface RectPainterDraft : PainterDraft

/**
 * 二维方形波动渲染
 */
abstract class RectPainter(draft: RectPainterDraft, val world: RectWorld) : Painter(draft) {
	
	override fun paint(gc: GraphicsContext) {
		for (canvasX in 0 until gc.canvas.width.toInt()) {
			for (canvasY in 0 until gc.canvas.height.toInt()) {
				val worldX = (canvasX / viewScale + viewportX).roundToInt()
				if (worldX < 0 || worldX > world.width) {
					gc.pixelWriter.setColor(canvasX, canvasY, backgroundColor)
					continue
				}
				
				val worldY = (canvasY / viewScale + viewportY).roundToInt()
				if (worldY < 0 || worldY > world.height) {
					gc.pixelWriter.setColor(canvasX, canvasY, backgroundColor)
					continue
				}
				
				val nodeId = RectNodeId(worldX, worldY)
				val color = getNodeColor(world.getNode(nodeId))
				gc.pixelWriter.setColor(canvasX, canvasY, color)
			}
		}
	}
	
	abstract fun getNodeColor(node: Node<RectNodeId>): Color
	
}

/**
 * 位移渲染
 */
class OffsetRectPainter(conf: RectPainterDraft, world: RectWorld) : RectPainter(conf, world) {
	override val backgroundColor: Color = Color.GRAY
	override val foregroundColor: Color = Color.WHITE
	private val foregroundColorReverse: Color = foregroundColor.invert()
	override fun getNodeColor(node: Node<RectNodeId>): Color {
		node.color?.let { return it }
		return node.run {
			var rate = Math.abs(offset * intensity)
			if (rate > 1.0) rate = 1.0
			val color = if (offset > 0) foregroundColor else foregroundColorReverse
			return@run colorMix(backgroundColor, color, 1.0 - rate, rate)
		}
	}
}

/**
 * 波能量渲染
 */
class EnergyRectPainter(conf: RectPainterDraft, world: RectWorld) : RectPainter(conf, world) {
	override val backgroundColor: Color = Color.BLACK
	override val foregroundColor: Color = Color.WHITE
	override fun getNodeColor(node: Node<RectNodeId>): Color {
		node.color?.let { return it }
		return node.run {
			var rate = mass * velocity * velocity * intensity * 500.0
			if (rate > 1.0) rate = 1.0
			return@run colorMix(backgroundColor, foregroundColor, 1.0 - rate, rate)
		}
	}
}
