package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.LinkDraft
import zkl.scienceFX.wave.physics.abstracts.NodeDraft
import zkl.scienceFX.wave.physics.abstracts.WorldDraft


class UniversalDrafter : WaveWorldDrafter {
	
	var defaultUnitMass = 1.0f
	var defaultDamping = 0.0f
	var defaultLinkStrength = 0.3f
	
	val nodes: ArrayList<NodeDraft> = ArrayList()
	val links: ArrayList<LinkDraft> = ArrayList()
	var extra: Any? = null
	
	override fun invoke() = object : WorldDraft {
		override val nodes: List<NodeDraft> = this@UniversalDrafter.nodes
		override val links: List<LinkDraft> = this@UniversalDrafter.links
		override val extra: Any? = this@UniversalDrafter.extra
	}
	
}

fun PhysicsConf.universalDrafter(body: UniversalDrafter.() -> Unit) {
	this.waveWorldDrafter = UniversalDrafter().also { body(it) }
}

fun UniversalDrafter.unit(body: (InstantNodeDraft.() -> Unit)? = null) {
	nodes += InstantNodeDraft(nodes.size, defaultUnitMass, defaultDamping)
		.also { body?.invoke(it) }
}

fun UniversalDrafter.link(unitId1: Int, unitId2: Int, body: (InstantLinkDraft.() -> Unit)? = null) {
	links += InstantLinkDraft(unitId1, unitId2, defaultLinkStrength, null)
		.also { body?.invoke(it) }
}

