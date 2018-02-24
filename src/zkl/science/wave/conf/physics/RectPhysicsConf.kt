package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.rect.*


fun Conf.rectPhysics(body: RectPhysicsConf.() -> Unit) {
	this.physicsConf = RectPhysicsConf().apply(body)
}

class RectPhysicsConf : PhysicsConf<RectWorld>() {
	
	var width: Int = 0
	var height: Int = 0
	
	var infantNodeDraft = InstantNodeDraft(1.0f, 0.0f, 0.0f, 0.0f, null)
	var infantLinkDraft = InstantLinkDraft(0.3f, null)
	var nodeDrafter: InstantNodeDraft.(x: Int, y: Int) -> Unit = { _, _ -> }
	var linkDrafter: InstantLinkDraft.(x: Int, y: Int, h: Int) -> Unit = { _, _, _ -> }
	var extra: Any? = null
	
	var worldDrafter: () -> RectWorldDraft = {
		object : RectWorldDraft {
			override val width: Int = this@RectPhysicsConf.width
			override val height: Int = this@RectPhysicsConf.height
			
			override fun getNode(x: Int, y: Int): RectNodeDraft {
				return infantNodeDraft.copy().apply { nodeDrafter(x, y) }
			}
			
			override fun getLink(x: Int, y: Int, h: Int): RectLinkDraft {
				return infantLinkDraft.copy().apply { linkDrafter(x, y, h) }
			}
			
			override val extra: Any? = this@RectPhysicsConf.extra
			
		}
	}
	
}


fun RectPhysicsConf.nodeDrafter(body: InstantNodeDraft.(x: Int, y: Int) -> Unit) {
	nodeDrafter = body
}

fun RectPhysicsConf.linkDrafter(body: InstantLinkDraft.(x: Int, y: Int, h: Int) -> Unit) {
	linkDrafter = body
}


fun RectPhysicsConf.cpuWorld() {
	world = { CPURectWorld(worldDrafter()) }
}