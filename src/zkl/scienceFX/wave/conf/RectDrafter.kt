package zkl.scienceFX.wave.conf

import zkl.scienceFX.wave.physics.abstracts.LinkDraft
import zkl.scienceFX.wave.physics.abstracts.NodeDraft
import zkl.scienceFX.wave.physics.abstracts.WorldDraft


fun PhysicsConf.rectDraft(body: (RectDrafter.() -> Unit)? = null) {
	this.waveWorldDrafter = RectDrafter().also { body?.invoke(it) }
}

open class RectDrafter : WaveWorldDrafter {
	
	var rowCount: Int = 100
	var columnCount: Int = 100
	fun getUnitId(row: Int, column: Int) = row * columnCount + column
	fun getPosition(index: Int) = (index / columnCount) to (index % columnCount)
	
	var defaultUnitMass = 1.0f
	var defaultDamping = 0.0f
	var defaultLinkStrength = 0.3f
	
	var onCreateUnitDraft = ArrayList<InstantNodeDraft.(row: Int, column: Int) -> Unit>()
	var onCreateLinkDraft = ArrayList<InstantLinkDraft.(row1: Int, column1: Int, row2: Int, column2: Int) -> Unit>()
	var extra: Any? = null
	
	override fun invoke(): WorldDraft {
		
		val units = Array(rowCount * columnCount) { index ->
			val (row, column) = getPosition(index)
			InstantNodeDraft(index, defaultUnitMass, defaultDamping).also { unit ->
				onCreateUnitDraft.forEach { factory -> factory(unit, row, column) }
			}
		}.asList()
		
		val linkCount = rowCount * (columnCount - 1) + (rowCount - 1) * columnCount
		val links = ArrayList<LinkDraft>(linkCount)
		(0 until rowCount).map { row ->
			(0 until columnCount - 1).map { column ->
				val unitId1 = getUnitId(row, column)
				val unitId2 = getUnitId(row, column + 1)
				val link = InstantLinkDraft(unitId1, unitId2, defaultLinkStrength).also { link ->
					onCreateLinkDraft.forEach { factory -> factory(link, row, column, row, column + 1) }
				}
				links.add(link)
			}
		}
		(0 until columnCount).map { column ->
			(0 until rowCount - 1).map { row ->
				val unitId1 = getUnitId(row, column)
				val unitId2 = getUnitId(row + 1, column)
				val link = InstantLinkDraft(unitId1, unitId2, defaultLinkStrength).also { link ->
					onCreateLinkDraft.forEach { factory -> factory(link, row, column, row + 1, column) }
				}
				links.add(link)
			}
		}
		
		return object : WorldDraft {
			override val nodes: List<NodeDraft> = units
			override val links: List<LinkDraft> = links
			override val extra: Any? = this@RectDrafter.extra
		}
	}
	
}

fun RectDrafter.onCreateUnitDraft(body: InstantNodeDraft.(row: Int, column: Int) -> Unit) {
	this.onCreateUnitDraft.add(body)
}

fun RectDrafter.onCreateLinkDraft(body: InstantLinkDraft.(row1: Int, column1: Int, row2: Int, column2: Int) -> Unit) {
	this.onCreateLinkDraft.add(body)
}


