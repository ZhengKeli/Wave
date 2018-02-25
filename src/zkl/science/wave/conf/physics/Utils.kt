package zkl.science.wave.conf.physics

import javafx.scene.paint.Color
import zkl.science.wave.painter.color
import zkl.science.wave.world.line.LineLinkDraft
import zkl.science.wave.world.line.LineNodeDraft
import zkl.science.wave.world.rect.RectLinkDraft
import zkl.science.wave.world.rect.RectNodeDraft

data class InstantNodeDraft(
	override var mass: Float,
	override var damping: Float = 0.0f,
	override var offset: Float = 0.0f,
	override var velocity: Float = 0.0f,
	override var extra: Any? = null
) : LineNodeDraft, RectNodeDraft

data class InstantLinkDraft(
	override var strength: Float,
	override var extra: Any? = null
) : LineLinkDraft, RectLinkDraft

fun InstantNodeDraft.setAsWall() {
	mass = Float.MAX_VALUE
	color = Color.RED
}
