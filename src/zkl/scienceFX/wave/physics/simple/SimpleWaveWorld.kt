package zkl.scienceFX.wave.physics.simple

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class SimpleWaveWorld(draft: WaveWorldDraft) : WaveWorld {
	
	override var units: List<WaveUnit> = draft.units.map { unitDraft ->
		InstantWaveUnit(unitDraft.id, unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	override var links: List<WaveLink> = draft.links.map { linkDraft ->
		InstantWaveLink(linkDraft.unitId1, linkDraft.unitId2, linkDraft.strength, linkDraft.extra)
	}
	override var extra: Any? = draft.extra
	
	override val invokers:MutableList<WaveInvoker> = LinkedList()
	
	
	
	override var time: Float = 0.0f
		private set
	
	@Synchronized
	override fun process(timeUnit: Float, count: Int) {
		repeat(count) {
			//invokers
			synchronized(invokers) {
				invokers.removeIf { invoker ->
					if (time < invoker.startTime) return@removeIf false
					if (time > invoker.startTime + invoker.span) return@removeIf true
					processInvoke(invoker, timeUnit)
					false
				}
			}
			
			//process
			links.forEach { processLink(it, timeUnit) }
			units.forEach { processUnit(it, timeUnit) }
			
			//time
			this.time += timeUnit
		}
	}
	
}



