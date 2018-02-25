package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.rect.*


fun Conf.rectPhysics(body: RectPhysicsConf.() -> Unit) {
	this.physicsConf = RectPhysicsConf().apply(body)
}

open class RectPhysicsConf : PhysicsConf<RectNodeId>() {
	
	var width: Int = 0
	var height: Int = 0
	val nodeCountX: Int get() = width + 1
	val nodeCountY: Int get() = height + 1
	val linkCountX: Int get() = width
	val linkCountY: Int get() = height
	
	var defaultNode = InstantNodeDraft(1.0f, 0.0f, 0.0f, 0.0f, null)
	var defaultLink = InstantLinkDraft(0.1f, null)
	var nodeDrafters: ArrayList<InstantNodeDraft.(x: Int, y: Int) -> Unit> = ArrayList()
	var linkDrafters: ArrayList<InstantLinkDraft.(x: Int, y: Int, h: Int) -> Unit> = ArrayList()
	var extra: Any? = null
	
	var worldDrafter: () -> RectWorldDraft = {
		object : RectWorldDraft {
			override val width: Int = this@RectPhysicsConf.width
			override val height: Int = this@RectPhysicsConf.height
			
			override fun getNode(x: Int, y: Int): RectNodeDraft {
				return defaultNode.copy().apply { nodeDrafters.forEach { it(x, y) } }
			}
			
			override fun getLink(x: Int, y: Int, h: Int): RectLinkDraft {
				return defaultLink.copy().apply { linkDrafters.forEach { it(x, y, h) } }
			}
			
			override val extra: Any? = this@RectPhysicsConf.extra
			
		}
	}
	
}


fun RectPhysicsConf.nodeDrafter(body: InstantNodeDraft.(x: Int, y: Int) -> Unit) {
	nodeDrafters.add(body)
}

fun RectPhysicsConf.linkDrafter(body: InstantLinkDraft.(x: Int, y: Int, h: Int) -> Unit) {
	linkDrafters.add(body)
}


fun RectPhysicsConf.cpuWorld() {
	world = { CPURectWorld(worldDrafter()) }
}

fun RectPhysicsConf.gpuWorld() {
	world = { CPURectWorld(worldDrafter()) }
	println("GPU for RectWorld is not supported yet! Drown down to CPU.")
	//todo "support GPU for RectWorld"
}
