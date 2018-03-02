package zkl.science.wave.world

interface NodeProperties {
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface LinkProperties {
	val strength: Float
	val extra: Any?
}

interface Node<out NodeId> : NodeProperties {
	val nodeId: NodeId
	override var offset: Float
	override var velocity: Float
	override var mass: Float
	override var damping: Float
	override var extra: Any?
}

interface Link<out NodeId, out LinkId> : LinkProperties {
	val linkId: LinkId
	val nodeId1: NodeId
	val nodeId2: NodeId
	override var strength: Float
	override var extra: Any?
}

interface World<NodeId, LinkId> {
	
	fun getNode(id: NodeId): Node<NodeId>
	fun getLink(id: LinkId): Link<NodeId, LinkId>
	val sources: MutableList<Source<NodeId>>
	var extra: Any?
	
	val time: Float
	fun process(timeUnit: Float, repeat: Int = 1)
	
}
