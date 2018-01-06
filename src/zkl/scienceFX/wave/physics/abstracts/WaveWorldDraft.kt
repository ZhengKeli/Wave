package zkl.scienceFX.wave.physics.abstracts

import java.util.*

class WaveWorldDraft {
	var units:List<WaveUnitDraft> = ArrayList()
	var links:List<WaveLinkDraft> = ArrayList()
	var extra:Any?=null
}

open class WaveUnitDraft(
	var offset: Float = 0.0f, //该震动单位的偏移程度
	var velocity: Float = 0.0f, //该震动单位的速度
	var mass: Float = 1.0f, //该震动单位的质量
	var damping: Float = 0.0f, //阻尼
	var extra: Any? = null
)

open class WaveLinkDraft(
	var unitId1: Int,
	var unitId2: Int,
	var strength: Float = 1.0f, //该震动链接的强度（弹性系数）
	var extra: Any? = null
)



