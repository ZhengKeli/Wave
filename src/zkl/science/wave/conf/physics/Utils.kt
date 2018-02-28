package zkl.science.wave.conf.physics

import javafx.scene.paint.Color
import zkl.science.wave.painter.color

fun NodeConf.setAsWall() {
	mass = Float.MAX_VALUE
	color = Color.RED
}