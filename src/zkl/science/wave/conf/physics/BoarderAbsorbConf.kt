package zkl.science.wave.conf.physics

import kotlin.math.min


class BoarderAbsorbConf {
	var absorbThick: Int = 30
	var absorbDamping: Float = 0.2f
}

fun LinePhysicsConf.boarderAbsorb(body: BoarderAbsorbConf.() -> Unit) {
	BoarderAbsorbConf().apply(body).run {
		nodeDrafter { x ->
			val distance = min(x, length - x)
			if (distance > absorbThick) return@nodeDrafter
			val rate = (absorbThick - distance).toFloat() / absorbThick
			damping += absorbDamping * rate
		}
	}
}

fun RectPhysicsConf.boarderAbsorb(body: BoarderAbsorbConf.() -> Unit) {
	BoarderAbsorbConf().apply(body).run {
		nodeDrafter { x, y ->
			val distance = min(min(x, width - x), min(y, height - y))
			if (distance > absorbThick) return@nodeDrafter
			val rate = (absorbThick - distance).toFloat() / absorbThick
			damping += absorbDamping * rate
		}
	}
}

