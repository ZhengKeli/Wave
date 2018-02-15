package zkl.scienceFX.wave.physics.abstracts


interface WaveWorld:WaveWorldDraft {
	
	val isDeployed: Boolean
	fun deploy(draft: WaveWorldDraft)
	fun release()
	
	val time: Float
	fun process(timeUnit: Float, count: Int = 1)
	
	override val units: List<WaveUnit>
	override val links: List<WaveLink>
	fun addInvoker(invoker: WaveInvoker)
	
	override var extra: Any?
	
}

interface WaveUnit : WaveUnitDraft {
	override val id: Int
	override var offset: Float
	override var velocity: Float
	override var mass: Float
	override var damping: Float
	override var extra: Any?
}

data class InstantWaveUnit(
	override val id: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : WaveUnit

interface WaveLink : WaveLinkDraft {
	override val unitId1: Int
	override val unitId2: Int
	override var strength: Float
	override var extra: Any?
}

data class InstantWaveLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : WaveLink
