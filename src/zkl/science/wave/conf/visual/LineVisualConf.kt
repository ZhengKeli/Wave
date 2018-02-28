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
	
	override var sceneWidth: Double = canvasWidth
	override var sceneHeight: Double = canvasHeight
	override var scenePadding: Double = sceneWidth * 0.05
	override var backgroundColor: Paint = Color.BLACK
	
	init {
		viewportX = 0.0
		viewportY = sceneHeight / 2.0
		painter = { world -> LinePainter(this, world as LineWorld) }
	}
	
}
