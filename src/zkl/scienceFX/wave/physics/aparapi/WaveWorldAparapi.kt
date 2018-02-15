package zkl.scienceFX.wave.physics.aparapi

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class WaveWorldAparapi(draft: WaveWorldDraft):WaveWorld {
	
	override val units: List<WaveUnit> = object :AbstractList<WaveUnit>(){
		override val size: Int get() = kernel.unitsCount
		override fun get(index: Int): WaveUnit = kernel.run {
			object : WaveUnit {
				override val id: Int get() = index
				override var offset: Float
					get() = if (computeCount%2 == 0) unitsOffset_s0[id] else unitsOffset_s1[id]
					set(value) { if (computeCount%2 == 0) unitsOffset_s0[id] = value else unitsOffset_s1[id] = value }
				override var velocity: Float
					get() = unitsVelocity[id]
					set(value) { unitsVelocity[id] = value }
				override var mass: Float
					get() = unitsMass[id]
					set(value) { unitsMass[id] = value }
				override var damping: Float
					get() = unitsDamping[id]
					set(value) { unitsDamping[id] = value }
				override var extra: Any?
					get() = unitsExtra[id]
					set(value) { unitsExtra[id] = value }
			}
		}
	}
	override val links: List<WaveLink> = object :AbstractList<WaveLink>(){
		override val size: Int get() = kernel.linksCount
		override fun get(index: Int): WaveLink = kernel.run {
			object: WaveLink {
				override val unitId1: Int
					get() = kernel.run { impactsFromUnitId[linksImpactId2[index]] }
				override val unitId2: Int
					get() = kernel.run { impactsFromUnitId[linksImpactId1[index]] }
				override var strength: Float
					get() = impactsStrength[linksImpactId1[index]]
					set(value) {
						impactsStrength[linksImpactId1[index]] = value
						impactsStrength[linksImpactId2[index]] = value
					}
				override var extra: Any?
					get() = linksExtra[index]
					set(value) { linksExtra[index] = value }
			}
		}
	}
	override var extra: Any? =  draft.extra
	
	private val invokers = ArrayList<WaveInvoker>()
	override fun addInvoker(invoker: WaveInvoker) {
		synchronized(invokers) {
			invokers.add(invoker)
		}
	}
	
	val kernel: WaveWorldKernel = kotlin.run {
		println("launching OpenCL ...")
		val kernel = WaveWorldKernel(draft)
		
		println("warming up OpenCL ...")
		kernel.process(1, 0f, emptyList())
		
		return@run kernel
	}
	override val time: Float get() {
		kernel.run{
			return if (!isExecuting) time else time+currentPass*timeUnit
		}
	}
	@Synchronized override fun process(timeUnit: Float, count: Int) {
		kernel.process(count, timeUnit, invokers)
		synchronized(invokers) { invokers.removeIf { time>it.startTime + it.span } }
	}
	
}

