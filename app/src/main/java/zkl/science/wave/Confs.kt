package zkl.science.wave

import javafx.scene.paint.Color
import zkl.science.wave.conf.conf
import zkl.science.wave.conf.export.export
import zkl.science.wave.conf.physics.*
import zkl.science.wave.conf.visual.energyPainter
import zkl.science.wave.conf.visual.lineVisual
import zkl.science.wave.conf.visual.offsetPainter
import zkl.science.wave.conf.visual.rectVisual
import zkl.science.wave.painter.color
import zkl.science.wave.painter.colorMix
import zkl.science.wave.world.line.LineNodeId
import zkl.science.wave.world.rect.RectNodeId
import java.io.File
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * 用于研究的配置
 */
object ConfsForStudy {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun simpleLine() = conf {
		linePhysics {
			length = 70
			nodeDrafter { (x) ->
				if (x == length) setAsWall()
			}
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		linePhysics {
			length = 100
			nodeDrafter { (x) ->
				if ((x > length / 2) == direction) {
					mass *= massScale
					color = Color.DARKTURQUOISE
				}
			}
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {}
	}
	
	/**
	 * 共振
	 */
	fun resonate(doResonate: Boolean = true) = conf {
		linePhysics {
			val wavePeriod = 40f
			val resonateCount = 6
			val waveSpeed = sqrt(defaultLink.strength / defaultNode.mass)
			val waveLength = waveSpeed * wavePeriod
			val resonateLength = (waveLength * resonateCount / 2.0).roundToInt()
			
			length = when {
				doResonate -> resonateLength
				else -> resonateLength + 4 //加上4就不共振了
			}
			
			cosSourceInteractor {
				nodeId = LineNodeId(0)
				period = wavePeriod
				repeat = 1000f
			}
			cpuWorld()
		}
		lineVisual { }
	}
	
	/**
	 * 阻尼
	 */
	fun damping() = conf {
		linePhysics {
			length = 100
			defaultNode.damping = 0.02f
			cosSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual { }
	}
	
	/**
	 * 不同阻尼中传播
	 */
	fun multiDamping(direction: Boolean = true, theDamping: Float = 0.05f) = conf {
		linePhysics {
			length = 80
			nodeDrafter { (x) ->
				if (direction && x > length * 3 / 4) {
					damping = theDamping
					color = Color.GREEN
				} else if (!direction && x < length / 4) {
					damping = theDamping
					color = Color.GREEN
				}
			}
			sinSourceInteractor {
				nodeId = LineNodeId(0)
				period *= 0.4f
				amplitude *= 2f
			}
			cpuWorld()
		}
		lineVisual { }
	}
	
	/**
	 * 阻尼吸收
	 */
	fun dampingAbsorb() = conf {
		linePhysics {
			
			length = 100
			val absorbThick = 40
			val absorbDamping = 0.3f
			val absorbStart = length - absorbThick
			nodeDrafter { (x) ->
				when {
					x == length -> setAsWall()
					x > absorbStart -> {
						val rate: Float = (x - absorbStart).toFloat() / absorbThick
						damping += absorbDamping * rate
						color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
				}
			}
			
			sinSourceInteractor { nodeId = LineNodeId(0) }
			
			cpuWorld()
		}
		lineVisual { }
	}
	
	/**
	 * 方波
	 */
	fun squareWave() = conf {
		linePhysics {
			timeUnit *= 5f
			
			length = 800
			val absorbThick = 400
			val absorbDamping = 0.01f
			val absorbStart = length - absorbThick
			nodeDrafter { (x) ->
				when {
					x == length -> setAsWall()
					x > absorbStart -> {
						val rate: Float = (x - absorbStart).toFloat() / absorbThick
						damping += absorbDamping * rate
						color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
				}
			}
			
			squareSourceInteractor {
				nodeId = LineNodeId(0)
				amplitude *= 50f
				period *= 20f
				repeat = 10f
			}
			
			cpuWorld()
		}
		lineVisual { }
	}
	
	/**
	 * 脉冲
	 */
	fun impact() = conf {
		linePhysics {
			timeUnit /= 10f
			processCount *= 10
			length = 300
			sinSourceInteractor {
				nodeId = LineNodeId(length / 2)
				period *= 0.02f
				amplitude *= 5000f
			}
			cpuWorld()
		}
		lineVisual { }
	}
	
	
	//二维
	
	/**
	 * 简单的二维示例
	 */
	fun simpleRect() = conf {
		rectPhysics {
			width = 100
			height = 100
			cosSourceInteractor {
				nodeId = RectNodeId(31, 43)
				repeat = 4f
			}
			cpuWorld()
		}
		rectVisual {
			offsetPainter()
		}
	}
	
	/**
	 * 不同介质中的传播
	 */
	fun multiMedia2D(direction: Boolean = true, massScale: Float = 2.0f) = conf {
		rectPhysics {
			timeUnit *= 5f
			timeOffset = 400f
			
			width = 300
			height = 300
			boarderAbsorb { }
			nodeDrafter { (_, y) ->
				if ((y > height / 2) == direction) mass *= massScale
				if (y == height / 2) color = Color.YELLOW
			}
			
			cosSourceInteractor {
				nodeId = RectNodeId(height / 3, width / 4)
				repeat = 100f
			}
			cosSourceInteractor {
				nodeId = RectNodeId(height / 3, width / 4 + 50)
				repeat = 100f
			}
			
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
			intensity *= 0.2
		}
	}
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf {
		rectPhysics {
			timeUnit *= 5f
			
			height = 300
			width = 400
			
			val wavelength = 20.0
			val slitWidth = wavelength * 3.0
			
			boarderAbsorb { }
			nodeDrafter { (x, y) ->
				if (x == width / 2 && !(y > height / 2 - slitWidth / 2 && y < height / 2 + slitWidth / 2)) {
					mass = Float.MAX_VALUE
					color = Color.RED
				}
			}
			
			cosSourceInteractor {
				nodeId = RectNodeId(width / 5, height / 2 + 3)
				period = (wavelength / Math.sqrt(defaultLink.strength.toDouble())).toFloat()
				repeat = 100f
			}
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf {
		rectPhysics {
			width = 200
			height = 200
			
			boarderAbsorb { }
			
			val sourceIds = arrayOf(
				RectNodeId(width / 3, height / 3),
				RectNodeId(width / 2 + 3, height / 2))
			sourceIds.forEach { id ->
				sinSourceInteractor {
					nodeId = id
					repeat = 70f
					amplitude /= 2
				}
			}
			
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	/**
	 * 抛物面 （计算量大！！）
	 */
	fun paraboloid() = conf {
		rectPhysics {
			
			timeUnit *= 10f
			processCount *= 20
			timeOffset = 4000f
			
			height = 600
			width = 1500
			
			/**
			 * 凹面反射镜的焦距（s越小就越越弯）
			 */
			val s = 30
			/**
			 * 凹面反射镜在水平方向上的长度
			 */
			val l = 150
			
			val absorbThick = 30
			boarderAbsorb { this.absorbThick = 30 }
			nodeDrafter { (ax, ay) ->
				val x = ax - absorbThick
				val y = Math.round(ay - height / 2.0).toInt()
				if (x in 0..l) {
					if (x == Math.round(y * y / 4.0 / s).toInt()
						|| y == Math.round(Math.sqrt(x * 4.0 * s)).toInt()
						|| y == Math.round(-Math.sqrt(x * 4.0 * s)).toInt()) {
						setAsWall()
					}
				}
			}
			
			cosSourceInteractor {
				nodeId = RectNodeId(s + absorbThick, height / 2)
				repeat = 1000f
				period = 20f
			}
			
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	/**
	 * 波带片
	 */
	fun zonePlate() = conf {
		rectPhysics {
			timeUnit *= 5f
			timeOffset = 400f
			
			height = 250
			width = 400
			boarderAbsorb { }
			
			/**
			 * 波源和接收点到波带片的距离
			 */
			val distance = 53.0f
			val wavelength = 21.0f
			val sourceUnitId = RectNodeId(width / 2 - distance.toInt(), height / 2)
			val waveSpeed = sqrt(defaultLink.strength / defaultNode.mass)
			val zeroPoints = ArrayList<Int>().apply {
				var k: Int = floor(2 * 2 * distance / wavelength).roundToInt() //半波长倍数
				while (true) {
					k += 1
					val h = sqrt(k * k * wavelength * wavelength / 16 - distance * distance)
					if (h > height / 2) break
					add(h.roundToInt())
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
			
			aparapiWorld()
		}
		rectVisual {
			intensity *= 0.5
			energyPainter()
		}
	}
	
	/**
	 * 二维共振（未完成）
	 */
	fun resonate2D() = conf {
		rectPhysics {
			timeUnit *= 5f
			processCount *= 2
			
			height = 250
			width = 400
			boarderAbsorb { }
			
			val km = 3f
			val kl = 10f
			val n = 1.5f
			val wavePeriod = 50f
			val waveSpeed = sqrt(defaultLink.strength * kl / defaultNode.mass / km)
			val wavelength = waveSpeed * wavePeriod
			
			val startX = width / 2
			val endX = startX + (wavelength * n).roundToInt()
			val startY = height / 3
			val endY = height * 2 / 3
			
			nodeDrafter { (x, y) ->
				if (y in startY..endY) {
					if (x in startX..endX) {
						mass *= km
					}
				}
			}
			
			linkDrafter { (x, y, _) ->
				if (y in startY until endY) {
					if (x in startX until endX) {
						strength *= kl
					}
				}
			}
			
			
			for (y in 50 until 200) {
				cosSourceInteractor {
					nodeId = RectNodeId(width / 4, y)
					period = wavePeriod
					repeat = 10f
					amplitude *= 0.05f
				}
			}
			
			
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
}

/**
 * 用于视频的配置
 */
object ConfsForVideo {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun firstOutput() = conf {
		linePhysics {
			length = 100
			sinSourceInteractor {
				nodeId = LineNodeId(0)
				amplitude *= 0.5f
			}
			cpuWorld()
		}
		lineVisual {
			sceneWidth = 4000.0
			viewportX = 0.0
		}
		export {
			exportDir = File("D:/scienceFX/firstOutput")
			//exportTimeRange = 30.0f..60.0f
		}
	}
	
	/**
	 * 波的独立传播
	 */
	fun independentPropagation() = conf {
		linePhysics {
			length = 200
			defaultNode.mass *= 1.1f
			sinSourceInteractor {
				nodeId = LineNodeId(0)
				amplitude *= 1.5f
				delay = period * 2.0f / 3.0f
			}
			sinSourceInteractor {
				nodeId = LineNodeId(length - 1)
				amplitude *= -1.0f
			}
			cpuWorld()
		}
		lineVisual {
			sceneWidth = 3000.0
		}
		export {
			exportDir = File("D:/scienceFX/independentPropagation")
//			exportTimeRange = 50f..400f
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
			sceneWidth = 2400.0
		}
		export {
			exportDir = File("D:/scienceFX/multiMedia1")
//			exportTimeRange = 0f..300f
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
			sceneWidth = 2000.0
			intensity = 1.5
		}
		export {
			exportDir = File("D:/scienceFX/multiMedia1")
//			exportTimeRange = 0f..300f
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
				nodeId = LineNodeId(0)
				amplitude = 150f
				period = 240f
				repeat = 2f
			}
			
			cpuWorld()
		}
		lineVisual {
			sceneWidth = 1800.0
		}
		export {
			exportDir = File("D:/scienceFX/squareWave")
			//exportTimeRange = 0f..1300f
		}
	}
	
	/**
	 * 拍频
	 */
	fun beatFrequency() = conf {
		linePhysics {
			timeUnit = 0.5f
			length = 1000
			cosSourceInteractor {
				nodeId = LineNodeId(0)
				period = 50f
				repeat = 15f
				amplitude = 5f
			}
			cosSourceInteractor {
				nodeId = LineNodeId(length - 1)
				period = 55f
				repeat = 14f
				amplitude = -5f * 50f / 55f
			}
			cpuWorld()
		}
		lineVisual {
			sceneWidth = 3000.0
		}
		export {
			exportDir = File("D:/scienceFX/beatFrequency")
			exportPrefix = "beatFrequency_"
			//exportTimeRange = 700f..1900f
//			autoMode = true
		}
	}
	
	
	//二维
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf {
		rectPhysics {
			timeUnit *= 5f
			
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
			
			aparapiWorld()
		}
		rectVisual {
			intensity *= 3.0
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/diffraction")
			exportPrefix = "diffraction_"
			//exportTimeRange = 0f..1000f
		}
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf {
		rectPhysics {
			timeUnit *= 2f
			
			height = 200
			width = 200
			
			boarderAbsorb { }
			val sourceIds = arrayOf(
				RectNodeId(width / 3, height / 3),
				RectNodeId(width / 2 + 3, height / 2))
			sourceIds.forEach { id ->
				sinSourceInteractor {
					nodeId = id
					repeat = 70f
					amplitude /= 2
				}
			}
			
			aparapiWorld()
		}
		rectVisual {
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/interference")
			exportPrefix = "interference_"
			//exportTimeRange = 0f..1000f
		}
	}
	
	/**
	 * 波带片
	 */
	fun zonePlate() = conf {
		rectPhysics {
			timeUnit *= 5f
			
			height = 250
			width = 400
			boarderAbsorb { }
			
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
			
			aparapiWorld()
		}
		rectVisual {
			intensity *= 0.5
			energyPainter()
		}
		export {
			exportDir = File("D:/scienceFX/zonePlate")
			exportPrefix = "zonePlate_"
			//exportTimeRange = 0f..800f
		}
	}
	
}
