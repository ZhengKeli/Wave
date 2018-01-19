package zkl.scienceFX.wave.physics.abstracts


interface WaveWorld {
	
	val isDeployed: Boolean
	fun deploy(draft: WaveWorldDraft)
	fun release()
	
	val time: Float
	fun process(timeUnit: Float, count: Int = 1)
	
	val units: List<WaveUnit>
	val links: List<WaveLink>
	fun addInvoker(invoker: WaveInvoker)
	
	var extra: Any?
	
}

interface WaveUnit {
	val id: Int
	var offset: Float
	var velocity: Float
	var mass: Float
	var damping: Float
	var extra: Any?
}

data class InstantWaveUnit(
	override val id: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?) : WaveUnit

interface WaveLink {
	val unitId1: Int
	val unitId2: Int
	var strength: Float
	var extra: Any?
}

data class InstantWaveLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?) : WaveLink


