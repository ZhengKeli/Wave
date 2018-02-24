package zkl.science.wave.world


interface Node {
	var offset: Float
	var velocity: Float
	var mass: Float
	var damping: Float
	var extra: Any?
}

interface Link<out NodeId> {
	val unitId1: NodeId
	val unitId2: NodeId
	var strength: Float
	var extra: Any?
}

interface World<NodeId, in LinkId> {
	
	fun getNode(id: NodeId): Node
	fun getLink(id: LinkId): Link<NodeId>
	val sources: MutableList<Source<NodeId>>
	var extra: Any?
	
	val time: Float
	fun process(timeUnit: Float, repeat: Int = 1)
	
}