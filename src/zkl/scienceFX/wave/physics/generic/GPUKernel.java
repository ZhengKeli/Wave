package zkl.scienceFX.wave.physics.generic;

import com.aparapi.Kernel;

import java.util.ArrayList;
import java.util.List;

import zkl.scienceFX.wave.physics.Invoking;
import zkl.scienceFX.wave.physics.SinInvoking;
import zkl.scienceFX.wave.physics.Source;

public class GPUKernel extends Kernel {
	/**
	 * 为了避免不同计算单元的 unitOffset 互相影响（在 intel 的运算平台上会互相影响），
	 * 将 unitOffset 分为两部分，交替地用于存储每次计算的源数据和计算结果，
	 * 用computeCount的奇偶来判断用哪个
	 */
	int computeCount = 0;
	
	int unitsCount;
	float[] unitsOffset_s0;
	float[] unitsOffset_s1;
	float[] unitsVelocity;
	float[] unitsMass;
	float[] unitsDamping;
	Object[] unitsExtra;
	
	int linksCount;
	Object[] linksExtra;
	
	int[] impactsFromUnitId;
	float[] impactsStrength;
	private int[] unitsImpactStartId;
	private int[] unitsImpactEndId;
	int[] linksImpactId1;
	int[] linksImpactId2;
	
	float time = 0.0f;
	float timeUnit;
	
	private int sinSourcesCount = 0;
	private int[] sinSourcesType = new int[1];
	private int[] sinSourcesInvokedUnitId = new int[1];
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
	
