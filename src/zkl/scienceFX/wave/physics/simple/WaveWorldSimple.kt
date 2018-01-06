package zkl.scienceFX.wave.physics.simple

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class WaveWorldSimple:WaveWorld{
	override var isDeployed: Boolean = false
		private set
	
	@Synchronized override fun deploy(draft: WaveWorldDraft) {
		println("constructing world ...")
		units = draft.units.mapIndexed { index, unitDraft ->
			InstantWaveUnit(index,unitDraft.offset,unitDraft.velocity,unitDraft.mass,unitDraft.damping,unitDraft.extra)
		}
		links = draft.links.mapIndexed { index, linkDraft ->
			InstantWaveLink(index, linkDraft.unitId1, linkDraft.unitId2, linkDraft.strength, linkDraft.extra)
		}
		extra= draft.extra
		time = 0.0f
		isDeployed =true
	}
	@Synchronized override fun release() {
		isDeployed =false
		
		units = emptyList()
		links = emptyList()
		System.gc()
	}
	
	
	override var extra: Any? = null
	override var units: List<WaveUnit> = emptyList()
	override var links: List<WaveLink> = emptyList()
	
	
	override var time: Float = 0.0f
		private set
	private val WaveLink.unit1: WaveUnit get()=units[unitId1]
	private val WaveLink.unit2: WaveUnit get()=units[unitId2]
	@Synchronized override fun process(timeUnit: Float, count: Int) {
		for (c in 1..count) {
			//invokers
			synchronized(invokers) {
				val invokerIterator= invokers.iterator()
				while (invokerIterator.hasNext()) {
					invokerIterator.next().also {invoker->
						if (time >= invoker.startTime) {
							if (time<=invoker.startTime + invoker.span) {
								invokeUnit(invoker, units[invoker.invokedUnitId],timeUnit)
							} else {
								invokerIterator.remove()
							}
						}
					}
				}
			}
			
			//process
			for (link in links) {
				link.apply {
					val impact=(unit1.offset-unit2.offset)*strength* timeUnit
					unit1.velocity-=impact/unit1.mass
					unit2.velocity+=impact/unit2.mass
				}
			}
			for (unit in units) {
				unit.run {
					offset += velocity * timeUnit
					velocity -= velocity * damping * timeUnit
				}
			}
			
			//time
			this.time += timeUnit
		}
	}
	private fun invokeUnit(invoker: WaveInvoker, targetUnit: WaveUnit, invokeTime: Float) {
		val invokerValue = invoker.getValue(time - invoker.startTime)
		when (invoker.type) {
			InvokeType.force -> targetUnit.velocity += invokerValue / targetUnit.mass * invokeTime
			InvokeType.position -> targetUnit.offset = invokerValue
		}
	}
	
	private val invokers = ArrayList<WaveInvoker>()
	override fun addInvoker(invoker: WaveInvoker) {
		synchronized(invokers) {
			invokers.add(invoker)
		}
	}
}



