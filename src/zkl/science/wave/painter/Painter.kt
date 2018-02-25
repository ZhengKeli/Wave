package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext


interface PainterConf {
	
	val viewportX: Double
	val viewportY: Double
	val viewScale: Double
	val intensity: Double
	
}

abstract class Painter(conf: PainterConf) {
	
	open val viewportX: Double = conf.viewportX
	open val viewportY: Double = conf.viewportY
	open val viewScale: Double = conf.viewScale
	open val intensity: Double = conf.intensity
	
	abstract fun paint(gc: GraphicsContext)
	
}

