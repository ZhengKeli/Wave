package zkl.scienceFX.wave.conf

import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.abstracts.WaveLinkDraft
import zkl.scienceFX.wave.physics.abstracts.WaveUnitDraft
import zkl.scienceFX.wave.physics.abstracts.WaveWorldDraft


fun PhysicsConf.lineDraft(body: LineDrafter.() -> Unit) {
	this.waveWorldDrafter = LineDrafter().also { body(it) }
}

class LineDrafter : WaveWorldDrafter {
	
	var unitCount: Int = 100
	
	var defaultUnitMass = 1.0f
	var defaultDamping = 0.0f
	var defaultLinkStrength = 0.3f
	var defaultColor: Color = Color.WHITE
	
	var onCreateUnitDraft = ArrayList<InstantWaveUnitDraft.(index: Int) -> Unit>()
	var onCreateLinkDraft = ArrayList<InstantWaveLinkDraft.(index1: Int, index2: Int) -> Unit>()
	var extra: Any? = null
	
	override fun invoke() = object : WaveWorldDraft {
		override val units: List<WaveUnitDraft> = Array(unitCount) { i: Int ->
			InstantWaveUnitDraft(i, 0.0f, 0.0f, defaultUnitMass, defaultDamping, defaultColor).also { unit ->
				onCreateUnitDraft.forEach { factory -> factory(unit, i) }
			}
		}.asList()
		override val links: List<WaveLinkDraft> = Array(unitCount - 1) { i: Int ->
			InstantWaveLinkDraft(i, i + 1, defaultLinkStrength).also { link ->
				onCreateLinkDraft.forEach { factory -> factory(link, i, i + 1) }
			}
		}.asList()
		override val extra: Any? = this@LineDrafter.extra
	}
	
}

fun LineDrafter.onCreateUnitDraft(body: (InstantWaveUnitDraft.(index: Int) -> Unit)) {
	this.onCreateUnitDraft.add(body)
}

fun LineDrafter.onCreateLinkDraft(body: InstantWaveLinkDraft.(index1: Int, index2: Int) -> Unit) {
	this.onCreateLinkDraft.add(body)
}
