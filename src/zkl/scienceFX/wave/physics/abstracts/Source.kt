package zkl.scienceFX.wave.physics.abstracts

import zkl.scienceFX.wave.physics.abstracts.Source.Type


interface Source {
	enum class Type { FORCE, POSITION }
	
	val nodeId: Int
	
	val type: Type
	fun getValue(time: Float): Float
	
	val startTime: Float
	val endTime: Float
	
}

val Source.span get() = endTime - startTime


class SinSource(
	override val nodeId: Int,
	override val startTime: Float,
	val period: Float,
	val initialPhase: Float = 0.0f,
	repeat: Float,
	override val type: Type,
	val scale: Float
) : Source {
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = (scale * Math.sin(time / period * 2.0 * Math.PI + initialPhase)).toFloat()
}

class SquareSource(
	override val nodeId: Int,
	override val startTime: Float,
	val period: Float,
	repeat: Float,
	val scale: Float
) : Source {
	override val type: Type = Type.POSITION
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = if (time % period < period / 2.0) scale else -scale
}

