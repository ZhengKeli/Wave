package zkl.science.wave.physics

import java.util.*

abstract class AbstractWorld<NodeId, in LinkId> : World<NodeId, LinkId> {
	
	protected abstract fun getAllNodes(): Collection<Node>
	
	protected abstract fun getAllLinks(): Collection<Link<NodeId>>
	
	override val sources = LinkedList<Source<NodeId>>()
	
	override var extra: Any? = null
	
	
	override var time: Float = 0.0f
		protected set
	
	override fun process(timeUnit: Float, repeat: Int) {
		repeat(repeat) {
			
			//sources
			sources.removeIf { source ->
				if (time < source.startTime) return@removeIf false
				if (time > source.startTime + source.span) return@removeIf true
				processSource(source, timeUnit)
				return@removeIf false
			}
			
			//process
			getAllLinks().forEach { processLink(it, timeUnit) }
			getAllNodes().forEach { processNode(it, timeUnit) }
			
			//time
			time += timeUnit
		}
	}
	
	private fun processNode(node: Node, timeUnit: Float) {
		node.run {
			this.offset += this.velocity * timeUnit
			this.velocity -= this.velocity * this.damping * timeUnit
		}
	}
	
	private fun processLink(link: Link<NodeId>, timeUnit: Float) {
		val unit1 = getNode(link.unitId1)
		val unit2 = getNode(link.unitId2)
		val impact = (unit1.offset - unit2.offset) * link.strength * timeUnit
		unit1.velocity -= impact / unit1.mass
		unit2.velocity += impact / unit2.mass
	}
	
	private fun processSource(source: Source<NodeId>, timeUnit: Float) {
		val targetUnit = getNode(source.nodeId)
		val invokerValue = source.getValue(this.time - source.startTime)
		when (source.type) {
			Invoking.Type.FORCE -> targetUnit.velocity += invokerValue / targetUnit.mass * timeUnit
			Invoking.Type.POSITION -> targetUnit.offset = invokerValue
		}
	}
	
}