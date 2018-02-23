package zkl.scienceFX.wave.physics.generic

import zkl.scienceFX.wave.physics.*
import java.util.*


class GenericNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class GenericLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int>

fun Node.process(timeUnit: Float) {
	run {
		offset += velocity * timeUnit
		velocity -= velocity * damping * timeUnit
	}
}

fun <NodeId> Link<NodeId>.process(world: World<NodeId, *>, timeUnit: Float) {
	val unit1 = world.getNode(this.unitId1)
	val unit2 = world.getNode(this.unitId2)
	val impact = (unit1.offset - unit2.offset) * strength * timeUnit
	unit1.velocity -= impact / unit1.mass
	unit2.velocity += impact / unit2.mass
}

fun <NodeId> Source<NodeId>.process(world: World<NodeId, *>, timeUnit: Float) {
	val targetUnit = world.getNode(this.nodeId)
	val invokerValue = getValue(world.time - startTime)
	when (type) {
		Invoking.Type.FORCE -> targetUnit.velocity += invokerValue / targetUnit.mass * timeUnit
		Invoking.Type.POSITION -> targetUnit.offset = invokerValue
	}
}

class GenericWorld(draft: GenericWorldDraft) : World<Int, Int> {
	
	private var _nodes: List<GenericNode> = draft.nodes.map { unitDraft ->
		GenericNode(unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	private var _links: List<GenericLink> = draft.links.map { linkDraft ->
		GenericLink(linkDraft.unitId1, linkDraft.unitId2, linkDraft.strength, linkDraft.extra)
	}
	
	override fun getNode(id: Int): GenericNode = _nodes[id]
	override fun getLink(id: Int): GenericLink = _links[id]
	override var extra: Any? = draft.extra
	override val invokers: MutableList<Source<Int>> = LinkedList()
	
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
					invoker.process(this, timeUnit)
					false
				}
			}
			
			//process
			_links.forEach { it.process(this, timeUnit) }
			_nodes.forEach { it.process(timeUnit) }
			
			//time
			this.time += timeUnit
		}
	}
	
}



