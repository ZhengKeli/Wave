package zkl.science.wave.world


interface Node<out NodeId> {
	val nodeId: NodeId
	var offset: Float
	var velocity: Float
	var mass: Float
	var damping: Float
	var extra: Any?
}

interface Link<out NodeId, out LinkId> {
	val linkId: LinkId
	val nodeId1: NodeId
	val nodeId2: NodeId
	var strength: Float
	var extra: Any?
}

interface World<NodeId, LinkId> {
	
	fun getNode(id: NodeId): Node<NodeId>
	fun getLink(id: LinkId): Link<NodeId, LinkId>
	val sources: MutableList<Source<NodeId>>
	var extra: Any?
	
	val time: Float
	fun process(timeUnit: Float, repeat: Int = 1)
	
}
