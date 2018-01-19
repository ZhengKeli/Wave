package zkl.scienceFX.wave.conf

import javafx.geometry.Rectangle2D
import java.io.File


fun Conf.export(body: ExportConf.() -> Unit) {
	this.exportConf = ExportConf().also { body.invoke(it) }
}

class ExportConf {
	lateinit var exportDir: File
	var exportPrefix: String = ""
	
	var exportViewPort: Rectangle2D? = null
	
	var isAutoModeOn = false
	var exportTimeRange: ClosedRange<Float>? = null
}