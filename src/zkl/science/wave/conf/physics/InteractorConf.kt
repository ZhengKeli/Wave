package zkl.science.wave.conf.physics

import zkl.science.wave.world.SinSource
import zkl.science.wave.world.SourceType
import zkl.science.wave.world.SquareSource
import zkl.science.wave.world.World
import kotlin.math.PI


fun <W : World<*, *>> PhysicsConf<W>.customInteractor(body: W.() -> Unit) {
	interactors.add(body)
}


fun <N : Any> PhysicsConf<World<N, *>>.sinSourceInteractor(body: SinSourceConf<N>.() -> Unit) {
	interactors += SinSourceConf<N>().apply(body)
}

fun <N : Any> PhysicsConf<World<N, *>>.cosSourceInteractor(body: SinSourceConf<N>.() -> Unit) {
	interactors += SinSourceConf<N>().apply(body).apply { initialPhase += (PI / 2.0).toFloat() }
}

class SinSourceConf<N : Any> : (World<N, *>) -> Unit {
	
	lateinit var nodeId: N
	var delay: Float = 0.0f
	var type: SourceType = SourceType.FORCE
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	var initialPhase: Float = 0.0f
	
	override fun invoke(world: World<N, *>) {
		val startTime = world.time + delay
		world.sources += SinSource(nodeId, startTime, type, scale, period, repeat, initialPhase)
	}
	
}


fun <N : Any> PhysicsConf<World<N, *>>.squareSourceInteractor(body: SquareSourceConf<N>.() -> Unit) {
	interactors += SquareSourceConf<N>().apply(body)
}

class SquareSourceConf<N : Any> : (World<N, *>) -> Unit {
	
	lateinit var nodeId: N
	var delay: Float = 0.0f
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	
	override fun invoke(world: World<N, *>) {
		val startTime = world.time + delay
		world.sources += SquareSource(nodeId, scale, startTime, period, repeat)
	}
	
}