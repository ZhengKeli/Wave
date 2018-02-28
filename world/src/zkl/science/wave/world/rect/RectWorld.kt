package zkl.science.wave.world.rect

import zkl.science.wave.world.*

interface RectWorldDraft {
	val width: Int
	val height: Int
	
	val nodeCountX: Int get() = width + 1
	val nodeCountY: Int get() = height + 1
	fun getNode(x: Int, y: Int): NodeProperties
	
	val linkCountX: Int get() = width
	val linkCountY: Int get() = height
	fun getLink(x: Int, y: Int, h: Int): LinkProperties
	
	val extra: Any?
}

data class RectNodeId(val x: Int, val y: Int)
data class RectLinkId(val x: Int, val y: Int, val h: Int)

typealias RectNode = Node<RectNodeId>
typealias RectLink = Link<RectNodeId, RectLinkId>

interface RectWorld : World<RectNodeId, RectLinkId>, RectWorldDraft {
	
	override fun getNode(x: Int, y: Int): RectNode
	override fun getLink(x: Int, y: Int, h: Int): RectLink
	
	override fun getNode(id: RectNodeId): RectNode = id.run { getNode(x, y) }
	override fun getLink(id: RectLinkId): RectLink = id.run { getLink(x, y, h) }
	
	fun getOffsetSnapshot(): Array<FloatArray> {
		return Array(width) { x -> FloatArray(height) { y -> getNode(RectNodeId(x, y)).offset } }
	}
	
}
