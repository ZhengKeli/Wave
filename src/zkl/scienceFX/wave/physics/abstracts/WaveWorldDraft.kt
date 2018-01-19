package zkl.scienceFX.wave.physics.abstracts

interface WaveWorldDraft {
	val units: List<WaveUnitDraft>
	val links: List<WaveLinkDraft>
	val extra: Any?
}

interface WaveUnitDraft {
	val id: Int
	val offset: Float
	val velocity: Float
	val mass: Float
	val damping: Float
	val extra: Any?
}

interface WaveLinkDraft {
	val unitId1: Int
	val unitId2: Int
	val strength: Float
	val extra: Any?
}
