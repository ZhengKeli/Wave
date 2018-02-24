package zkl.science.wave.conf.physics

import zkl.science.wave.conf.Conf
import zkl.science.wave.world.line.*


fun Conf.linePhysics(body: LinePhysicsConf.() -> Unit) {
	this.physicsConf = LinePhysicsConf().apply(body)
}

open class LinePhysicsConf : PhysicsConf<LineWorld>() {
	
	var length: Int = 0
	
	var infantNodeDraft = InstantNodeDraft(1.0f, 0.0f, 0.0f, 0.0f, null)
	var infantLinkDraft = InstantLinkDraft(0.3f, null)
	var nodeDrafters: ArrayList<InstantNodeDraft.(x: Int) -> Unit> = ArrayList()
	var linkDrafters: ArrayList<InstantLinkDraft.(x: Int) -> Unit> = ArrayList()
	var extra: Any? = null
	
	var worldDrafter: () -> LineWorldDraft = {
		object : LineWorldDraft {
			override val length: Int = this@LinePhysicsConf.length
			
			override fun getNode(x: Int): LineNodeDraft {
				return infantNodeDraft.copy().apply { nodeDrafters.forEach { it(x) } }
			}
			
			override fun getLink(x: Int): LineLinkDraft {
				return infantLinkDraft.copy().apply { linkDrafters.forEach { it(x) } }
			}
			
			override val extra: Any? = this@LinePhysicsConf.extra
		}
	}
	
}


fun LinePhysicsConf.nodeDrafter(body: InstantNodeDraft.(x: Int) -> Unit) {
	nodeDrafters.add(body)
}

fun LinePhysicsConf.linkDrafter(body: InstantLinkDraft.(x: Int) -> Unit) {
	linkDrafters.add(body)
}


fun LinePhysicsConf.cpuWorld() {
	world = { CPULineWorld(worldDrafter()) }
}

