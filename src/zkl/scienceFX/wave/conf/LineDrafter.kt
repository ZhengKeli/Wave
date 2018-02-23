package zkl.scienceFX.wave.conf

import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.abstracts.LinkDraft
import zkl.scienceFX.wave.physics.abstracts.NodeDraft
import zkl.scienceFX.wave.physics.abstracts.WorldDraft


fun PhysicsConf.lineDraft(body: LineDrafter.() -> Unit) {
	this.waveWorldDrafter = LineDrafter().also { body(it) }
}

class LineDrafter : WaveWorldDrafter {
	
	var unitCount: Int = 100
	
	var defaultUnitMass = 1.0f
	var defaultDamping = 0.0f
	var defaultLinkStrength = 0.3f
	var defaultColor: Color = Color.WHITE
	
	var onCreateUnitDraft = ArrayList<InstantNodeDraft.(index: Int) -> Unit>()
	var onCreateLinkDraft = ArrayList<InstantLinkDraft.(index1: Int, index2: Int) -> Unit>()
	var extra: Any? = null
	
	override fun invoke() = object : WorldDraft {
		override val nodes: List<NodeDraft> = Array(unitCount) { i: Int ->
			InstantNodeDraft(i, 0.0f, 0.0f, defaultUnitMass, defaultDamping, defaultColor).also { unit ->
				onCreateUnitDraft.forEach { factory -> factory(unit, i) }
			}
		}.asList()
		override val links: List<LinkDraft> = Array(unitCount - 1) { i: Int ->
			InstantLinkDraft(i, i + 1, defaultLinkStrength).also { link ->
				onCreateLinkDraft.forEach { factory -> factory(link, i, i + 1) }
			}
		}.asList()
		override val extra: Any? = this@LineDrafter.extra
	}
	
}

fun LineDrafter.onCreateUnitDraft(body: (InstantNodeDraft.(index: Int) -> Unit)) {
	this.onCreateUnitDraft.add(body)
}

fun LineDrafter.onCreateLinkDraft(body: InstantLinkDraft.(index1: Int, index2: Int) -> Unit) {
	this.onCreateLinkDraft.add(body)
}
