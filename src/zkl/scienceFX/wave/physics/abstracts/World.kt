package zkl.scienceFX.wave.physics.abstracts


interface World {
	
	val nodes: List<Node>
	val links: List<Link>
	var extra: Any?
	
	val invokers:MutableList<Source>
	
	
	val time: Float
	
	fun process(timeUnit: Float, count: Int = 1)
	
	fun processLink(link: Link, timeUnit: Float) {
		val unit1 = nodes[link.unitId1]
		val unit2 = nodes[link.unitId2]
		val impact = (unit1.offset - unit2.offset) * link.strength * timeUnit
		unit1.velocity -= impact / unit1.mass
		unit2.velocity += impact / unit2.mass
	}
	
	fun processUnit(unit: Node, timeUnit: Float) {
		unit.run {
			offset += velocity * timeUnit
			velocity -= velocity * damping * timeUnit
		}
	}
	
	fun processInvoke(invoker: Source, timeUnit: Float) {
		val targetUnit = nodes[invoker.invokedUnitId]
		val invokerValue = invoker.getValue(time - invoker.startTime)
		when (invoker.type) {
			Source.Type.FORCE -> targetUnit.velocity += invokerValue / targetUnit.mass * timeUnit
			Source.Type.POSITION -> targetUnit.offset = invokerValue
		}
	}
	
}

interface Node : NodeDraft {
	override val id: Int
	override var offset: Float
	override var velocity: Float
	override var mass: Float
	override var damping: Float
	override var extra: Any?
}

data class InstantNode(
	override val id: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

interface Link : LinkDraft {
	override val unitId1: Int
	override val unitId2: Int
	override var strength: Float
	override var extra: Any?
}

data class InstantLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : Link
