package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.LinkProperties
import zkl.science.wave.world.NodeProperties
import zkl.science.wave.world.World


data class NodeConf(
	override var mass: Float,
	override var damping: Float = 0.0f,
	override var offset: Float = 0.0f,
	override var velocity: Float = 0.0f,
	override var extra: Any? = null
) : NodeProperties

data class LinkConf(
	override var strength: Float,
	override var extra: Any? = null
) : LinkProperties

fun <N, L> Conf.physics(body: PhysicsConf<N, L>.() -> Unit) {
	this.physicsConf = PhysicsConf<N, L>().apply(body)
}

open class PhysicsConf<N, L> {
	
	var timeUnit: Float = 0.1f
	var timeOffset: Float = 0.0f
	var processCount: Int = 5
	
	var defaultNode: NodeConf = NodeConf(1.0f, 0.0f, 0.0f, 0.0f, null)
	var nodeDrafters: MutableList<NodeConf.(N) -> Unit> = ArrayList()
	
	var defaultLink: LinkConf = LinkConf(0.1f, null)
	var linkDrafters: MutableList<LinkConf.(L) -> Unit> = ArrayList()
	
	open lateinit var world: () -> World<N, L>
	
	open val interactors: MutableList<World<N, L>.() -> Unit> = ArrayList()
	
	@Suppress("UNCHECKED_CAST")
	fun interact(world: World<*, *>) {
		interactors.forEach { it.invoke(world as World<N, L>) }
	}
	
}

fun <N> PhysicsConf<N, *>.nodeDrafter(body: NodeConf.(N) -> Unit) {
	nodeDrafters.add(body)
}

fun <L> PhysicsConf<*, L>.linkDrafter(body: LinkConf.(L) -> Unit) {
	linkDrafters.add(body)
}

