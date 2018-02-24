package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.World


fun <W : World<*, *>> Conf.physics(body: PhysicsConf<W>.() -> Unit) {
	this.physicsConf = PhysicsConf<W>().apply(body)
}

open class PhysicsConf<W : World<*, *>> {
	
	var timeUnit: Float = 0.2f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	open lateinit var world: () -> W
	
	open val interactors = ArrayList<W.() -> Unit>()
	
}

