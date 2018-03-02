package zkl.science.wave.world.line

import zkl.science.wave.world.*

interface LineWorldDraft {
	val length: Int
	
	val nodeCount get() = length + 1
	fun getNode(x: Int): NodeProperties
	
	val linkCount get() = length
	fun getLink(x: Int): LinkProperties
	
	val extra: Any?
	
}

data class LineNodeId(val x: Int)
data class LineLinkId(val x: Int)

typealias LineNode = Node<LineNodeId>
typealias LineLink = Link<LineNodeId, LineLinkId>

interface LineWorld : World<LineNodeId, LineLinkId>, LineWorldDraft {
	override val length: Int
	override val linkCount get() = length
	
	override fun getNode(x: Int): LineNode
	override fun getLink(x: Int): LineLink
	
	override fun getNode(id: LineNodeId): LineNode = getNode(id.x)
	override fun getLink(id: LineLinkId): LineLink = getLink(id.x)
	
	fun getOffsetSnapshot(): FloatArray {
		return FloatArray(length) { x -> getNode(x).offset }
	}
	
}
