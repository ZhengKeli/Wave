package zkl.science.wave.world.generic

import zkl.science.wave.world.*


interface GenericNodeDraft : NodeProperties

interface GenericLinkDraft : LinkProperties {
	val nodeId1: Int
	val nodeId2: Int
}

interface GenericWorldDraft {
	val nodeCount: Int get() = nodes.size
	val nodes: List<GenericNodeDraft>
	
	val linkCount: Int get() = links.size
	val links: List<GenericLinkDraft>
	
	val extra: Any?
}


interface GenericNode : Node<Int>, GenericNodeDraft

interface GenericLink : Link<Int, Int>, GenericLinkDraft

interface GenericWorld : World<Int, Int>, GenericWorldDraft {
	override val nodes: List<GenericNode>
	override val links: List<GenericLink>
	override fun getNode(id: Int): GenericNode = nodes[id]
	override fun getLink(id: Int): GenericLink = links[id]
}
