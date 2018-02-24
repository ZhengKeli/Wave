package zkl.science.wave.physics.rect

import zkl.science.wave.physics.AbstractWorld
import zkl.science.wave.physics.Link
import zkl.science.wave.physics.Node


class CPURectNode(
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : Node

class CPURectLink(
	private val id: RectLinkId,
	override var strength: Float,
	override var extra: Any?
) : Link<RectNodeId> {
	override val unitId1: RectNodeId get() = id.run { RectNodeId(x, y) }
	override val unitId2: RectNodeId get() = id.run { if (h == 0) RectNodeId(x + 1, y) else RectNodeId(x, y + 1) }
}

class CPURectWorld(draft: RectWorldDraft) : RectWorld, AbstractWorld<RectNodeId, RectLinkId>() {
	
	override val width: Int = draft.width
	override val height: Int = draft.height
	
	private val _nodes = Array(draft.nodeCountX) { x ->
		Array(draft.nodeCountY) { y ->
			draft.getNode(x, y).run { CPURectNode(offset, velocity, mass, damping, extra) }
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
	
	override fun getNode(id: RectNodeId): Node = id.run { _nodes[x][y] }
	override fun getLink(id: RectLinkId): Link<RectNodeId> = id.run { if (id.h == 0) _hLinks[x][y] else _vLinks[x][y] }
	
}