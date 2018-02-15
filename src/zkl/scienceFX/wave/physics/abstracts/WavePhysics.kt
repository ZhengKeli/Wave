package zkl.scienceFX.wave.physics.abstracts

object WavePhysics {
	
	fun processLink(link: WaveLink, unit1: WaveUnit, unit2: WaveUnit, timeUnit: Float) {
		val impact = (unit1.offset - unit2.offset) * link.strength * timeUnit
		unit1.velocity -= impact / unit1.mass
		unit2.velocity += impact / unit2.mass
	}
	
	fun processUnit(unit: WaveUnit, timeUnit: Float) {
		unit.run {
			offset += velocity * timeUnit
			velocity -= velocity * damping * timeUnit
		}
	}
	
}
