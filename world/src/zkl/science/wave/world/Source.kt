package zkl.science.wave.world


enum class SourceType { FORCE, POSITION }

interface Source<out NodeId> {
	val nodeId: NodeId
	
	val type: SourceType
	fun getValue(time: Float): Float
	
	val startTime: Float
	val endTime: Float
	val span: Float get() = endTime - startTime
}

class SinSource<out NodeId>(
	override val nodeId: NodeId,
	override val startTime: Float,
	override val type: SourceType,
	val scale: Float,
	val period: Float,
	repeat: Float,
	val initialPhase: Float = 0.0f
) : Source<NodeId> {
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = (scale * Math.sin(time / period * 2.0 * Math.PI + initialPhase)).toFloat()
}

class SquareSource<out NodeId>(
	override val nodeId: NodeId,
	val scale: Float,
	override val startTime: Float,
	val period: Float,
	repeat: Float
) : Source<NodeId> {
	override val type: SourceType = SourceType.POSITION
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = if (time % period < period / 2.0) scale else -scale
}
