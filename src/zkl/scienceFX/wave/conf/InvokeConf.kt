package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.SinSource
import zkl.scienceFX.wave.physics.abstracts.Source
import zkl.scienceFX.wave.physics.abstracts.SquareSource
import zkl.scienceFX.wave.physics.abstracts.World
import kotlin.math.PI


class SinInvokeConf : (World) -> Unit {
	
	var targetUnitId: Int = 0
	var delay: Float = 0.0f
	var type: Source.Type = Source.Type.FORCE
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	var initialPhase: Float = 0.0f
	
	override fun invoke(world: World) {
		SinSource(
			nodeId = targetUnitId,
			startTime = world.time + delay,
			period = period,
			initialPhase = initialPhase,
			repeat = repeat,
			type = type,
			scale = scale
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


class SquareInvokeConf : (World) -> Unit {
	
	var targetUnitId: Int = 0
	var delay: Float = 0.0f
	var scale: Float = 10.0f
	var period: Float = 40.0f
	var repeat: Float = 1.0f
	
	override fun invoke(world: World) {
		SquareSource(
			nodeId = targetUnitId,
			startTime = world.time + delay,
			period = period,
			repeat = repeat,
			scale = scale
		).let { world.invokers.add(it) }
	}
	
}

fun PhysicsConf.squareInvoke(body: SquareInvokeConf.() -> Unit) {
	this.onInvoke.add(SquareInvokeConf().also { body(it) })
}
