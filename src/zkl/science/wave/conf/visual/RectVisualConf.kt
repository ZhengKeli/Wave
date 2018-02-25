package zkl.science.wave.conf.visual

import zkl.science.wave.conf.Conf
import zkl.science.wave.painter.EnergyRectPainter
import zkl.science.wave.painter.OffsetRectPainter
import zkl.science.wave.painter.RectPainterDraft
import zkl.science.wave.world.rect.RectWorld

fun Conf.rectVisual(body: RectVisualConf.() -> Unit) {
	visualConf = RectVisualConf().apply(body)
}

class RectVisualConf : VisualConf(), RectPainterDraft {
	
	override var sceneWidth: Double = canvasWidth
	override var sceneHeight: Double = canvasHeight
	
	override var viewportX: Double = 0.0
	override var viewportY: Double = 0.0
	override var viewportWidth: Double = canvasWidth
	override var viewportHeight: Double = canvasHeight
	
	override var samplingSize: Double = 1.0
	override val drawingSize: Double = 1.0
	
}

fun RectVisualConf.energyPainter(){
	painter = { EnergyRectPainter(this, it as RectWorld) }
}

fun RectVisualConf.offsetPainter(){
	painter = { OffsetRectPainter(this, it as RectWorld) }
}
