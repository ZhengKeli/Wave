package zkl.scienceFX.wave.conf

class VisualConf2D : VisualConf() {
	var samplingSize: Double = 1.0
}

fun Conf.visualConf2D(body: VisualConf2D.() -> Unit) {
	this.visualConf = VisualConf2D()
		.also {
			body.invoke(it)
		}
		.apply {
			val drafter = physics.waveWorldDrafter as RectDrafter
			canvasHeight = drafter.rowCount.toDouble() / samplingSize
			canvasWidth = drafter.columnCount.toDouble() / samplingSize
		}
}