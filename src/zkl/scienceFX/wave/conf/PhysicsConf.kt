package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.World
import zkl.scienceFX.wave.physics.abstracts.WorldDraft

fun Conf.physics(body: PhysicsConf.() -> Unit) {
	this.physics = PhysicsConf().also { body.invoke(it) }
}

open class PhysicsConf {
	
	var timeUnit: Float = 0.2f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	lateinit var waveWorldCreator: WaveWorldCreator
	
	lateinit var waveWorldDrafter: WaveWorldDrafter
	
	val onInvoke = ArrayList<World.() -> Unit>()
	
}

typealias WaveWorldCreator = (WorldDraft) -> World

typealias WaveWorldDrafter = () -> WorldDraft


fun PhysicsConf.onInvoke(body: World.() -> Unit) {
	this.onInvoke.add(body)
}

