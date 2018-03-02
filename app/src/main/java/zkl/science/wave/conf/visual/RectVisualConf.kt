package zkl.science.wave.conf.visual

import zkl.science.wave.conf.Conf
import zkl.science.wave.conf.physics.RectPhysicsConf
import zkl.science.wave.painter.EnergyRectPainter
import zkl.science.wave.painter.OffsetRectPainter
import zkl.science.wave.painter.RectPainterDraft
import zkl.science.wave.world.rect.RectWorld

fun Conf.rectVisual(body: RectVisualConf.() -> Unit) {
	val physicsConf = physicsConf as RectPhysicsConf
	visualConf = RectVisualConf(physicsConf.nodeCountX, physicsConf.nodeCountY).apply(body)
}

class RectVisualConf(nodeCountX: Int, nodeCountY: Int) : VisualConf(), RectPainterDraft {
	init {
		canvasWidth = nodeCountX.toDouble()
		canvasHeight = nodeCountY.toDouble()
	}
}

fun RectVisualConf.energyPainter() {
	painter = { EnergyRectPainter(this, it as RectWorld) }
}

fun RectVisualConf.offsetPainter() {
	painter = { OffsetRectPainter(this, it as RectWorld) }
}
