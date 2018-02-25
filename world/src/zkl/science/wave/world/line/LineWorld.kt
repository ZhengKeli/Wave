package zkl.science.wave.world.line

import zkl.science.wave.world.World

interface LineNodeDraft {
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface LineLinkDraft {
	val strength: Float
	val extra: Any?
}

interface LineWorldDraft {
	val length: Int
	
	val noteCount get() = length + 1
	fun getNode(x: Int): LineNodeDraft
	
	val linkCount get() = length
	fun getLink(x: Int): LineLinkDraft
	
	val extra: Any?
	
}

interface LineWorld : World<Int, Int> {
	val length: Int
	val noteCount get() = length + 1
	val linkCount get() = length
	
	fun getOffsetSnapshot(): FloatArray {
		return FloatArray(length) { x -> getNode(x).offset }
	}
	
}
