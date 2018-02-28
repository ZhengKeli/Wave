package zkl.science.wave.conf.physics

import zkl.science.wave.world.SinSource
import zkl.science.wave.world.SourceType
import zkl.science.wave.world.SquareSource
import zkl.science.wave.world.World
import kotlin.math.PI


fun <N, L> PhysicsConf<N, L>.customInteractor(body: World<N, L>.() -> Unit) {
	interactors.add(body)
}


fun <N : Any, L> PhysicsConf<N, L>.sinSourceInteractor(body: SinSourceConf<N>.() -> Unit) {
	interactors.add(SinSourceConf<N>().apply(body))
}

fun <N : Any, L> PhysicsConf<N, L>.cosSourceInteractor(body: SinSourceConf<N>.() -> Unit) {
	interactors.add(SinSourceConf<N>().apply(body).apply { initialPhase += (PI / 2.0).toFloat() })
}

class SinSourceConf<N : Any> : (World<N, *>) -> Unit {
	
	lateinit var nodeId: N
	var delay: Float = 0.0f
	var type: SourceType = SourceType.FORCE
	var amplitude: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	var initialPhase: Float = 0.0f
	
	override fun invoke(world: World<N, *>) {
		val startTime = world.time + delay
		world.sources += SinSource(nodeId, startTime, type, amplitude, period, repeat, initialPhase)
	}
	
}


fun <N : Any, L> PhysicsConf<N, L>.squareSourceInteractor(body: SquareSourceConf<N>.() -> Unit) {
	interactors.add(SquareSourceConf<N>().apply(body))
}

class SquareSourceConf<N : Any> : (World<N, *>) -> Unit {
	
	lateinit var nodeId: N
	var delay: Float = 0.0f
	var amplitude: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	
	override fun invoke(world: World<N, *>) {
		val startTime = world.time + delay
		world.sources += SquareSource(nodeId, amplitude, startTime, period, repeat)
	}
	
}
