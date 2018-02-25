package zkl.science.wave.conf

import zkl.science.wave.conf.export.ExportConf
import zkl.science.wave.conf.physics.PhysicsConf
import zkl.science.wave.conf.visual.VisualConf


fun conf(body: Conf.() -> Unit) = Conf().also { it.body() }

class Conf {
	
	lateinit var physicsConf: PhysicsConf<*>
	
	lateinit var visualConf: VisualConf
	
	var exportConf: ExportConf? = null
	
}