	public GPUKernel(GenericWorldDraft worldDraft) {
		//排入unit信息
		List<GenericNodeDraft> units = worldDraft.getNodes();
		unitsCount = units.size();
		unitsOffset_s0 = new float[unitsCount];
		unitsOffset_s1 = new float[unitsCount];
		unitsVelocity = new float[unitsCount];
		unitsMass = new float[unitsCount];
		unitsDamping = new float[unitsCount];
		unitsExtra = new Object[unitsCount];
		for (int unitId = 0; unitId < unitsCount; unitId++) {
			GenericNodeDraft unitDraft = units.get(unitId);
			unitsOffset_s0[unitId] = unitDraft.getOffset();
			unitsVelocity[unitId] = unitDraft.getVelocity();
			unitsMass[unitId] = unitDraft.getMass();
			unitsDamping[unitId] = unitDraft.getDamping();
			unitsExtra[unitId] = unitDraft.getExtra();
		}
		
		//排入link信息
		List<GenericLinkDraft> links = worldDraft.getLinks();
		linksCount = links.size();
		linksExtra = new Object[linksCount];
		//构建 unit-links 映射表
		ArrayList<ArrayList<Integer>> unitsLinksId = new ArrayList<>(unitsCount);
		for (int unitId = 0; unitId < unitsCount; unitId++) {
			unitsLinksId.add(new ArrayList<>(4));
		}
		for (int linkId = 0; linkId < linksCount; linkId++) {
			GenericLinkDraft linkDraft = links.get(linkId);
			linksExtra[linkId] = linkDraft.getExtra();
			unitsLinksId.get(linkDraft.getUnitId1()).add(linkId);
			unitsLinksId.get(linkDraft.getUnitId2()).add(linkId);
		}
		
		//根据 unit-links 映射表构建 unit-impacts 映射
		impactsFromUnitId = new int[linksCount * 2];
		impactsStrength = new float[linksCount * 2];
		unitsImpactStartId = new int[unitsCount];
		unitsImpactEndId = new int[unitsCount];
		linksImpactId1 = new int[linksCount];
		linksImpactId2 = new int[linksCount];
		int impactId = 0;
		for (int unitId = 0; unitId < unitsCount; unitId++) {
			unitsImpactStartId[unitId] = impactId;
			for (int linkId : unitsLinksId.get(unitId)) {
				GenericLinkDraft link = links.get(linkId);
				if (unitId == link.getUnitId1()) {
					impactsFromUnitId[impactId] = link.getUnitId2();
					linksImpactId1[linkId] = impactId;
				} else if (unitId == link.getUnitId2()) {
					impactsFromUnitId[impactId] = link.getUnitId1();
					linksImpactId2[linkId] = impactId;
				}
				impactsStrength[impactId] = link.getStrength();
				impactId++;
			}
			unitsImpactEndId[unitId] = impactId;
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
				sinSourcesInvokedUnitId = new int[sinSourcesCount];
				sinSourcesStartTime = new float[sinSourcesCount];
				sinSourcesEndTime = new float[sinSourcesCount];
				sinSourcesScale = new float[sinSourcesCount];
				sinSourcesPeriod = new float[sinSourcesCount];
				sinSourcesInitialPhase = new float[sinSourcesCount];
				for (int sinSourceId = 0; sinSourceId < sinSources.size(); sinSourceId++) {
					Source<Integer> source = sinSources.get(sinSourceId);
					SinInvoking sinInvoking = (SinInvoking) source.getInvoking();
					sinSourcesType[sinSourceId] = getInvokerTypeCode(sinInvoking.getType());
					sinSourcesInvokedUnitId[sinSourceId] = source.getNodeId();
					sinSourcesStartTime[sinSourceId] = sinInvoking.getStartTime();
					sinSourcesEndTime[sinSourceId] = sinInvoking.getEndTime();
					sinSourcesScale[sinSourceId] = sinInvoking.getScale();
					sinSourcesPeriod[sinSourceId] = sinInvoking.getPeriod();
					sinSourcesInitialPhase[sinSourceId] = sinInvoking.getInitialPhase();
				}
			}
			execute(unitsCount, count);
			computeCount += count;
			time += this.timeUnit * count;
			sinSourcesCount = 0;
		} else {
			for (int i = 0; i < count; i++) {
				int setPositionUnitId = -1;
				float setPosition = 0.0f;
				for (Source<Integer> source : sources) {
					if (time > source.getEndTime()) continue;
					int nodeId = source.getNodeId();
					float time = this.time - source.getStartTime();
					
					if (source.getType() == Invoking.Type.FORCE) {
						unitsVelocity[nodeId] += source.getValue(time) * this.timeUnit / unitsMass[nodeId];
					} else if (source.getType() == Invoking.Type.POSITION) {
						setPositionUnitId = nodeId;
						setPosition = source.getValue(time);
					}
				}
				execute(unitsCount);
				if (setPositionUnitId != -1) {
					if (computeCount % 2 == 0) {
						unitsOffset_s1[setPositionUnitId] = setPosition;
					} else {
						unitsOffset_s0[setPositionUnitId] = setPosition;
					}
				}
				computeCount++;
				time += this.timeUnit;
			}
		}
		
	}
	
	
	@Override
	public void run() {
		int unitId = getGlobalId();
		int thisComputeCount = computeCount + getPassId();
		float force = 0.0f;
		
		//计算invokers
		boolean positionSet = false;
		float nowProcessedTime = time + getPassId() * timeUnit;
		for (int sinSourceId = 0; sinSourceId < sinSourcesCount; sinSourceId++) {
			if (sinSourcesInvokedUnitId[sinSourceId] != unitId) continue;
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
					unitsOffset_s1[unitId] = invokeValue;
				} else {
					unitsOffset_s0[unitId] = invokeValue;
				}
				positionSet = true;
			}
		}
		
		//计算由link产生的force
		int linkedId = unitsImpactStartId[unitId];
		int linkedEndId = unitsImpactEndId[unitId];
		if (thisComputeCount % 2 == 0) {
			while (linkedId < linkedEndId) {
				force += (unitsOffset_s0[impactsFromUnitId[linkedId]] - unitsOffset_s0[unitId]) * impactsStrength[linkedId];
				linkedId++;
			}
		} else {
			while (linkedId < linkedEndId) {
				force += (unitsOffset_s1[impactsFromUnitId[linkedId]] - unitsOffset_s1[unitId]) * impactsStrength[linkedId];
				linkedId++;
			}
		}
		
		
		//由 force 算出新速度和新位移
		unitsVelocity[unitId] += force / unitsMass[unitId] * timeUnit;
		if (!positionSet) {
			if (thisComputeCount % 2 == 0) {
				unitsOffset_s1[unitId] = unitsOffset_s0[unitId] + unitsVelocity[unitId] * timeUnit;
			} else {
				unitsOffset_s0[unitId] = unitsOffset_s1[unitId] + unitsVelocity[unitId] * timeUnit;
			}
		}
		
		
		//阻尼
		unitsVelocity[unitId] -= unitsVelocity[unitId] * unitsDamping[unitId] * timeUnit;
		
	}
	
}
