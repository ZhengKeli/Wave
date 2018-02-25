package zkl.science.wave.painter

import javafx.scene.paint.Color
import zkl.science.wave.conf.physics.InstantNodeDraft
import zkl.science.wave.world.Node


var InstantNodeDraft.color: Color?
	get() = this.extra as? Color
	set(value) {
		this.extra = value
	}
var Node.color: Color?
	get() = this.extra as? Color
	set(value) {
		this.extra = value
	}

