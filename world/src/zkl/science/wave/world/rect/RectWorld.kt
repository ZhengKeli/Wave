package zkl.science.wave.world.rect

import zkl.science.wave.world.LinkProperties
import zkl.science.wave.world.NodeProperties
import zkl.science.wave.world.World

data class RectNodeId(val x: Int, val y: Int)

data class RectLinkId(val x: Int, val y: Int, val h: Int)

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

interface RectWorld : World<RectNodeId, RectLinkId> {
	val width: Int
	val height: Int
	val nodeCountX: Int get() = width + 1
	val nodeCountY: Int get() = height + 1
	val linkCountX: Int get() = width
	val linkCountY: Int get() = height
	
	fun getOffsetSnapshot(): Array<FloatArray> {
		return Array(width) { x -> FloatArray(height) { y -> getNode(RectNodeId(x, y)).offset } }
	}
	
}
