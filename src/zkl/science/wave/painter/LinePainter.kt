package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.StrokeLineCap
import zkl.science.wave.world.line.LineWorld

interface LinePainterDraft : PainterDraft {
	val sceneWidth: Double
	val sceneHeight: Double
	val scenePadding: Double
	val backgroundColor: Paint
}

/**
 * 一维线条的波动渲染
 */
class LinePainter(draft: LinePainterDraft, val world: LineWorld) : Painter(draft), LinePainterDraft {
	
	override val sceneWidth: Double = draft.sceneWidth
	override val sceneHeight: Double = draft.sceneHeight
	override val scenePadding: Double = draft.scenePadding
	override val backgroundColor: Paint = draft.backgroundColor
	
	private val interval: Double = (draft.sceneWidth - scenePadding * 2.0) / world.length
	private val radius: Double = if (interval > 12.0) interval / 3.0 else 0.0
	private val lineWidth: Double = if (interval > 12.0) radius / 2.0 else 2.0
	
	override fun paint(gc: GraphicsContext) {
		gc.save()
		gc.run {
			fill = backgroundColor
			fillRect(0.0, 0.0, sceneWidth, sceneHeight)
		}
		gc.translate(viewportX, viewportY)
		gc.scale(viewScale, viewScale)
		gc.translate(scenePadding, 0.0)
		kotlin.run {
			for (id in 0 until world.length) {
				val link = world.getLink(id)
				val node1 = world.getNode(link.nodeId1)
				val node1X = +link.nodeId1.x * interval
				val node1Y = -node1.offset * intensity
				val node2 = world.getNode(link.nodeId2)
				val node2X = +link.nodeId2.x * interval
				val node2Y = -node2.offset * intensity
				
				gc.stroke = colorMix(node1.color ?: Color.WHITE, node2.color ?: Color.WHITE)
				gc.lineWidth = lineWidth
				gc.lineCap = StrokeLineCap.ROUND
				gc.strokeLine(node1X, node1Y, node2X, node2Y)
			}
			if (radius <= 0.0) return@run
			for (id in 0..world.length) {
				val node = world.getNode(id)
				val nodeX = +id * interval
				val nodeY = -node.offset * intensity
				
				gc.fill = node.color ?: Color.WHITE
				gc.fillOval(nodeX - radius, nodeY - radius, radius * 2, radius * 2)
			}
		}
		gc.restore()
	}
	
}
