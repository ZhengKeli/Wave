package zkl.science.wave.conf.physics

import zkl.science.wave.world.line.LineLinkDraft
import zkl.science.wave.world.line.LineNodeDraft
import zkl.science.wave.world.rect.RectLinkDraft
import zkl.science.wave.world.rect.RectNodeDraft

data class InstantNodeDraft(
	override val mass: Float,
	override val damping: Float = 0.0f,
	override val offset: Float = 0.0f,
	override val velocity: Float = 0.0f,
	override val extra: Any? = null
) : LineNodeDraft, RectNodeDraft

data class InstantLinkDraft(
	override val strength: Float,
	override val extra: Any? = null
) : LineLinkDraft, RectLinkDraft
