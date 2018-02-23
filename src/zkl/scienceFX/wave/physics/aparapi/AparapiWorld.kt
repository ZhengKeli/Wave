package zkl.scienceFX.wave.physics.aparapi

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class AparapiWorld(draft: WorldDraft) : World<Int, Int> {
	
	override fun getNode(id: Int): Node = kernel.run {
		object : Node {
			override var offset: Float
				get() = if (computeCount % 2 == 0) unitsOffset_s0[id] else unitsOffset_s1[id]
				set(value) {
					if (computeCount % 2 == 0) unitsOffset_s0[id] = value else unitsOffset_s1[id] = value
				}
			override var velocity: Float
				get() = unitsVelocity[id]
				set(value) {
					unitsVelocity[id] = value
				}
			override var mass: Float
				get() = unitsMass[id]
				set(value) {
					unitsMass[id] = value
				}
			override var damping: Float
				get() = unitsDamping[id]
				set(value) {
					unitsDamping[id] = value
				}
			override var extra: Any?
				get() = unitsExtra[id]
				set(value) {
					unitsExtra[id] = value
				}
		}
	}
	
	override fun getLink(id: Int): Link<Int> = kernel.run {
		object : Link<Int> {
			override val unitId1: Int
				get() = kernel.run { impactsFromUnitId[linksImpactId2[id]] }
			override val unitId2: Int
				get() = kernel.run { impactsFromUnitId[linksImpactId1[id]] }
			override var strength: Float
				get() = impactsStrength[linksImpactId1[id]]
				set(value) {
					impactsStrength[linksImpactId1[id]] = value
					impactsStrength[linksImpactId2[id]] = value
				}
			override var extra: Any?
				get() = linksExtra[id]
				set(value) {
					linksExtra[id] = value
				}
		}
	}
	
	override val invokers: MutableList<Source<Int>> = LinkedList()
	
	override var extra: Any? = draft.extra
	
	val kernel: AparapiKernel = kotlin.run {
		println("launching OpenCL ...")
		val kernel = AparapiKernel(draft)
		
		println("warming up OpenCL ...")
		kernel.process(1, 0f, emptyList())
		
		return@run kernel
	}
	
	override val time: Float
		get() {
			kernel.run {
				return if (!isExecuting) time else time + currentPass * timeUnit
			}
		}
	
	@Synchronized
	override fun process(timeUnit: Float, count: Int) {
		kernel.process(count, timeUnit, invokers)
		synchronized(invokers) { invokers.removeIf { time > it.startTime + it.span } }
	}
	
}

