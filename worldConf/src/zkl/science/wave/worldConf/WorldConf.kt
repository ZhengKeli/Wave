package zkl.science.wave.worldConf

import zkl.science.wave.world.World


open class WorldConf<W : World<*, *>> {
	
	var timeUnit: Float = 0.2f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	open lateinit var creator: () -> W
	
	open val interactors = ArrayList<W.() -> Unit>()
	
}

fun <W : World<*, *>> WorldConf<W>.interactor(body: W.() -> Unit) {
	interactors.add(body)
}
