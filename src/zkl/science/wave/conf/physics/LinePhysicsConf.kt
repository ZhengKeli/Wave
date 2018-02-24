package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.line.*


fun Conf.linePhysics(body: LinePhysicsConf.() -> Unit) {
	this.physicsConf = LinePhysicsConf().apply(body)
}

class LinePhysicsConf : PhysicsConf<LineWorld>() {
	
	var length: Int = 0
	
	var infantNodeDraft = InstantNodeDraft(1.0f, 0.0f, 0.0f, 0.0f, null)
	var infantLinkDraft = InstantLinkDraft(0.3f, null)
	var nodeDrafter: InstantNodeDraft.(x: Int) -> Unit = {}
	var linkDrafter: InstantLinkDraft.(x: Int) -> Unit = {}
	var extra: Any? = null
	
	var worldDrafter: () -> LineWorldDraft = {
		object : LineWorldDraft {
			override val length: Int = this@LinePhysicsConf.length
			
			override fun getNode(x: Int): LineNodeDraft {
				return this@LinePhysicsConf.infantNodeDraft.copy().also { nodeDrafter(it, x) }
			}
			
			override fun getLink(x: Int): LineLinkDraft {
				return this@LinePhysicsConf.infantLinkDraft.copy().also { linkDrafter(it, x) }
			}
			
			override val extra: Any? = this@LinePhysicsConf.extra
		}
	}
	
}


fun LinePhysicsConf.nodeDrafter(body: InstantNodeDraft.(x: Int) -> Unit) {
	nodeDrafter = body
}

fun LinePhysicsConf.linkDrafter(body: InstantLinkDraft.(x: Int) -> Unit) {
	linkDrafter = body
}


fun LinePhysicsConf.cpuWorld() {
	world = { CPULineWorld(worldDrafter()) }
}

