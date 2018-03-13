package zkl.science.wave.conf.export

import zkl.science.wave.conf.Conf
import java.io.File


fun Conf.export(body: ExportConf.() -> Unit) {
	this.exportConf = ExportConf().also { body.invoke(it) }
}

class ExportConf {
	lateinit var exportDir: File
	var exportPrefix: String = ""
	
	var exportTimeRange: ClosedRange<Float>? = null
}

val ExportConf?.autoExport get() = this?.exportTimeRange != null
