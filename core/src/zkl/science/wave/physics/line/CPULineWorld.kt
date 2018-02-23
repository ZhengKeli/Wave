package zkl.science.wave.physics.line

import zkl.science.wave.physics.AbstractWorld
import zkl.science.wave.physics.Link
import zkl.science.wave.physics.Node

class CPULineNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class CPULineLink(
	private val x: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int> {
	override val unitId1: Int get() = x
	override val unitId2: Int get() = x + 1
}

class CPULineWorld(draft: LineWorldDraft) : LineWorld, AbstractWorld<Int, Int>() {
	
	override val length get() = links.size
	override val nodes = draft.run {
		Array(noteCount) {
			getNode(it).run { CPULineNode(offset, velocity, mass, damping, extra) }
		}.toMutableList()
	}
	override val links = draft.run {
		Array(linkCount) {
			getLink(it).run { CPULineLink(it, strength, extra) }
		}.toMutableList()
	}
	
	override fun getNode(id: Int): CPULineNode = nodes[id]
	override fun getLink(id: Int): CPULineLink = links[id]
	
}
