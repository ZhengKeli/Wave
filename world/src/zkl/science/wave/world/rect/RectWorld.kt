package zkl.science.wave.world.rect

import zkl.science.wave.world.World

data class RectNodeId(val x: Int, val y: Int)

data class RectLinkId(val x: Int, val y: Int, val h: Int)

interface RectNodeDraft {
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface RectLinkDraft {
	val strength: Float
	val extra: Any?
}

interface RectWorldDraft {
	val width: Int
	val height: Int
	
	val nodeCountX: Int get() = width + 1
	val nodeCountY: Int get() = height + 1
	fun getNode(x: Int, y: Int): RectNodeDraft
	
	val linkCountX: Int get() = width
	val linkCountY: Int get() = height
	fun getLink(x: Int, y: Int, h: Int): RectLinkDraft
	
	val extra: Any?
}

interface RectWorld : World<RectNodeId, RectLinkId> {
	val width: Int
	val height: Int
}
