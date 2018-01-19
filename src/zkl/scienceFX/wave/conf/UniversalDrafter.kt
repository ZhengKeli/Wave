package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.WaveLinkDraft
import zkl.scienceFX.wave.physics.abstracts.WaveUnitDraft
import zkl.scienceFX.wave.physics.abstracts.WaveWorldDraft


class UniversalDrafter : WaveWorldDrafter {
	
	var defaultUnitMass = 1.0f
	var defaultDamping = 0.0f
	var defaultLinkStrength = 0.3f
	
	val units: ArrayList<WaveUnitDraft> = ArrayList()
	val links: ArrayList<WaveLinkDraft> = ArrayList()
	var extra: Any? = null
	
	override fun invoke() = object : WaveWorldDraft {
		override val units: List<WaveUnitDraft> = this@UniversalDrafter.units
		override val links: List<WaveLinkDraft> = this@UniversalDrafter.links
		override val extra: Any? = this@UniversalDrafter.extra
	}
	
}

fun PhysicsConf.universalDrafter(body: UniversalDrafter.() -> Unit) {
	this.waveWorldDrafter = UniversalDrafter().also { body(it) }
}

fun UniversalDrafter.unit(body: (InstantWaveUnitDraft.() -> Unit)? = null) {
	units += InstantWaveUnitDraft(units.size, defaultUnitMass, defaultDamping)
		.also { body?.invoke(it) }
}

fun UniversalDrafter.link(unitId1: Int, unitId2: Int, body: (InstantWaveLinkDraft.() -> Unit)? = null) {
	links += InstantWaveLinkDraft(unitId1, unitId2, defaultLinkStrength, null)
		.also { body?.invoke(it) }
}

