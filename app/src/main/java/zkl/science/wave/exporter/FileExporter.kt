package zkl.science.wave.exporter

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class FileExporter(val exportDir: File, val fileNamePrefix: String = "") {
	
	var imageId: Int = 0
	
	fun exportImage(bufferedImage: BufferedImage) {
		if (imageId == 0) {
			exportDir.deleteRecursively()
			exportDir.mkdirs()
		}
		val imageFile = File(exportDir, "$fileNamePrefix$imageId.png")
		ImageIO.write(bufferedImage, "png", imageFile)
		System.gc()
		println("exported ${imageFile.name}")
		this.imageId++
	}
	
	fun openExportDir() {
		try {
			Runtime.getRuntime().exec("explorer \"$exportDir\"")
		} catch (e: Exception) {
		}
	}
	
}
