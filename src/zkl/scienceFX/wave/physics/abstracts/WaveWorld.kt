package zkl.scienceFX.wave.physics.abstracts


interface WaveWorld : WaveWorldDraft {
	
	override val units: List<WaveUnit>
	override val links: List<WaveLink>
	override var extra: Any?
	
	val invokers:MutableList<WaveInvoker>
	
	
	val time: Float
	
	fun process(timeUnit: Float, count: Int = 1)
	
	fun processLink(link: WaveLink, timeUnit: Float) {
		val unit1 = units[link.unitId1]
		val unit2 = units[link.unitId2]
		val impact = (unit1.offset - unit2.offset) * link.strength * timeUnit
		unit1.velocity -= impact / unit1.mass
		unit2.velocity += impact / unit2.mass
	}
	
	fun processUnit(unit: WaveUnit, timeUnit: Float) {
		unit.run {
			offset += velocity * timeUnit
			velocity -= velocity * damping * timeUnit
		}
	}
	
	fun processInvoke(invoker: WaveInvoker, timeUnit: Float) {
		val targetUnit = units[invoker.invokedUnitId]
		val invokerValue = invoker.getValue(time - invoker.startTime)
		when (invoker.type) {
			WaveInvoker.Type.FORCE -> targetUnit.velocity += invokerValue / targetUnit.mass * timeUnit
			WaveInvoker.Type.POSITION -> targetUnit.offset = invokerValue
		}
	}
	
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
