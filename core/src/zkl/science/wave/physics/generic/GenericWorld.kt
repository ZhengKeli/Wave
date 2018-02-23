package zkl.science.wave.physics.generic

import zkl.science.wave.physics.AbstractWorld
import zkl.science.wave.physics.Link
import zkl.science.wave.physics.Node


class GenericNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class GenericLink(
	override val unitId1: Int,
	override val unitId2: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int>

class GenericWorld(draft: GenericWorldDraft) : AbstractWorld<Int, Int>() {
	
	private val _nodes = draft.nodes.map { unitDraft ->
		GenericNode(unitDraft.offset, unitDraft.velocity, unitDraft.mass, unitDraft.damping, unitDraft.extra)
	}
	private val _links = draft.links.map { linkDraft ->
		GenericLink(linkDraft.unitId1, linkDraft.unitId2, linkDraft.strength, linkDraft.extra)
	}
	
	override fun getAllNodes(): List<GenericNode> = _nodes
	override fun getAllLinks(): List<GenericLink> = _links
	
	override fun getNode(id: Int): GenericNode = getAllNodes()[id]
	override fun getLink(id: Int): GenericLink = getAllLinks()[id]
	
}



