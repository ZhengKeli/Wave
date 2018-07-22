package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.LinkProperties
import zkl.science.wave.world.NodeProperties
import zkl.science.wave.world.rect.CPURectWorld
import zkl.science.wave.world.rect.RectLinkId
import zkl.science.wave.world.rect.RectNodeId
import zkl.science.wave.world.rect.RectWorldDraft


fun Conf.rectPhysics(body: RectPhysicsConf.() -> Unit) {
	this.physicsConf = RectPhysicsConf().apply(body)
}

open class RectPhysicsConf : PhysicsConf<RectNodeId, RectLinkId>(), RectWorldDraft {
	
	override var width: Int = 0
	override var height: Int = 0
	
	override fun getNode(x: Int, y: Int): NodeProperties {
		return defaultNode.copy().apply { nodeDrafters.forEach { it(RectNodeId(x, y)) } }
	}
	
	override fun getLink(x: Int, y: Int, h: Int): LinkProperties {
		return defaultLink.copy().apply { linkDrafters.forEach { it(RectLinkId(x, y, h)) } }
	}
	
	override var extra: Any? = null
	
}


fun RectPhysicsConf.nodeDrafter(body: NodeConf.(RectNodeId) -> Unit) {
	nodeDrafters.add(body)
}

fun RectPhysicsConf.linkDrafter(body: LinkConf.(RectLinkId) -> Unit) {
	linkDrafters.add(body)
}


fun RectPhysicsConf.cpuWorld() {
	world = { CPURectWorld(this) }
}

fun RectPhysicsConf.aparapiWorld() {
	world = { CPURectWorld(this) }
	println("GPU for RectWorld is not supported yet! Drown down to CPU.")
	//todo "support GPU for RectWorld"
}
