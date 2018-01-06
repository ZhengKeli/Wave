package zkl.scienceFX.wave.physics.aparapi

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class WaveWorldAparapi:WaveWorld {
	override var isDeployed: Boolean = false
		private set
	
	@Synchronized override fun deploy(draft: WaveWorldDraft) {
		
		println("constructing world ...")
		//将数据序列化地储存
		kernel = WaveWorldKernel(draft)
		extra= draft.extra
		isDeployed =true
		
		
		println("warming up OpenCL ...")
		kernel!!.process(1,0f, invokers)
		
	}
	@Synchronized override fun release() {
		isDeployed =false
		
		kernel?.dispose()
		kernel = null
		
		System.gc()
	}
	
	
	override var extra: Any? = null
	override val units: List<WaveUnit> = object :AbstractList<WaveUnit>(){
		override val size: Int get() = kernel!!.unitsCount
		override fun get(index: Int): WaveUnit = kernel!!.run {
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
		override val size: Int get() = kernel!!.linksCount
		override fun get(index: Int): WaveLink = kernel!!.run {
			object: WaveLink {
				override val id: Int = index
				override val unitId1: Int
					get() = kernel.run { impactsFromUnitId[linksImpactId2[id]] }
				override val unitId2: Int
					get() = kernel.run { impactsFromUnitId[linksImpactId1[id]] }
				override var strength: Float
					get() = impactsStrength[linksImpactId1[index]]
					set(value) {
						impactsStrength[linksImpactId1[index]] = value
						impactsStrength[linksImpactId2[index]] = value
					}
				override var extra: Any?
					get() = linksExtra[id]
					set(value) { linksExtra[id] = value }
			}
		}
	}
	
	var kernel: WaveWorldKernel? = null
	override val time: Float get() {
		kernel!!.run{
			if(isExecuting) return time+currentPass*timeUnit
			else return time
		}
	}
	@Synchronized override fun process(timeUnit: Float, count: Int) {
		kernel!!.process(count, timeUnit, invokers)
		synchronized(invokers) { invokers.removeIf { time>it.startTime + it.span } }
	}
	
	private val invokers = ArrayList<WaveInvoker>()
	override fun addInvoker(invoker: WaveInvoker) {
		synchronized(invokers) {
			invokers.add(invoker)
		}
	}
}

