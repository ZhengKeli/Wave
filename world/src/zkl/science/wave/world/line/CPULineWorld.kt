package zkl.science.wave.world.line

import zkl.science.wave.world.AbstractWorld
import zkl.science.wave.world.Link
import zkl.science.wave.world.Node

class CPULineNode(
	override val nodeId: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node<Int>

class CPULineLink(
	override val linkId: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int, Int> {
	override val nodeId1: Int get() = linkId
	override val nodeId2: Int get() = linkId + 1
}

class CPULineWorld(draft: LineWorldDraft) : LineWorld, AbstractWorld<Int, Int>() {
	
	override val length get() = links.size
	override val nodes = draft.run {
		Array(noteCount) { x ->
			getNode(x).run { CPULineNode(x, offset, velocity, mass, damping, extra) }
		}.toMutableList()
	}
	override val links = draft.run {
		Array(linkCount) { x ->
			getLink(x).run { CPULineLink(x, strength, extra) }
		}.toMutableList()
	}
	
	override fun getNode(id: Int): CPULineNode = nodes[id]
	override fun getLink(id: Int): CPULineLink = links[id]
	
}
