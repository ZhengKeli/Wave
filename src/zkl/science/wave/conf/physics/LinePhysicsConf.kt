package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.LinkProperties
import zkl.science.wave.world.NodeProperties
import zkl.science.wave.world.line.CPULineWorld
import zkl.science.wave.world.line.LineWorldDraft


fun Conf.linePhysics(body: LinePhysicsConf.() -> Unit) {
	this.physicsConf = LinePhysicsConf().apply(body)
}

open class LinePhysicsConf : PhysicsConf<Int, Int>(), LineWorldDraft {
	
	override var length: Int = 0
	
	override fun getNode(x: Int): NodeProperties {
		return defaultNode.copy().apply { nodeDrafters.forEach { it(x) } }
	}
	
	override fun getLink(x: Int): LinkProperties {
		return defaultLink.copy().apply { linkDrafters.forEach { it(x) } }
	}
	
	override var extra: Any? = null
	
}

fun LinePhysicsConf.cpuWorld() {
	world = { CPULineWorld(this) }
}

