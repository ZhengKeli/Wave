package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.aparapi.WaveWorldAparapi
import zkl.scienceFX.wave.physics.simple.WaveWorldSimple

fun PhysicsConf.simpleWaveWorld() {
	waveWorldCreator = { WaveWorldSimple(it) }
}

fun PhysicsConf.aparapiWaveWorld() {
	waveWorldCreator = { WaveWorldAparapi(it) }
}
