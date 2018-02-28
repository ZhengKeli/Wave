package zkl.science.wave.world.generic

import zkl.science.wave.world.Source
import java.util.*

class GPUGenericWorld(draft: GenericWorldDraft) : GenericWorld {
	
	override val nodes: List<GenericNode> = object : AbstractList<GenericNode>() {
		override val size: Int get() = kernel.nodesCount
		override fun get(index: Int): GenericNode = kernel.run {
			object : GenericNode {
				override val nodeId: Int = index
				
				override var offset: Float
					get() = if (computeCount % 2 == 0) nodesOffset_s0[nodeId] else nodesOffset_s1[nodeId]
					set(value) {
						if (computeCount % 2 == 0) nodesOffset_s0[nodeId] = value else nodesOffset_s1[nodeId] = value
					}
				override var velocity: Float
					get() = nodesVelocity[nodeId]
					set(value) {
						nodesVelocity[nodeId] = value
					}
				override var mass: Float
					get() = nodesMass[nodeId]
					set(value) {
						nodesMass[nodeId] = value
					}
				override var damping: Float
					get() = nodesDamping[nodeId]
					set(value) {
						nodesDamping[nodeId] = value
					}
				override var extra: Any?
					get() = nodesExtra[nodeId]
					set(value) {
						nodesExtra[nodeId] = value
					}
			}
		}
	}
	override val links: List<GenericLink> = object : AbstractList<GenericLink>() {
		override val size: Int get() = kernel.linksCount
		override fun get(index: Int) = kernel.run {
			object : GenericLink {
				override val linkId: Int = index
				
				override val nodeId1: Int
					get() = kernel.run { impactsFromNodeId[linksImpactId2[linkId]] }
				override val nodeId2: Int
					get() = kernel.run { impactsFromNodeId[linksImpactId1[linkId]] }
				
				override var strength: Float
					get() = impactsStrength[linksImpactId1[linkId]]
					set(value) {
						impactsStrength[linksImpactId1[linkId]] = value
						impactsStrength[linksImpactId2[linkId]] = value
					}
				override var extra: Any?
					get() = linksExtra[linkId]
					set(value) {
						linksExtra[linkId] = value
					}
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
	override val time: Float get() = kernel.run { if (!isExecuting) time else time + currentPass * timeUnit }
	
	@Synchronized
	override fun process(timeUnit: Float, repeat: Int) {
		kernel.process(repeat, timeUnit, sources)
		synchronized(sources) { sources.removeIf { time > it.startTime + it.span } }
	}
	
}

