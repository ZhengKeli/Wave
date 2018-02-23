package zkl.scienceFX.wave.physics.abstracts

import zkl.scienceFX.wave.physics.abstracts.Source.Type


interface Source {
	enum class Type { FORCE, POSITION }
	val type: Type
	val invokedUnitId: Int
	val startTime: Float
	val endTime: Float
	fun getValue(time: Float): Float
}

val Source.span get() = endTime - startTime


class SinSource(
	override val startTime: Float,
	override val type: Type,
	override val invokedUnitId: Int,
	val scale: Float,
	val period: Float,
	repeat: Float,
	val initialPhase: Float = 0.0f
) : Source {
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = (scale * Math.sin(time / period * 2.0 * Math.PI + initialPhase)).toFloat()
}

class SquareSource(
	override val startTime: Float,
	override val invokedUnitId: Int,
	val scale: Float,
	val period: Float,
	repeat: Float
) : Source {
	override val type: Type = Type.POSITION
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = if (time % period < period / 2.0) scale else -scale
}


