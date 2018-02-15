package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.WaveWorld
import zkl.scienceFX.wave.physics.abstracts.WaveWorldDraft

fun Conf.physics(body: PhysicsConf.() -> Unit) {
	this.physics = PhysicsConf().also { body.invoke(it) }
}

open class PhysicsConf {
	
	var timeUnit: Float = 0.2f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	lateinit var waveWorldCreator: WaveWorldCreator
	
	lateinit var waveWorldDrafter: WaveWorldDrafter
	
	val onInvoke = ArrayList<WaveWorld.() -> Unit>()
	
}

typealias WaveWorldCreator = (WaveWorldDraft) -> WaveWorld

typealias WaveWorldDrafter = () -> WaveWorldDraft


fun PhysicsConf.onInvoke(body: WaveWorld.() -> Unit) {
	this.onInvoke.add(body)
}

