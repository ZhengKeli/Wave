package zkl.scienceFX.wave.physics.abstracts

interface WorldDraft {
	val nodes: List<NodeDraft>
	val links: List<LinkDraft>
	val extra: Any?
}

interface NodeDraft {
	val id: Int
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface LinkDraft {
	val unitId1: Int
	val unitId2: Int
	val strength: Float
	val extra: Any?
}
