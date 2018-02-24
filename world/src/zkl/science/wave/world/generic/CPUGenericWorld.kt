package zkl.science.wave.world.generic

import zkl.science.wave.world.AbstractWorld
import zkl.science.wave.world.Link
import zkl.science.wave.world.Node

class CPUGenericNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class CPUGenericLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int>

class CPUGenericWorld(draft: GenericWorldDraft) : GenericWorld, AbstractWorld<Int, Int>() {
	
	override val nodeCount: Int get() = nodes.size
	override val nodes = draft.nodes.map { unitDraft ->
		CPUGenericNode(unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	
	override val linkCount: Int get() = links.size
	override val links = draft.links.map { linkDraft ->
		CPUGenericLink(linkDraft.nodeId1, linkDraft.nodeId2, linkDraft.strength, linkDraft.extra)
	}
	
	override fun getNode(id: Int): CPUGenericNode = this.nodes[id]
	override fun getLink(id: Int): CPUGenericLink = links[id]
	
}
