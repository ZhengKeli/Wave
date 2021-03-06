package zkl.science.wave.painter

import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color


interface PainterDraft {
	val viewportX: Double
	val viewportY: Double
	val viewScale: Double
	val intensity: Double
}

abstract class Painter(draft: PainterDraft) : PainterDraft {
	
	override val viewportX: Double = draft.viewportX
	override val viewportY: Double = draft.viewportY
	override val viewScale: Double = draft.viewScale
	override val intensity: Double = draft.intensity
	
	abstract fun paint(gc: GraphicsContext)
	abstract val backgroundColor: Color
	abstract val foregroundColor: Color
}

