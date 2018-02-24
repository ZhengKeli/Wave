package zkl.science.wave.world

import java.util.*

internal fun processNode(node: Node, timeUnit: Float) {
	node.run {
		this.offset += this.velocity * timeUnit
		this.velocity -= this.velocity * this.damping * timeUnit
	}
}

internal fun <NodeId, LinkId> processLink(world: World<NodeId, LinkId>, link: Link<NodeId>, timeUnit: Float) {
	val unit1 = world.getNode(link.unitId1)
	val unit2 = world.getNode(link.unitId2)
	val impact = (unit1.offset - unit2.offset) * link.strength * timeUnit
	unit1.velocity -= impact / unit1.mass
	unit2.velocity += impact / unit2.mass
}

internal fun <NodeId, LinkId> processSource(world: World<NodeId, LinkId>, source: Source<NodeId>, timeUnit: Float) {
	val targetUnit = world.getNode(source.nodeId)
	val invokerValue = source.getValue(world.time - source.startTime)
	when (source.type) {
		SourceType.FORCE -> targetUnit.velocity += invokerValue / targetUnit.mass * timeUnit
		SourceType.POSITION -> targetUnit.offset = invokerValue
	}
}

abstract class AbstractWorld<NodeId, in LinkId> : World<NodeId, LinkId> {
	
	protected abstract val nodes: Iterable<Node>
	
	protected abstract val links: Iterable<Link<NodeId>>
	
	override val sources: MutableList<Source<NodeId>> = LinkedList<Source<NodeId>>()
	
	override var extra: Any? = null
	
	
	override var time: Float = 0.0f
		protected set
	
	override fun process(timeUnit: Float, repeat: Int) {
		repeat(repeat) {
			
			//sources
			sources.removeIf { source ->
				if (time < source.startTime) return@removeIf false
				if (time > source.startTime + source.span) return@removeIf true
				processSource(this@AbstractWorld, source, timeUnit)
				return@removeIf false
			}
			
			//process
			links.forEach { processLink(this@AbstractWorld, it, timeUnit) }
			nodes.forEach { processNode(it, timeUnit) }
			
			//time
			time += timeUnit
		}
	}
	
}
