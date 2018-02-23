package zkl.scienceFX.wave.physics.aparapi;

import com.aparapi.Kernel;

import java.util.ArrayList;
import java.util.List;

import zkl.scienceFX.wave.physics.abstracts.LinkDraft;
import zkl.scienceFX.wave.physics.abstracts.NodeDraft;
import zkl.scienceFX.wave.physics.abstracts.SinSource;
import zkl.scienceFX.wave.physics.abstracts.Source;
import zkl.scienceFX.wave.physics.abstracts.WorldDraft;

public class AparapiKernel extends Kernel {
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
	
	float time =0.0f;
	float timeUnit;
	
	private int sinInvokersCount = 0;
	private int[] sinInvokersType = new int[1];
	private int[] sinInvokersInvokedUnitId = new int[1];
	private float[] sinInvokersStartTime = new float[1];
	private float[] sinInvokersEndTime = new float[1];
	private float[] sinInvokersScale = new float[1];
	private float[] sinInvokersPeriod = new float[1];
	private float[] sinInvokersInitialPhase = new float[1];
	private static final float PI= (float) Math.PI;
	private static final int INVOKER_TYPE_FORCE=0;
	private static final int INVOKER_TYPE_POSITION=1;
	private static int getInvokerTypeCode(Source.Type invokeType){
		if(invokeType== Source.Type.FORCE){
			return INVOKER_TYPE_FORCE;
		}else if(invokeType== Source.Type.POSITION){
			return INVOKER_TYPE_POSITION;
		}else{
			return -1;
		}
	}
	
