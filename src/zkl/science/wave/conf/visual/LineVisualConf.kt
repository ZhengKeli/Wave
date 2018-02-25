package zkl.science.wave.conf.visual

import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import zkl.science.wave.conf.Conf
import zkl.science.wave.painter.LinePainter
import zkl.science.wave.painter.LinePainterDraft
import zkl.science.wave.world.line.LineWorld

fun Conf.lineVisual(body: LineVisualConf.() -> Unit) {
	visualConf = LineVisualConf().apply(body)
}

class LineVisualConf : VisualConf(), LinePainterDraft {
	
	init {
		canvasWidth = viewportWidth
		canvasHeight = viewportHeight
	}
	
	override var scenePadding: Double = sceneWidth * 0.05
	
	override var backgroundFill: Paint = Color.BLACK
	
	override var offsetScale: Double = 1.0
	
	init {
		painter = { world -> LinePainter(this, world as LineWorld) }
	}
	
}
