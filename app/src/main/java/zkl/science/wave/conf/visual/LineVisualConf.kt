package zkl.science.wave.conf.visual

import zkl.science.wave.conf.Conf
import zkl.science.wave.painter.LinePainter
import zkl.science.wave.painter.LinePainterDraft
import zkl.science.wave.world.line.LineWorld

fun Conf.lineVisual(body: LineVisualConf.() -> Unit) {
	visualConf = LineVisualConf().apply(body)
}

class LineVisualConf : VisualConf(), LinePainterDraft {
	
	override var canvasWidth: Double = super.canvasWidth
		set(value) {
			field = value
			sceneWidth = canvasWidth
		}
	override var canvasHeight: Double = super.canvasHeight
		set(value) {
			field = value
			sceneHeight = canvasHeight
		}
	
	override var sceneWidth: Double = canvasWidth
		set(value) {
			field = value
			viewportX = sceneWidth / 2.0 - canvasWidth / 2.0
			scenePadding = sceneWidth * 0.05
		}
	override var sceneHeight: Double = canvasHeight
		set(value) {
			field = value
			viewportY = -sceneHeight / 2.0
		}
	
	override var scenePadding: Double = sceneWidth * 0.05
	
	init {
		viewportX = +sceneWidth / 2.0 - canvasWidth / 2.0
		viewportY = -sceneHeight / 2.0
		painter = { world -> LinePainter(this, world as LineWorld) }
	}
	
}
