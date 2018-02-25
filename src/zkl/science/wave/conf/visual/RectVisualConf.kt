package zkl.science.wave.conf.visual

import zkl.science.wave.conf.Conf
import zkl.science.wave.painter.EnergyRectPainter
import zkl.science.wave.painter.OffsetRectPainter
import zkl.science.wave.painter.RectPainterConf
import zkl.science.wave.world.rect.RectWorld
import kotlin.math.min

fun Conf.rectVisual(body: RectVisualConf.() -> Unit) {
	visualConf = RectVisualConf().apply(body)
}

class RectVisualConf : VisualConf(), RectPainterConf {
	
	override val matchWorldSize: Boolean = true
	override fun matchWorldSize(world: RectWorld) {
		viewScale = min(canvasWidth / world.nodeCountX, canvasHeight / world.nodeCountY)
		viewportX = -((canvasWidth / viewScale) - world.nodeCountX) / 2
		viewportY = -((canvasHeight / viewScale) - world.nodeCountY) / 2
		canvasWidth = world.nodeCountX * viewScale
		canvasHeight = world.nodeCountY * viewScale
	}
	
}

fun RectVisualConf.energyPainter() {
	painter = { EnergyRectPainter(this, it as RectWorld) }
}

fun RectVisualConf.offsetPainter() {
	painter = { OffsetRectPainter(this, it as RectWorld) }
}
