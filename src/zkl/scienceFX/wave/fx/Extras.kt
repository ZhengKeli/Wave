package zkl.scienceFX.wave.fx

import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.abstracts.WaveUnit
import zkl.scienceFX.wave.physics.abstracts.WaveUnitDraft


var WaveUnitDraft.color:Color?
	get() = this.extra as? Color
	set(value) { this.extra = value}
var WaveUnit.color:Color?
	get() = this.extra as? Color
	set(value) { this.extra = value}

