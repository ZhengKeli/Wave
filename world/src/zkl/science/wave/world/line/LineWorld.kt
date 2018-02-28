package zkl.science.wave.world.line

import zkl.science.wave.world.LinkProperties
import zkl.science.wave.world.NodeProperties
import zkl.science.wave.world.World

interface LineWorldDraft {
	val length: Int
	
	val nodeCount get() = length + 1
	fun getNode(x: Int): NodeProperties
	
	val linkCount get() = length
	fun getLink(x: Int): LinkProperties
	
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
