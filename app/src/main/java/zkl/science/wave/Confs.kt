package zkl.science.wave

import javafx.scene.paint.Color
import zkl.science.wave.conf.conf
import zkl.science.wave.conf.physics.*
import zkl.science.wave.conf.visual.energyPainter
import zkl.science.wave.conf.visual.lineVisual
import zkl.science.wave.conf.visual.offsetPainter
import zkl.science.wave.conf.visual.rectVisual
import zkl.science.wave.painter.color
import zkl.science.wave.painter.colorMix
import zkl.science.wave.world.line.LineNodeId
import zkl.science.wave.world.rect.RectNodeId
import java.util.*
import kotlin.math.floor
import kotlin.math.roundToInt
import kotlin.math.sqrt

val CONF by lazy { ConfsForVideo.interference() }
//val DEFAULT_CONF = Confs.diffraction()

object Confs {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun simpleLine() = conf {
		linePhysics {
			length = 50
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual {
			intensity = 0.3
		}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		linePhysics {
			nodeDrafter { (x) ->
				if ((x > length / 2) == direction) {
					mass *= massScale
					color = Color.DARKTURQUOISE
				}
			}
			sinSourceInteractor { nodeId = LineNodeId(0) }
			cpuWorld()
		}
		lineVisual { }
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
			
			length =
				if (doResonate) resonateLength
				else resonateLength + 4 //加上4就不共振了
			
			cosSourceInteractor {
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
			cosSourceInteractor { }
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
				amplitude = 70f
				period = 20f
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
			timeUnit = 0.5f
			processCount = 5
			
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
				amplitude = 150f
				period = 400f
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
			timeUnit = 0.01f
			length = 300
			sinSourceInteractor {
				nodeId = LineNodeId(length / 2)
				period = 1.0f
				amplitude = 5000f
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
			width = 200
			height = 200
			cosSourceInteractor {
				nodeId = RectNodeId(0, 0)
				period = 20f
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
	fun multiMedia2D(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		rectPhysics {
			timeOffset = 320f
			
			width = 300
			height = 300
			boarderAbsorb { }
			nodeDrafter { (_, y) ->
				if ((y > height / 2) == direction) mass *= massScale
				if (y == height / 2) color = Color.YELLOW
			}
			
			cosSourceInteractor {
				nodeId = RectNodeId(height / 3, width / 3)
				period = 20f
				repeat = 100f
				amplitude = 5f
			}
			cosSourceInteractor {
				nodeId = RectNodeId(height / 3, width / 3 + 30)
				period = 20f
				repeat = 100f
				amplitude = 5f
			}
			
			gpuWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf {
		rectPhysics {
			timeUnit = 0.5f
			
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
			gpuWorld()
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
			
			var interactCount = 0
			val sourceIds = arrayOf(
				RectNodeId(width / 3, height / 3),
				RectNodeId(width / 2 + 3, height / 2))
			customInteractor {
				SinSourceConf<RectNodeId>().apply {
					nodeId = sourceIds[interactCount++]
					period = 20f
					repeat = 70f
					amplitude = 5f
				}.invoke(this)
			}
			
			gpuWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	/**
	 * 波束 & 反射和折射 【计算量大！！】
	 */
	fun planeWave() = conf {
		rectPhysics {
			
			timeUnit = 1f
			timeOffset = 4000f
			processCount = 100
			
			height = 600
			width = 2500
			
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
			
			gpuWorld()
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
			timeOffset = 320f
			timeUnit = 0.1f
			processCount = 10
			
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
			
			gpuWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
	fun resonate2D() = conf {
		rectPhysics {
			timeUnit = 0.5f
			processCount = 10
			
			height = 250
			width = 400
			boarderAbsorb { }
			
			val k = 5f
			val n = 0.5f
			val wavelength = 30
			val waveSpeed = sqrt(defaultLink.strength * k / defaultNode.mass)
			
			val startX = width / 2
			val endX = startX + (wavelength * n).roundToInt()
			val startY = height / 3
			val endY = height * 2 / 3
			
			linkDrafter { (x, y, _) ->
				if (y in startY until endY) {
					if (x in startX until endX) {
						strength *= k
					}
				}
			}
			
			cosSourceInteractor {
				nodeId = RectNodeId(width / 4, height / 2)
				period = wavelength / waveSpeed
				repeat = 10f
			}
			
			gpuWorld()
		}
		rectVisual {
			energyPainter()
		}
	}
	
}

