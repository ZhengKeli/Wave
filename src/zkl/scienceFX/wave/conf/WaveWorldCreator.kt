package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.aparapi.AparapiWorld
import zkl.scienceFX.wave.physics.simple.SimpleWorld

fun PhysicsConf.simpleWaveWorld() {
	waveWorldCreator = { SimpleWorld(it) }
}

fun PhysicsConf.aparapiWaveWorld() {
	waveWorldCreator = { AparapiWorld(it) }
}
