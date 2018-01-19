package zkl.scienceFX.wave.conf


fun conf(body: Conf.() -> Unit) = Conf().also { it.body() }

class Conf {
	
	lateinit var physics: PhysicsConf
	
	lateinit var visualConf: VisualConf
	
	var exportConf: ExportConf? = null
	
}
