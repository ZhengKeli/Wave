package zkl.science.wave.world.generic

import zkl.science.wave.world.AbstractWorld

class CPUGenericNode(
	override val nodeId: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : GenericNode

class CPUGenericLink(
	override val linkId: Int,
	override val nodeId1: Int,
	override val nodeId2: Int,
	override var strength: Float,
	override var extra: Any?
) : GenericLink

class CPUGenericWorld(draft: GenericWorldDraft) : GenericWorld, AbstractWorld<Int, Int>() {
	
	override val nodes = draft.nodes.mapIndexed { index, unitDraft ->
		CPUGenericNode(index, unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	override val links = draft.links.mapIndexed { index, linkDraft ->
		CPUGenericLink(index, linkDraft.nodeId1, linkDraft.nodeId2, linkDraft.strength, linkDraft.extra)
	}
	
	override fun getNode(id: Int): CPUGenericNode = nodes[id]
	override fun getLink(id: Int): CPUGenericLink = links[id]
	
}
