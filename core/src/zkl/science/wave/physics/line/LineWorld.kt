package zkl.science.wave.physics.line

import zkl.science.wave.physics.World
import zkl.science.wave.physics.generic.GenericNodeDraft

typealias LineNodeDraft = GenericNodeDraft

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
}
