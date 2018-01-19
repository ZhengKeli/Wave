package zkl.scienceFX.wave.physics.abstracts


enum class InvokeType { force, position }
interface WaveInvoker {
	val type: InvokeType
	val invokedUnitId: Int
	val startTime: Float
	val span: Float
	fun getValue(time: Float): Float
}


class SinWaveInvoker(
	override val startTime: Float,
	override val type: InvokeType,
	override val invokedUnitId: Int,
	val scale: Float,
	val period: Float,
	repeat: Float,
	val initialPhase: Float = 0.0f
) : WaveInvoker {
	override val span: Float = period * repeat
	override fun getValue(time: Float): Float = (scale * Math.sin(time / period * 2.0 * Math.PI + initialPhase)).toFloat()
}

class SquareWaveInvoker(
	override val startTime: Float,
	override val invokedUnitId: Int,
	val scale: Float,
	val period: Float,
	repeat: Float
) : WaveInvoker {
	override val type: InvokeType = InvokeType.position
	override val span: Float = period * repeat
	override fun getValue(time: Float): Float = if (time % period < period / 2.0) scale else -scale
}


