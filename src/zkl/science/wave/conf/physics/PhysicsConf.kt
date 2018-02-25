package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.World


fun <N> Conf.physics(body: PhysicsConf<N>.() -> Unit) {
	this.physicsConf = PhysicsConf<N>().apply(body)
}

open class PhysicsConf<N> {
	
	var timeUnit: Float = 0.2f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	open lateinit var world: () -> World<N, *>
	
	open val interactors = ArrayList<World<N, *>.() -> Unit>()
	
}

