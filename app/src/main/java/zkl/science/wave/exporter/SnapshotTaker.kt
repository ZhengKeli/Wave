package zkl.science.wave.exporter

import javafx.embed.swing.SwingFXUtils
import javafx.scene.SnapshotParameters
import javafx.scene.canvas.Canvas
import javafx.scene.image.WritableImage
import java.awt.image.BufferedImage

class SnapshotTaker {
	private var snapshotImage: WritableImage? = null
	private val snapshotParameters = SnapshotParameters()
	
	var bufferedImage: BufferedImage? = null
		private set
	
	fun takeSnapshot(canvas: Canvas): BufferedImage {
		val snapshotImage = canvas.snapshot(snapshotParameters, snapshotImage)
		return SwingFXUtils.fromFXImage(snapshotImage, bufferedImage).also { bufferedImage = it }
	}
}