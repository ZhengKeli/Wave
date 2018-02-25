package zkl.science.wave.conf.visual

import zkl.science.wave.conf.Conf
import zkl.science.wave.painter.Painter
import zkl.science.wave.painter.PainterConf
import zkl.science.wave.world.World
import kotlin.math.round


fun Conf.visual(body: VisualConf.() -> Unit) {
	this.visualConf = VisualConf().also { body.invoke(it) }
}

open class VisualConf : PainterConf {
	
	var canvasWidth: Double = 1200.0
	var canvasHeight: Double = 700.0
	
	override var viewportX: Double = 0.0
	override var viewportY: Double = 0.0
	override var viewScale: Double = 1.0
	
	override var intensity: Double = 1.0
	lateinit var painter: (world: World<*, *>) -> Painter
	
	var framePeriod: Long = 20
	var fps: Double
		get() = 1000.0 / framePeriod
		set(value) {
			framePeriod = round(1000.0 / value).toLong()
		}
	
}