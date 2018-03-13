package zkl.science.wave

import javafx.scene.paint.Color
import zkl.science.wave.conf.conf
import zkl.science.wave.conf.export.export
import zkl.science.wave.conf.physics.*
import zkl.science.wave.conf.visual.energyPainter
import zkl.science.wave.conf.visual.lineVisual
import zkl.science.wave.conf.visual.rectVisual
import zkl.science.wave.painter.color
import zkl.science.wave.painter.colorMix
import zkl.science.wave.world.line.LineNodeId
import zkl.science.wave.world.rect.RectNodeId
import java.io.File
import java.util.*
import kotlin.math.sqrt

val DEFAULT_CONF_FOR_VIDEO = ConfsForVideo.zonePlate()

/**
 * 用于拍摄视频的Conf
 */
object ConfsForVideo {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun firstOutput() = conf {
		linePhysics {
			length = 100
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {
			intensity = 0.3
		}
		export {
			exportDir = File("D:/scienceFX/firstOutput")
			exportTimeRange = 30.0f..60.0f
		}
	}
	
	/**
	 * 波的独立传播
	 */
	fun independentPropagation() = conf {
		linePhysics {
			length = 200
			defaultNode.mass = 1.1f
			sinSourceInteractor {
				nodeId = LineNodeId(0)
				amplitude *= 2.5f
				delay = period * 2.0f / 3.0f
			}
			sinSourceInteractor {
				nodeId = LineNodeId(length - 1)
				amplitude *= -1.5f
			}
			cpuWorld()
		}
		lineVisual {
			canvasWidth = 4000.0
			canvasHeight = 1500.0
		}
		export {
			exportDir = File("D:/scienceFX/independentPropagation")
			exportTimeRange = 50f..400f
		}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia1() = conf {
		linePhysics {
			length = 140
			nodeDrafter { (x) ->
				if (x > length / 2) {
					mass *= 10.0f
					color = Color.DARKTURQUOISE
				}
			}
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {
			canvasWidth = 2400.0
			canvasHeight = 1200.0
			intensity = 1.3
			
			viewportX = 100.0 - canvasWidth / 2.0
			viewportY = 0.0
			
		}
		export {
			exportDir = File("D:/scienceFX/multiMedia1")
			exportTimeRange = 0f..300f
			
		}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia2() = conf {
		linePhysics {
			length = 140
			nodeDrafter { (x) ->
				if (x < length / 2) {
					mass *= 10.0f
					color = Color.DARKTURQUOISE
				}
			}
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {
			canvasWidth = 2000.0
			canvasHeight = 1500.0
			intensity = 1.5
			
			viewportX = -canvasWidth / 2.0
			viewportY = 0.0
		}
		export {
			exportDir = File("D:/scienceFX/multiMedia1")
			exportTimeRange = 0f..300f
		}
	}
	
	/**
	 * 方波和阻尼吸收
	 */
	fun squareWave() = conf {
		linePhysics {
			timeUnit = 0.5f
			processCount = 6
			
			length = 420
			val absorbThick = 200
			val absorbDamping = 0.05f
			val absorbStart = length - absorbThick
			nodeDrafter { (x) ->
				if (x == length) {
					setAsWall()
				} else if (x > absorbStart) {
					val rate: Float = (x - absorbStart).toFloat() / absorbThick
					damping = defaultNode.damping + absorbDamping * rate
					color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
				}
			}
			
			squareSourceInteractor {
				amplitude = 150f
				period = 240f
				repeat = 2f
			}
			
			cpuWorld()
		}
		lineVisual {
			canvasWidth = 1800.0
			canvasHeight = 600.0
		}
		export {
			exportDir = File("D:/scienceFX/squareWave")
			exportTimeRange = 0f..1300f
		}
	}
	
	/**
	 * 拍频
	 */
	fun beatFrequency() = conf {
		linePhysics {
			timeUnit = 0.47f
			length = 1000
			cosSourceInteractor {
				nodeId = LineNodeId(0)
				period = 50f
				repeat = 15f
				amplitude = 10f
			}
			cosSourceInteractor {
				nodeId = LineNodeId(length - 1)
				period = 55f
				repeat = 14f
				amplitude = -10f * 50f / 55f
			}
			cpuWorld()
		}
		lineVisual {
			canvasWidth = 2000.0
			canvasHeight = 1200.0
			
			viewportX = -canvasWidth / 2.0
			viewportY = 0.0
		}
		export {
			exportDir = File("D:/scienceFX/beatFrequency")
			exportPrefix = "beatFrequency_"
			exportTimeRange = 700f..1900f
//			autoMode = true
		}
	}
	
	
	//二维
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf {
		rectPhysics {
			height = 300
			width = 400
			
			boarderAbsorb { }
			
			val wavelength = 20.0f
			val slitWidth = wavelength * 3.0
			val sourceUnitId = RectNodeId(width / 5, height / 2 + 3)
			nodeDrafter { (x, y) ->
				if (x == width / 2 && !(y > height / 2 - slitWidth / 2 && y < height / 2 + slitWidth / 2))
					setAsWall()
			}
			
			cosSourceInteractor {
				nodeId = sourceUnitId
				period = wavelength / sqrt(defaultLink.strength)
				repeat = 100f
			}
			
			gpuWorld()
		}
		rectVisual {
			intensity = 3.0
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/diffraction")
			exportPrefix = "diffraction_"
			exportTimeRange = 0f..1000f
		}
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf {
		rectPhysics {
			timeOffset = 100.0f
			
			height = 200
			width = 200
			
			val sourceIds = arrayOf(
				RectNodeId(width / 3, height / 3),
				RectNodeId(width / 2 + 3, height / 2))
			sourceIds.forEach { id->
				sinSourceInteractor {
					nodeId = id
					period = 20f
					repeat = 70f
					amplitude = 5f
				}
			}
			
			gpuWorld()
		}
		rectVisual {
			intensity = 0.7
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/interference")
			exportPrefix = "interference_"
//			exportTimeRange = 0f..1000f
		}
	}
	
	/**
	 * 波带片
	 */
	fun zonePlate() = conf {
		rectPhysics {
			timeUnit = 0.1f
			processCount = 10
			height = 250
			width = 400
			/**
			 * 波源和接收点到波带片的距离
			 */
			val distance = 53.0f
			val wavelength = 21.0f
			
			val sourceUnitId = RectNodeId(width / 2 - distance.toInt(), height / 2)
			val waveSpeed = Math.sqrt((defaultLink.strength / defaultNode.mass).toDouble()).toFloat()
			val zeroPoints = ArrayList<Int>().apply {
				var k: Int = Math.floor((2 * 2 * distance / wavelength).toDouble()).toInt() //半波长倍数
				while (true) {
					k += 1
					val h = sqrt((k * k * wavelength * wavelength / 16 - distance * distance).toDouble())
					if (h > height / 2) break
					add(h.toInt())
				}
			}
			nodeDrafter { (x, y) ->
				if (x == width / 2) {
					val r = Math.abs(y - height / 2)
					val i = zeroPoints.indexOfFirst { r < it }
					if (i % 2 == 1) setAsWall()
				}
			}
			
			cosSourceInteractor {
				nodeId = sourceUnitId
				period = wavelength / waveSpeed
				repeat = 1000f
			}
			
			gpuWorld()
		}
		rectVisual {
			intensity = 1.5
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/zonePlate")
			exportPrefix = "zonePlate_"
//			exportTimeRange = 0f..800f
		}
	}
	
}