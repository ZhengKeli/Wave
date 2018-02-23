package zkl.science.wave.physics.line

import zkl.science.wave.physics.AbstractWorld
import zkl.science.wave.physics.Link
import zkl.science.wave.physics.generic.GenericNode

typealias LineNode = GenericNode

class LineLink(
	private val x: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int> {
	override val unitId1: Int get() = x
	override val unitId2: Int get() = x + 1
}

class LineWorld(draft: LineWorldDraft) : AbstractWorld<Int, Int>() {
	
	val length get() = links.size
	override val nodes = draft.run {
		Array(noteCount) {
			getNode(it).run { LineNode(offset, velocity, mass, damping, extra) }
		}.toMutableList()
	}
	override val links = draft.run {
		Array(linkCount) {
			getLink(it).run { LineLink(it, strength, extra) }
		}.toMutableList()
	}
	
	override fun getNode(id: Int): LineNode = nodes[id]
	override fun getLink(id: Int): LineLink = links[id]
	
}
