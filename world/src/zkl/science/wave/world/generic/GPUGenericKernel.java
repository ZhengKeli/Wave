package zkl.science.wave.world.generic;

import com.aparapi.Kernel;

import java.util.ArrayList;
import java.util.List;

import zkl.science.wave.world.Invoking;
import zkl.science.wave.world.SinInvoking;
import zkl.science.wave.world.Source;

public class GPUGenericKernel extends Kernel {
	/**
	 * 为了避免不同计算单元的 nodeOffset 互相影响（在 intel 的运算平台上会互相影响），
	 * 将 nodeOffset 分为两部分，交替地用于存储每次计算的源数据和计算结果，
	 * 用computeCount的奇偶来判断用哪个
	 */
	int computeCount = 0;
	
	int nodesCount;
	float[] nodesOffset_s0;
	float[] nodesOffset_s1;
	float[] nodesVelocity;
	float[] nodesMass;
	float[] nodesDamping;
	Object[] nodesExtra;
	
	int linksCount;
	Object[] linksExtra;
	
	int[] impactsFromNodeId;
	float[] impactsStrength;
	private int[] nodesImpactStartId;
	private int[] nodesImpactEndId;
	int[] linksImpactId1;
	int[] linksImpactId2;
	
	float time = 0.0f;
	float timeUnit;
	
	private int sinSourcesCount = 0;
	private int[] sinSourcesType = new int[1];
	private int[] sinSourcesInvokedNodeId = new int[1];
	private float[] sinSourcesStartTime = new float[1];
	private float[] sinSourcesEndTime = new float[1];
	private float[] sinSourcesScale = new float[1];
	private float[] sinSourcesPeriod = new float[1];
	private float[] sinSourcesInitialPhase = new float[1];
	private static final float PI = (float) Math.PI;
	private static final int INVOKER_TYPE_FORCE = 0;
	private static final int INVOKER_TYPE_POSITION = 1;
	
	private static int getInvokerTypeCode(Invoking.Type invokeType) {
		if (invokeType == Invoking.Type.FORCE) {
			return INVOKER_TYPE_FORCE;
		} else if (invokeType == Invoking.Type.POSITION) {
			return INVOKER_TYPE_POSITION;
		} else {
			return -1;
		}
	}
	
	public GPUGenericKernel(GenericWorldDraft worldDraft) {
		//排入node信息
		List<GenericNodeDraft> nodes = worldDraft.getNodes();
		nodesCount = nodes.size();
		nodesOffset_s0 = new float[nodesCount];
		nodesOffset_s1 = new float[nodesCount];
		nodesVelocity = new float[nodesCount];
		nodesMass = new float[nodesCount];
		nodesDamping = new float[nodesCount];
		nodesExtra = new Object[nodesCount];
		for (int nodeId = 0; nodeId < nodesCount; nodeId++) {
			GenericNodeDraft nodeDraft = nodes.get(nodeId);
			nodesOffset_s0[nodeId] = nodeDraft.getOffset();
			nodesVelocity[nodeId] = nodeDraft.getVelocity();
			nodesMass[nodeId] = nodeDraft.getMass();
			nodesDamping[nodeId] = nodeDraft.getDamping();
			nodesExtra[nodeId] = nodeDraft.getExtra();
		}
		
		//排入link信息
		List<GenericLinkDraft> links = worldDraft.getLinks();
		linksCount = links.size();
		linksExtra = new Object[linksCount];
		//构建 node-links 映射表
		ArrayList<ArrayList<Integer>> nodesLinksId = new ArrayList<>(nodesCount);
		for (int nodeId = 0; nodeId < nodesCount; nodeId++) {
			nodesLinksId.add(new ArrayList<>(4));
		}
		for (int linkId = 0; linkId < linksCount; linkId++) {
			GenericLinkDraft linkDraft = links.get(linkId);
			linksExtra[linkId] = linkDraft.getExtra();
			nodesLinksId.get(linkDraft.getNodeId1()).add(linkId);
			nodesLinksId.get(linkDraft.getNodeId2()).add(linkId);
		}
		
		//根据 node-links 映射表构建 node-impacts 映射
		impactsFromNodeId = new int[linksCount * 2];
		impactsStrength = new float[linksCount * 2];
		nodesImpactStartId = new int[nodesCount];
		nodesImpactEndId = new int[nodesCount];
		linksImpactId1 = new int[linksCount];
		linksImpactId2 = new int[linksCount];
		int impactId = 0;
		for (int nodeId = 0; nodeId < nodesCount; nodeId++) {
			nodesImpactStartId[nodeId] = impactId;
			for (int linkId : nodesLinksId.get(nodeId)) {
				GenericLinkDraft link = links.get(linkId);
				if (nodeId == link.getNodeId1()) {
					impactsFromNodeId[impactId] = link.getNodeId2();
					linksImpactId1[linkId] = impactId;
				} else if (nodeId == link.getNodeId2()) {
					impactsFromNodeId[impactId] = link.getNodeId1();
					linksImpactId2[linkId] = impactId;
				}
				impactsStrength[impactId] = link.getStrength();
				impactId++;
			}
			nodesImpactEndId[nodeId] = impactId;
		}
		
	}
	
	private boolean canNotAccelerateWarned = false;
	
