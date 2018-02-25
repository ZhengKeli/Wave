package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import zkl.science.wave.world.line.LineWorld

interface LinePainterDraft : PainterDraft {
	val scenePadding: Double
	val offsetScale: Double
	val backgroundFill: Paint
}

/**
 * 一维线条的波动渲染
 */
class LinePainter(draft: LinePainterDraft, val world: LineWorld) : Painter {
	
	private val viewportX: Double = draft.viewportX
	private val viewportY: Double = draft.viewportY
	private val viewportWidth: Double = draft.viewportWidth
	private val viewportHeight: Double = draft.viewportHeight
	
	private val startX: Double = draft.scenePadding
	private val startY: Double = draft.viewportHeight / 2.0
	
	private val interval: Double = (draft.sceneWidth - draft.scenePadding * 2.0) / world.length
	private val radius: Double = if (interval < 15.0) interval / 3.0 else 0.0
	private val lineWidth: Double = if (interval < 15.0) radius / 1.5 else 5.0
	private val offsetScale: Double = draft.offsetScale
	
	private val backgroundFill: Paint = draft.backgroundFill
	
	override fun paint(gc: GraphicsContext) {
		gc.save()
		gc.translate(viewportX, viewportY)
		kotlin.run {
			gc.run {
				fill = backgroundFill
				fillRect(0.0, 0.0, viewportWidth, viewportHeight)
			}
			for (id in 0 until world.length) {
				val link = world.getLink(id)
				val node1 = world.getNode(link.unitId1)
				val node1X = startX + link.unitId1 * interval
				val node1Y = startY - node1.offset * offsetScale
				val node2 = world.getNode(link.unitId2)
				val node2X = startX + link.unitId2 * interval
				val node2Y = startY - node2.offset * offsetScale
				
				gc.stroke = colorMix(node1.color ?: Color.WHITE, node2.color ?: Color.WHITE)
				gc.lineWidth = lineWidth
				gc.strokeLine(node1X, node1Y, node2X, node2Y)
			}
			if (radius > 0.0) return@run
			for (id in 0..world.length) {
				val node = world.getNode(id)
				val nodeX = startX + id * interval
				val nodeY = startY - node.offset * offsetScale
				
				gc.fill = node.color
				gc.fillOval(nodeX - radius, nodeY - radius, radius * 2, radius * 2)
			}
		}
		gc.restore()
	}
	
}
