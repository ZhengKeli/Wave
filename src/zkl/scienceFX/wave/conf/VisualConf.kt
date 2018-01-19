package zkl.scienceFX.wave.conf

import javafx.geometry.Rectangle2D
import zkl.scienceFX.wave.fx.WavePainter


fun Conf.visual(body: VisualConf.() -> Unit) {
	this.visualConf = VisualConf().also { body.invoke(it) }
}

open class VisualConf {
	
	lateinit var painter: WavePainter
	var canvasWidth: Double = 4000.0
	var canvasHeight: Double = 3000.0
	
	var drawArea: Rectangle2D? = null
	
	var framePeriod: Long = 40 //fps = 25
	var fps: Double
		get() = 1000.0 / framePeriod
		set(value) {
			framePeriod = Math.round(1000.0 / value)
		}
	
}