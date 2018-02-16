package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.SinWaveInvoker
import zkl.scienceFX.wave.physics.abstracts.SquareWaveInvoker
import zkl.scienceFX.wave.physics.abstracts.WaveInvoker
import zkl.scienceFX.wave.physics.abstracts.WaveWorld
import kotlin.math.PI


class SinInvokeConf : (WaveWorld) -> Unit {
	
	var targetUnitId: Int = 0
	var delay: Float = 0.0f
	var type: WaveInvoker.Type = WaveInvoker.Type.FORCE
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	var initialPhase: Float = 0.0f
	
	override fun invoke(world: WaveWorld) {
		SinWaveInvoker(
			invokedUnitId = targetUnitId,
			startTime = world.time + delay,
			type = type,
			scale = scale,
			period = period,
			repeat = repeat,
			initialPhase = initialPhase
		).let { world.invokers.add(it) }
	}
	
}

fun PhysicsConf.sinInvoke(body: SinInvokeConf.() -> Unit) {
	this.onInvoke.add(SinInvokeConf().also { body(it) })
}

fun PhysicsConf.cosInvoke(body: SinInvokeConf.() -> Unit) {
	sinInvoke {
		body.invoke(this)
		initialPhase += (PI / 2.0).toFloat()
	}
}


class SquareInvokeConf : (WaveWorld) -> Unit {
	
	var targetUnitId: Int = 0
	var delay: Float = 0.0f
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	
	override fun invoke(world: WaveWorld) {
		SquareWaveInvoker(
			invokedUnitId = targetUnitId,
			startTime = world.time + delay,
			scale = scale,
			period = period,
			repeat = repeat
		).let { world.invokers.add(it) }
	}
	
}

fun PhysicsConf.squareInvoke(body: SquareInvokeConf.() -> Unit) {
	this.onInvoke.add(SquareInvokeConf().also { body(it) })
}
