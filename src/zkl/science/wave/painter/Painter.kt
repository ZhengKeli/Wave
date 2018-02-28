package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext


interface PainterDraft {
	
	val viewportX: Double
	val viewportY: Double
	val viewScale: Double
	val intensity: Double
	
}

abstract class Painter(draft: PainterDraft) {
	
	open val viewportX: Double = draft.viewportX
	open val viewportY: Double = draft.viewportY
	open val viewScale: Double = draft.viewScale
	open val intensity: Double = draft.intensity
	
	abstract fun paint(gc: GraphicsContext)
	
}

