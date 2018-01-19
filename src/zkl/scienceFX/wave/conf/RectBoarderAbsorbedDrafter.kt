package zkl.scienceFX.wave.conf


fun PhysicsConf.rectBoarderAbsorbedDraft(body: RectBoarderAbsorbedDrafter.() -> Unit) {
	this.waveWorldDrafter = RectBoarderAbsorbedDrafter().also { body.invoke(it) }
}

class RectBoarderAbsorbedDrafter : RectDrafter() {
	val absorbThick: Int = 30
	var absorbDamping: Float = 0.2f
	
	init {
		this.onCreateUnitDraft { row, column ->
			when {
				row < absorbThick && column < absorbThick -> {
					val rate = Math.max(absorbThick - row, absorbThick - column).toFloat() / absorbThick
					setAsSoftWall(rate)
				}
				row > rowCount - absorbThick && column > columnCount - absorbThick -> {
					val rate = Math.max(row - (rowCount - absorbThick), column - (columnCount - absorbThick)).toFloat() / absorbThick
					setAsSoftWall(rate)
				}
				row < absorbThick -> setAsSoftWall((absorbThick - row).toFloat() / absorbThick)
				column < absorbThick -> setAsSoftWall((absorbThick - column).toFloat() / absorbThick)
				row > rowCount - absorbThick -> setAsSoftWall((row - (rowCount - absorbThick)).toFloat() / absorbThick)
				column > columnCount - absorbThick -> setAsSoftWall((column - (columnCount - absorbThick)).toFloat() / absorbThick)
			}
		}
	}
	
	private fun InstantWaveUnitDraft.setAsSoftWall(rate: Float) {
		this.damping = defaultDamping + absorbDamping * rate
		//waveUnit.visualExtra2D.color = Color.GREEN
		
	}
	
}