	public void process(int count, float timeUnit, List<Source<Integer>> sources) {
		boolean invokerLegal = true;
		for (Source source : sources) {
			if (!(source.getInvoking() instanceof SinInvoking)) {
				invokerLegal = false;
				if (!canNotAccelerateWarned) {
					System.err.println(
						"Warn: The WorldAparapi can NOT accelerate with such type of invoking working:" +
							source.getClass().getName());
					canNotAccelerateWarned = true;
				}
				break;
			}
		}
		this.timeUnit = timeUnit;
		if (invokerLegal) {
			ArrayList<Source<Integer>> sinSources = new ArrayList<>();
			for (Source<Integer> source : sources) {
				if (source.getInvoking() instanceof SinInvoking) {
					sinSources.add(source);
				}
			}
			sinSourcesCount = sinSources.size();
			if (sinSourcesCount > 0) {
				sinSourcesType = new int[sinSourcesCount];
				sinSourcesInvokedNodeId = new int[sinSourcesCount];
				sinSourcesStartTime = new float[sinSourcesCount];
				sinSourcesEndTime = new float[sinSourcesCount];
				sinSourcesScale = new float[sinSourcesCount];
				sinSourcesPeriod = new float[sinSourcesCount];
				sinSourcesInitialPhase = new float[sinSourcesCount];
				for (int sinSourceId = 0; sinSourceId < sinSources.size(); sinSourceId++) {
					Source<Integer> source = sinSources.get(sinSourceId);
					SinInvoking sinInvoking = (SinInvoking) source.getInvoking();
					sinSourcesType[sinSourceId] = getInvokerTypeCode(sinInvoking.getType());
					sinSourcesInvokedNodeId[sinSourceId] = source.getNodeId();
					sinSourcesStartTime[sinSourceId] = sinInvoking.getStartTime();
					sinSourcesEndTime[sinSourceId] = sinInvoking.getEndTime();
					sinSourcesScale[sinSourceId] = sinInvoking.getScale();
					sinSourcesPeriod[sinSourceId] = sinInvoking.getPeriod();
					sinSourcesInitialPhase[sinSourceId] = sinInvoking.getInitialPhase();
				}
			}
			execute(nodesCount, count);
			computeCount += count;
			time += this.timeUnit * count;
			sinSourcesCount = 0;
		} else {
			for (int i = 0; i < count; i++) {
				int setPositionNodeId = -1;
				float setPosition = 0.0f;
				for (Source<Integer> source : sources) {
					if (time > source.getEndTime()) continue;
					int nodeId = source.getNodeId();
					float time = this.time - source.getStartTime();
					
					if (source.getType() == Invoking.Type.FORCE) {
						nodesVelocity[nodeId] += source.getValue(time) * this.timeUnit / nodesMass[nodeId];
					} else if (source.getType() == Invoking.Type.POSITION) {
						setPositionNodeId = nodeId;
						setPosition = source.getValue(time);
					}
				}
				execute(nodesCount);
				if (setPositionNodeId != -1) {
					if (computeCount % 2 == 0) {
						nodesOffset_s1[setPositionNodeId] = setPosition;
					} else {
						nodesOffset_s0[setPositionNodeId] = setPosition;
					}
				}
				computeCount++;
				time += this.timeUnit;
			}
		}
		
	}
	
	
	@Override
	public void run() {
		int nodeId = getGlobalId();
		int thisComputeCount = computeCount + getPassId();
		float force = 0.0f;
		
		//计算invokers
		boolean positionSet = false;
		float nowProcessedTime = time + getPassId() * timeUnit;
		for (int sinSourceId = 0; sinSourceId < sinSourcesCount; sinSourceId++) {
			if (sinSourcesInvokedNodeId[sinSourceId] != nodeId) continue;
			if (nowProcessedTime < sinSourcesStartTime[sinSourceId]) continue;
			if (nowProcessedTime > sinSourcesEndTime[sinSourceId]) continue;
			float time = nowProcessedTime - sinSourcesStartTime[sinSourceId];
			float phase = time / sinSourcesPeriod[sinSourceId] * 2.0f * PI
				+ sinSourcesInitialPhase[sinSourceId];
			float invokeValue = sinSourcesScale[sinSourceId] * sin(phase);
			
			if (sinSourcesType[sinSourceId] == INVOKER_TYPE_FORCE) {
				force += invokeValue;
			} else if (sinSourcesType[sinSourceId] == INVOKER_TYPE_POSITION) {
				if (thisComputeCount % 2 == 0) {
					nodesOffset_s1[nodeId] = invokeValue;
				} else {
					nodesOffset_s0[nodeId] = invokeValue;
				}
				positionSet = true;
			}
		}
		
		//计算由link产生的force
		int linkedId = nodesImpactStartId[nodeId];
		int linkedEndId = nodesImpactEndId[nodeId];
		if (thisComputeCount % 2 == 0) {
			while (linkedId < linkedEndId) {
				force += (nodesOffset_s0[impactsFromNodeId[linkedId]] - nodesOffset_s0[nodeId]) * impactsStrength[linkedId];
				linkedId++;
			}
		} else {
			while (linkedId < linkedEndId) {
				force += (nodesOffset_s1[impactsFromNodeId[linkedId]] - nodesOffset_s1[nodeId]) * impactsStrength[linkedId];
				linkedId++;
			}
		}
		
		
		//由 force 算出新速度和新位移
		nodesVelocity[nodeId] += force / nodesMass[nodeId] * timeUnit;
		if (!positionSet) {
			if (thisComputeCount % 2 == 0) {
				nodesOffset_s1[nodeId] = nodesOffset_s0[nodeId] + nodesVelocity[nodeId] * timeUnit;
			} else {
				nodesOffset_s0[nodeId] = nodesOffset_s1[nodeId] + nodesVelocity[nodeId] * timeUnit;
			}
		}
		
		
		//阻尼
		nodesVelocity[nodeId] -= nodesVelocity[nodeId] * nodesDamping[nodeId] * timeUnit;
		
	}
	
}
