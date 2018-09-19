package zkl.science.wave.world.line

import org.nd4j.linalg.cpu.nativecpu.NDArray
import zkl.science.wave.world.Source

class ND4JLineWorld(draft: LineWorldDraft) : LineWorld {
	
	val positions = NDArray(intArrayOf(draft.length))
	val velocities = NDArray(intArrayOf(draft.length))
	
	init {
		positions.get()
	}
	
	override val length: Int
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	
	override fun getNode(x: Int): LineNode {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun getLink(x: Int): LineLink {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override val sources: MutableList<Source<LineNodeId>>
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	override var extra: Any?
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
		set(value) {}
	override val time: Float
		get() = TODO("not implemented") //To change initializer of created properties use File | Settings | File Templates.
	
	override fun process(timeUnit: Float, repeat: Int) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	
}