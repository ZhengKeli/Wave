package zkl.science.wave.world.generic

import zkl.science.wave.world.Link
import zkl.science.wave.world.Node
import zkl.science.wave.world.Source
import java.util.*

class GPUGenericWorld(draft: GenericWorldDraft) : GenericWorld {
	
	override val nodeCount: Int get() = kernel.nodesCount
	override fun getNode(id: Int): Node<Int> = kernel.run {
		object : Node<Int> {
			override val nodeId: Int = id
			
			override var offset: Float
				get() = if (computeCount % 2 == 0) nodesOffset_s0[id] else nodesOffset_s1[id]
				set(value) {
					if (computeCount % 2 == 0) nodesOffset_s0[id] = value else nodesOffset_s1[id] = value
				}
			override var velocity: Float
				get() = nodesVelocity[id]
				set(value) {
					nodesVelocity[id] = value
				}
			override var mass: Float
				get() = nodesMass[id]
				set(value) {
					nodesMass[id] = value
				}
			override var damping: Float
				get() = nodesDamping[id]
				set(value) {
					nodesDamping[id] = value
				}
			override var extra: Any?
				get() = nodesExtra[id]
				set(value) {
					nodesExtra[id] = value
				}
		}
	}
	
	override val linkCount: Int get() = kernel.linksCount
	override fun getLink(id: Int): Link<Int, Int> = kernel.run {
		object : Link<Int, Int> {
			override val linkId: Int = id
			
			override val nodeId1: Int
				get() = kernel.run { impactsFromNodeId[linksImpactId2[id]] }
			override val nodeId2: Int
				get() = kernel.run { impactsFromNodeId[linksImpactId1[id]] }
			override var strength: Float
				get() = impactsStrength[linksImpactId1[id]]
				set(value) {
					impactsStrength[linksImpactId1[id]] = value
					impactsStrength[linksImpactId2[id]] = value
				}
			override var extra: Any?
				get() = linksExtra[id]
				set(value) {
					linksExtra[id] = value
				}
		}
	}
	
	override val sources: MutableList<Source<Int>> = LinkedList()
	
	override var extra: Any? = draft.extra
	
	val kernel: GPUGenericKernel = kotlin.run {
		println("launching OpenCL ...")
		val kernel = GPUGenericKernel(draft)
		
		println("warming up OpenCL ...")
		kernel.process(1, 0f, emptyList())
		
		return@run kernel
	}
	
	override val time: Float
		get() {
			kernel.run {
				return if (!isExecuting) time else time + currentPass * timeUnit
			}
		}
	
	@Synchronized
	override fun process(timeUnit: Float, repeat: Int) {
		kernel.process(repeat, timeUnit, sources)
		synchronized(sources) { sources.removeIf { time > it.startTime + it.span } }
	}
	
}

