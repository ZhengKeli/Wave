package zkl.science.wave.conf.physics

import zkl.science.wave.world.generic.*


class GenericPhysicsConf : PhysicsConf<Int, Int>(), GenericWorldDraft {
	
	private val _nodes = ArrayList<GenericNodeDraft>()
	private val _links = ArrayList<GenericLinkDraft>()
	
	override val nodes: List<GenericNodeDraft> get() = _nodes
	override val links: List<GenericLinkDraft> get() = _links
	override val extra: Any? = null
	
	fun node(body: NodeConf.() -> Unit): Int {
		val conf = defaultNode.copy().apply(body)
		_nodes.add(object : GenericNodeDraft {
			override val offset: Float = conf.offset
			override val velocity: Float = conf.velocity
			override val mass: Float = conf.mass
			override val damping: Float = conf.damping
			override val extra: Any? = conf.extra
		})
		return _nodes.lastIndex
	}
	
	fun link(nodeId1: Int, nodeId2: Int, body: LinkConf.() -> Unit): Int {
		val conf = defaultLink.copy().apply(body)
		_links.add(object : GenericLinkDraft {
			override val nodeId1: Int = nodeId1
			override val nodeId2: Int = nodeId2
			override val strength: Float = conf.strength
			override val extra: Any? = conf.extra
		})
		return _nodes.lastIndex
	}
	
}

fun GenericPhysicsConf.cpuWorld() {
	world = { CPUGenericWorld(this) }
}

fun GenericPhysicsConf.gpuWorld() {
	world = { GPUGenericWorld(this) }
}
