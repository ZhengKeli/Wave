package zkl.science.wave.conf

import zkl.science.wave.conf.export.ExportConf
import zkl.science.wave.conf.physics.PhysicsConf
import zkl.science.wave.conf.visual.VisualConf


fun conf(body: Conf.() -> Unit): Conf = Conf().also { it.body() }

fun lazyConf(body: Conf.() -> Unit): Lazy<Conf> = lazy { conf(body) }

class Conf {
	
	lateinit var physicsConf: PhysicsConf<*, *>
	
	var visualConf: VisualConf? = null
	
	var exportConf: ExportConf? = null
	
}
