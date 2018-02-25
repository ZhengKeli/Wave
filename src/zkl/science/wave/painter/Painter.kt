package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext


interface PainterDraft {
	
	val sceneWidth: Double
	val sceneHeight: Double
	
	val viewportX: Double
	val viewportY: Double
	val viewportWidth: Double
	val viewportHeight: Double
	
	val intensity:Double
	
}

interface Painter {
	
	fun paint(gc: GraphicsContext)
	
}

