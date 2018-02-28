package zkl.science.wave.world.line

import zkl.science.wave.world.AbstractWorld

class CPULineNode(
	val x: Int,
	override var offset: Float,
	override var velocity: Float,
	override var mass: Float,
	override var damping: Float,
	override var extra: Any?
) : LineNode {
	override val nodeId: LineNodeId get() = LineNodeId(x)
}

class CPULineLink(
	val x: Int,
	override var strength: Float,
	override var extra: Any?
) : LineLink {
	override val linkId: LineLinkId get() = LineLinkId(x)
	override val nodeId1: LineNodeId get() = LineNodeId(x)
	override val nodeId2: LineNodeId get() = LineNodeId(x + 1)
}

class CPULineWorld(draft: LineWorldDraft) : LineWorld, AbstractWorld<LineNodeId, LineLinkId>() {
	
	override val length get() = links.size
	override val nodes = draft.run {
		Array(nodeCount) { x ->
			getNode(x).run { CPULineNode(x, offset, velocity, mass, damping, extra) }
		}.toMutableList()
	}
	override val links = draft.run {
		Array(linkCount) { x ->
			getLink(x).run { CPULineLink(x, strength, extra) }
		}.toMutableList()
	}
	
	override fun getNode(x: Int): CPULineNode = nodes[x]
	override fun getLink(x: Int): CPULineLink = links[x]
	
}
