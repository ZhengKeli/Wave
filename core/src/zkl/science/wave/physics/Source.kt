package zkl.science.wave.physics

import zkl.science.wave.physics.Invoking.Type


interface Invoking {
	
	enum class Type { FORCE, POSITION }
	
	val type: Type
	fun getValue(time: Float): Float
	
	val startTime: Float
	val endTime: Float
}

class SinInvoking(
	override val startTime: Float,
	val period: Float,
	val initialPhase: Float = 0.0f,
	repeat: Float,
	override val type: Type,
	val scale: Float
) : Invoking {
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = (scale * Math.sin(time / period * 2.0 * Math.PI + initialPhase)).toFloat()
}

class SquareInvoking(
	override val startTime: Float,
	val period: Float,
	repeat: Float,
	val scale: Float
) : Invoking {
	override val type: Type = Type.POSITION
	override val endTime: Float = startTime + period * repeat
	override fun getValue(time: Float): Float = if (time % period < period / 2.0) scale else -scale
}

val Invoking.span get() = endTime - startTime

class Source<out NodeId>(val nodeId: NodeId, val invoking: Invoking) : Invoking by invoking
