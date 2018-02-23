package zkl.scienceFX.wave.physics.simple

import zkl.scienceFX.wave.physics.abstracts.*
import java.util.*

class SimpleWorld(draft: WorldDraft) : World {
	
	override var nodes: List<Node> = draft.nodes.map { unitDraft ->
		InstantNode(unitDraft.id, unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	override var links: List<Link> = draft.links.map { linkDraft ->
		InstantLink(linkDraft.unitId1, linkDraft.unitId2, linkDraft.strength, linkDraft.extra)
	}
	override var extra: Any? = draft.extra
	
	override val invokers:MutableList<Source> = LinkedList()
	
	
	
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
			nodes.forEach { processUnit(it, timeUnit) }
			
			//time
			this.time += timeUnit
		}
	}
	
}



