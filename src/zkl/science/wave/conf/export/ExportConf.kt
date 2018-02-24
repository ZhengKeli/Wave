package zkl.science.wave.conf.export

import javafx.geometry.Rectangle2D
import zkl.science.wave.conf.Conf
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