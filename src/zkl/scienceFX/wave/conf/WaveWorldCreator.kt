package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.aparapi.AparapiWaveWorld
import zkl.scienceFX.wave.physics.simple.SimpleWaveWorld

fun PhysicsConf.simpleWaveWorld() {
	waveWorldCreator = { SimpleWaveWorld(it) }
}

fun PhysicsConf.aparapiWaveWorld() {
	waveWorldCreator = { AparapiWaveWorld(it) }
}
