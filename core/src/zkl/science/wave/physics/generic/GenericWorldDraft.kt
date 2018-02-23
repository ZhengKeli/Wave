package zkl.science.wave.physics.generic

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
	val unitId1: Int
	val unitId2: Int
	val strength: Float
	val extra: Any?
}
