package zkl.science.wave.world.generic

import zkl.science.wave.world.World

interface GenericWorldDraft {
	val nodes: List<GenericNodeDraft>
	val links: List<GenericLinkDraft>
	val extra: Any?
}

interface GenericNodeDraft {
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface GenericLinkDraft {
	val nodeId1: Int
	val nodeId2: Int
	val strength: Float
	val extra: Any?
}

interface GenericWorld : World<Int, Int> {
	val nodeCount: Int
	val linkCount: Int
}
