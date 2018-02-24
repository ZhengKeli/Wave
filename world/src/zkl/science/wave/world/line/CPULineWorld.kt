package zkl.science.wave.world.line

import zkl.science.wave.world.AbstractWorld
import zkl.science.wave.world.Link
import zkl.science.wave.world.Node

class CPULineNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class CPULineLink(
	override val unitId1: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int> {
	override val unitId2: Int get() = unitId1 + 1
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
