package zkl.science.wave.painter

import javafx.scene.paint.Color
import zkl.science.wave.conf.physics.NodeConf
import zkl.science.wave.world.Node

fun colorMix(color1: Color, color2: Color, weight1: Double = 1.0, weight2: Double = 1.0): Color {
	val weightSum = weight1 + weight2
	return Color(
		(color1.red * weight1 + color2.red * weight2) / weightSum,
		(color1.green * weight1 + color2.green * weight2) / weightSum,
		(color1.blue * weight1 + color2.blue * weight2) / weightSum,
		(color1.opacity * weight1 + color2.opacity * weight2) / weightSum
	)
}

var NodeConf.color: Color?
	get() = this.extra as? Color
	set(value) {
		this.extra = value
	}
var Node<*>.color: Color?
	get() = this.extra as? Color
	set(value) {
		this.extra = value
	}