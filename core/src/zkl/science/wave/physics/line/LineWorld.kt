package zkl.science.wave.physics.line

import zkl.science.wave.physics.AbstractWorld
import zkl.science.wave.physics.Link
import zkl.science.wave.physics.generic.GenericNode

typealias LineNode = GenericNode

class LineLink(
	val x: Int,
	override var strength: Float,
	override var extra: Any?
) : Link<Int> {
	override val unitId1: Int get() = x
	override val unitId2: Int get() = x + 1
}

class LineWorld(draft: LineWorldDraft) : AbstractWorld<Int, Int>() {
	
	private val _nodes = draft.run {
		Array(noteCount) {
			getNode(it).run { LineNode(offset, velocity, mass, damping, extra) }
		}.toMutableList()
	}
	private val _links = draft.run {
		Array(linkCount) {
			getLink(it).run { LineLink(it, strength, extra) }
		}.toMutableList()
	}
	
	public override fun getAllNodes() = _nodes
	public override fun getAllLinks() = _links
	
	override fun getNode(id: Int): LineNode = getAllNodes()[id]
	override fun getLink(id: Int): LineLink = getAllLinks()[id]
	
}