package zkl.science.wave.world.rect

import zkl.science.wave.world.AbstractWorld
import zkl.science.wave.world.Link
import zkl.science.wave.world.Node


class CPURectNode(
	override val nodeId: RectNodeId,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node<RectNodeId>

class CPURectLink(
	override val linkId: RectLinkId,
	override var strength: Float,
	override var extra: Any?
) : Link<RectNodeId, RectLinkId> {
	override val nodeId1: RectNodeId get() = linkId.run { RectNodeId(x, y) }
	override val nodeId2: RectNodeId get() = linkId.run { if (h == 0) RectNodeId(x + 1, y) else RectNodeId(x, y + 1) }
}

class CPURectWorld(draft: RectWorldDraft) : RectWorld, AbstractWorld<RectNodeId, RectLinkId>() {
	
	override val width: Int = draft.width
	override val height: Int = draft.height
	
	private val _nodes = Array(draft.nodeCountX) { x ->
		Array(draft.nodeCountY) { y ->
			draft.getNode(x, y).run { CPURectNode(RectNodeId(x, y), offset, velocity, mass, damping, extra) }
		}
	}
	private val _hLinks = Array(draft.linkCountX) { x ->
		Array(draft.linkCountY) { y ->
			draft.getLink(x, y, 0).run { CPURectLink(RectLinkId(x, y, 0), strength, extra) }
		}
	}
	private val _vLinks = Array(draft.linkCountX) { x ->
		Array(draft.linkCountY) { y ->
			draft.getLink(x, y, 1).run { CPURectLink(RectLinkId(x, y, 1), strength, extra) }
		}
	}
	
	override val nodes = _nodes.asSequence().flatMap { it.asSequence() }.asIterable()
	override val links = (_hLinks.asSequence().flatMap { it.asSequence() } + _vLinks.asSequence().flatMap { it.asSequence() }).asIterable()
	
	override fun getNode(id: RectNodeId): CPURectNode = id.run { _nodes[x][y] }
	override fun getLink(id: RectLinkId): CPURectLink = id.run { if (id.h == 0) _hLinks[x][y] else _vLinks[x][y] }
	
}