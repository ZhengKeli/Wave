package zkl.scienceFX.wave.conf

import javafx.scene.paint.Color
import zkl.scienceFX.wave.fx.color
import zkl.scienceFX.wave.physics.abstracts.WaveLinkDraft
import zkl.scienceFX.wave.physics.abstracts.WaveUnitDraft

data class InstantWaveUnitDraft(
	override val id: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : WaveUnitDraft {
	constructor(id: Int, mass: Float, damping: Float) : this(id, 0.0f, 0.0f, mass, damping, null)
}

data class InstantWaveLinkDraft(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : WaveLinkDraft {
	constructor(unitId1: Int, unitId2: Int, strength: Float) : this(unitId1, unitId2, strength, null)
}

fun InstantWaveUnitDraft.setAsWall() {
	mass = Float.MAX_VALUE
	color = Color.RED
}
