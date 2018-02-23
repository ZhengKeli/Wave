package zkl.scienceFX.wave.fx

import javafx.scene.paint.Color
import zkl.scienceFX.wave.conf.InstantNodeDraft
import zkl.scienceFX.wave.physics.abstracts.Node


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