	public AparapiKernel(WorldDraft worldDraft) {
		//排入unit信息
		List<NodeDraft> units = worldDraft.getNodes();
		unitsCount = units.size();
		unitsOffset_s0 = new float[unitsCount];
		unitsOffset_s1 = new float[unitsCount];
		unitsVelocity = new float[unitsCount];
		unitsMass = new float[unitsCount];
		unitsDamping = new float[unitsCount];
		unitsExtra = new Object[unitsCount];
		for (int unitId = 0; unitId < unitsCount; unitId++) {
			NodeDraft unitDraft = units.get(unitId);
			unitsOffset_s0[unitId] = unitDraft.getOffset();
			unitsVelocity[unitId] = unitDraft.getVelocity();
			unitsMass[unitId] = unitDraft.getMass();
			unitsDamping[unitId] = unitDraft.getDamping();
			unitsExtra[unitId] = unitDraft.getExtra();
		}
		
		//排入link信息
		List<LinkDraft> links = worldDraft.getLinks();
		linksCount=links.size();
		linksExtra = new Object[linksCount];
		//构建 unit-links 映射表
		ArrayList<ArrayList<Integer>> unitsLinksId = new ArrayList<>(unitsCount);
		for(int unitId = 0; unitId< unitsCount; unitId++) {
			unitsLinksId.add(new ArrayList<>(4));
		}
		for(int linkId=0;linkId<linksCount;linkId++) {
			LinkDraft linkDraft = links.get(linkId);
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
				LinkDraft link = links.get(linkId);
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
	
	private boolean canNotAccelerateWarned=false;
	public void process(int count,float timeUnit, List<Source> invokers){
		boolean invokerLegal=true;
		for (Source invoker : invokers) {
			if (!(invoker instanceof SinSource)) {
				invokerLegal=false;
				if(!canNotAccelerateWarned) {
					System.err.println(
						"Warn: The WorldAparapi can NOT accelerate with such type of invoker working:" +
							invoker.getClass().getName());
					canNotAccelerateWarned=true;
				}
				break;
			}
		}
		this.timeUnit = timeUnit;
		if (invokerLegal) {
			ArrayList<SinSource> sinInvokers = new ArrayList<>();
			for (Source invoker : invokers) {
				if (invoker instanceof SinSource) {
					sinInvokers.add((SinSource) invoker);
				}
			}
			sinInvokersCount=sinInvokers.size();
			if (sinInvokersCount > 0) {
				sinInvokersType = new int[sinInvokersCount];
				sinInvokersInvokedUnitId = new int[sinInvokersCount];
				sinInvokersStartTime = new float[sinInvokersCount];
				sinInvokersEndTime = new float[sinInvokersCount];
				sinInvokersScale = new float[sinInvokersCount];
				sinInvokersPeriod = new float[sinInvokersCount];
				sinInvokersInitialPhase = new float[sinInvokersCount];
				for(int sinInvokerId=0;sinInvokerId<sinInvokers.size();sinInvokerId++) {
					SinSource sinInvoker = sinInvokers.get(sinInvokerId);
					sinInvokersType[sinInvokerId] = getInvokerTypeCode(sinInvoker.getType());
					sinInvokersInvokedUnitId[sinInvokerId] = sinInvoker.getInvokedUnitId();
					sinInvokersStartTime[sinInvokerId] = sinInvoker.getStartTime();
					sinInvokersEndTime[sinInvokerId] = sinInvoker.getEndTime();
					sinInvokersScale[sinInvokerId] = sinInvoker.getScale();
					sinInvokersPeriod[sinInvokerId] = sinInvoker.getPeriod();
					sinInvokersInitialPhase[sinInvokerId] = sinInvoker.getInitialPhase();
				}
			}
			execute(unitsCount, count);
			computeCount += count;
			time += this.timeUnit * count;
			sinInvokersCount=0;
		}else {
			for(int i=0;i<count;i++) {
				int setPositionUnitId = -1;
				float setPosition = 0.0f;
				for (Source invoker : invokers) {
					if (time > invoker.getEndTime()) continue;
					int invokedUnitId = invoker.getInvokedUnitId();
					float time = this.time - invoker.getStartTime();
					
					if (invoker.getType() == Source.Type.FORCE) {
						unitsVelocity[invokedUnitId] += invoker.getValue(time) * this.timeUnit / unitsMass[invokedUnitId];
					} else if (invoker.getType() == Source.Type.POSITION) {
						setPositionUnitId = invokedUnitId;
						setPosition = invoker.getValue(time);
					}
				}
				execute(unitsCount);
				if (setPositionUnitId!=-1) {
					if (computeCount%2 == 0) {
						unitsOffset_s1[setPositionUnitId] = setPosition;
					}else {
						unitsOffset_s0[setPositionUnitId] = setPosition;
					}
				}
				computeCount++;
				time += this.timeUnit;
			}
		}
		
	}
	
	
	@Override public void run() {
		int unitId=getGlobalId();
		int thisComputeCount = computeCount + getPassId();
		float force = 0.0f;
		
		//计算invokers
		boolean positionSet=false;
		float nowProcessedTime = time + getPassId() * timeUnit;
		for (int sinInvokerId = 0; sinInvokerId < sinInvokersCount; sinInvokerId++) {
			if (sinInvokersInvokedUnitId[sinInvokerId] != unitId) continue;
			if (nowProcessedTime < sinInvokersStartTime[sinInvokerId]) continue;
			if (nowProcessedTime > sinInvokersEndTime[sinInvokerId]) continue;
			float time = nowProcessedTime - sinInvokersStartTime[sinInvokerId];
			float phase = time / sinInvokersPeriod[sinInvokerId] * 2.0f * PI
				+ sinInvokersInitialPhase[sinInvokerId];
			float invokeValue = sinInvokersScale[sinInvokerId] * sin(phase);
			
			if (sinInvokersType[sinInvokerId] == INVOKER_TYPE_FORCE) {
				force += invokeValue;
			}else if (sinInvokersType[sinInvokerId] == INVOKER_TYPE_POSITION){
				if (thisComputeCount%2 == 0) {
					unitsOffset_s1[unitId] = invokeValue;
				}else {
					unitsOffset_s0[unitId] = invokeValue;
				}
				positionSet=true;
			}
		}
		
		//计算由link产生的force
		int linkedId = unitsImpactStartId[unitId];
		int linkedEndId = unitsImpactEndId[unitId];
		if (thisComputeCount%2 == 0) {
			while (linkedId < linkedEndId) {
				force += (unitsOffset_s0[impactsFromUnitId[linkedId]] - unitsOffset_s0[unitId]) * impactsStrength[linkedId];
				linkedId++;
			}
		}else{
			while (linkedId < linkedEndId) {
				force += (unitsOffset_s1[impactsFromUnitId[linkedId]] - unitsOffset_s1[unitId]) * impactsStrength[linkedId];
				linkedId++;
			}
		}
		
		
		//由 force 算出新速度和新位移
		unitsVelocity[unitId] += force / unitsMass[unitId] * timeUnit;
		if (!positionSet) {
			if (thisComputeCount%2 == 0) {
				unitsOffset_s1[unitId] = unitsOffset_s0[unitId]+unitsVelocity[unitId] * timeUnit;
			}else {
				unitsOffset_s0[unitId] = unitsOffset_s1[unitId]+unitsVelocity[unitId] * timeUnit;
			}
		}
		
		
		//阻尼
		unitsVelocity[unitId] -= unitsVelocity[unitId] * unitsDamping[unitId] * timeUnit;
		
	}
	
}
