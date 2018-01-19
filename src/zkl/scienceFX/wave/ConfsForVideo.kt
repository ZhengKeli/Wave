package zkl.scienceFX.wave

import javafx.geometry.Rectangle2D
import javafx.scene.paint.Color
import zkl.scienceFX.wave.conf.*
import zkl.scienceFX.wave.fx.*
import java.io.File
import java.util.*
import kotlin.math.PI

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
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 100
			}
			sinInvoke { }
		}
		visual {
			canvasWidth = 4000.0
			canvasHeight = 1000.0
			painter = LinePainter()
		}
		export {
			exportDir = File("D:/scienceFX/firstOutput")
			exportTimeRange = 0.0f..100.0f
		}
	}
	
	/**
	 * 波的独立传播
	 */
	fun independentPropagation() = conf {
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 200
				defaultUnitMass = 1.1f
				sinInvoke {
					targetUnitId = 0
				}
				sinInvoke {
					targetUnitId = 0
					scale *= 2.5f
					delay = period * 2.0f / 3.0f
				}
				sinInvoke {
					targetUnitId = unitCount - 1
					scale *= -1.5f
				}
			}
		}
		visual {
			canvasWidth = 4000.0
			canvasHeight = 1500.0
			painter = LinePainter()
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
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 140
				onCreateUnitDraft { index ->
					if (index > unitCount / 2) {
						mass *= 10.0f
						color = Color.DARKTURQUOISE
					}
				}
			}
			sinInvoke { }
		}
		visual {
			canvasWidth = 2400.0
			canvasHeight = 1200.0
			painter = LinePainter(1.3)
			drawArea = Rectangle2D(100.0 - canvasWidth / 2.0, 0.0, canvasWidth * 2.0, canvasHeight)
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
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 140
				onCreateUnitDraft { index ->
					if (index < unitCount / 2) {
						mass *= 10.0f
						color = Color.DARKTURQUOISE
					}
				}
			}
			sinInvoke { }
		}
		visual {
			canvasWidth = 2000.0
			canvasHeight = 1500.0
			painter = LinePainter(1.5)
			drawArea = Rectangle2D(-canvasWidth / 2.0, 0.0, canvasWidth * 2.0, canvasHeight)
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
		physics {
			val absorbThick = 200
			val absorbDamping = 0.05f
			
			simpleWaveWorld()
			timeUnit = 0.5f
			processCount = 6
			lineDraft {
				unitCount = 420
				onCreateUnitDraft { index ->
					val startIndex = unitCount - absorbThick
					if (index == unitCount) {
						setAsWall()
					} else if (index > startIndex) {
						val rate: Float = (index - startIndex).toFloat() / absorbThick
						damping = defaultDamping + absorbDamping * rate
						color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
				}
			}
			squareInvoke {
				scale = 150f
				period = 240f
				repeat = 2f
			}
		}
		visual {
			canvasWidth = 1800.0
			canvasHeight = 600.0
			painter = LinePainter()
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
		physics {
			simpleWaveWorld()
			timeUnit = 0.47f
			lineDraft {
				unitCount = 1000
				cosInvoke {
					targetUnitId = 0
					period = 50f
					repeat = 15f
					scale = 10f
				}
				cosInvoke {
					targetUnitId = unitCount - 1
					period = 55f
					repeat = 14f
					scale = -10f * 50f / 55f
				}
			}
		}
		visual {
			painter = LinePainter()
			canvasWidth = 2000.0
			canvasHeight = 1200.0
			drawArea = Rectangle2D(-canvasWidth / 2.0, 0.0, canvasWidth * 2.0, canvasHeight)
		}
		export {
			exportDir = File("D:/scienceFX/beatFrequency")
			exportPrefix = "beatFrequency_"
			exportTimeRange = 700f..1900f
//			isAutoModeOn = true
		}
	}
	
	
	//二维
	
	/**
	 * 水面模拟
	 */
	fun simple2D() = conf {
		physics {
			timeUnit = 0.5f
			simpleWaveWorld()
			rectDraft {
				columnCount = 200
				rowCount = 150
				//defaultDamping=0.01f
				
				this@physics.sinInvoke {
					targetUnitId = getUnitId(0, 0)
					//delay = 50f
					period = 80.0f
					repeat = 3f
				}
			}
		}
		visualConf2D {
			painter = WaterSurfacePainter()
		}
		export {
			exportDir = File("D:/scienceFX/simple2D")
			exportPrefix = "simple2D_"
			exportTimeRange = 0f..1300f
		}
	}
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf {
		physics {
			aparapiWaveWorld()
			rectBoarderAbsorbedDraft {
				rowCount = 300
				columnCount = 400
				val wavelength = 20.0
				val slitWidth = wavelength * 3.0
				val sourceUnitId = getUnitId(rowCount / 2 + 3, columnCount / 5)
				onCreateUnitDraft { row, column ->
					if (column == columnCount / 2 && !(row > rowCount / 2 - slitWidth / 2 && row < rowCount / 2 + slitWidth / 2)) {
						mass = Float.MAX_VALUE
						color = Color.RED
					}
				}
				
				this@physics.cosInvoke {
					targetUnitId = sourceUnitId
					period = (wavelength / Math.sqrt(defaultLinkStrength.toDouble())).toFloat()
					repeat = 100f
				}
			}
			
		}
		visualConf2D { painter = WaveEnergyPainter(60f) }
		export {
			exportDir = File("D:/scienceFX/diffraction")
			exportPrefix = "diffraction_"
			exportTimeRange = 0f..1000f
			isAutoModeOn = true
		}
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf {
		physics {
			aparapiWaveWorld()
			rectBoarderAbsorbedDraft {
				rowCount = 200
				columnCount = 200
				val sourceUnitId1 = getUnitId(rowCount / 3, columnCount / 3)
				val sourceUnitId2 = getUnitId(rowCount / 3 + 30, rowCount / 3 + 33)
				
				var invokeCount = 0
				this@physics.onInvoke {
					invokeCount++
					if (invokeCount % 2 == 1) SinInvokeConf().apply {
						targetUnitId = sourceUnitId1
						period = 20f
						repeat = 100f
						scale = 5f
					}.invoke(this)
					else SinInvokeConf().apply {
						targetUnitId = sourceUnitId2
						period = 20f
						repeat = 50f
						scale = 5f
						initialPhase = (PI / 2.0).toFloat()
					}.invoke(this)
				}
			}
		}
		visualConf2D {
			painter = WaveEnergyPainter(7f)
		}
		export {
			exportDir = File("D:/scienceFX/interference")
			exportPrefix = "interference_"
			exportTimeRange = 0f..1000f
			isAutoModeOn = true
		}
	}
	
	/**
	 * 波带片
	 */
	fun zonePlate() = conf {
		physics {
			aparapiWaveWorld()
			timeUnit = 0.1f
			processCount = 10
			rectBoarderAbsorbedDraft {
				rowCount = 250
				columnCount = 400
				/**
				 * 波源和接收点到波带片的距离
				 */
				val distance = 53.0f
				val wavelength = 21.0f
				
				val sourceUnitId = getUnitId(rowCount / 2, columnCount / 2 - distance.toInt())
				val waveSpeed = Math.sqrt((defaultLinkStrength / defaultUnitMass).toDouble()).toFloat()
				val zeroPoints = ArrayList<Int>().apply {
					var k: Int = Math.floor((2 * 2 * distance / wavelength).toDouble()).toInt() //半波长倍数
					while (true) {
						k += 1
						val h: Double = Math.sqrt((k * k * wavelength * wavelength / 16 - distance * distance).toDouble())
						if (h <= rowCount / 2) {
							this.add(h.toInt())
						} else {
							break
						}
					}
				}
				onCreateUnitDraft { row, column ->
					if (column == columnCount / 2) {
						val r = Math.abs(row - rowCount / 2)
						var i = 0
						while (i < zeroPoints.size) {
							if (r >= zeroPoints[i]) {
								i++
							} else {
								break
							}
						}
						if (i % 2 == 1) {
							setAsWall()
						}
					}
				}
				
				this@physics.cosInvoke {
					targetUnitId = sourceUnitId
					period = wavelength / waveSpeed
					repeat = 1000f
				}
				
			}
		}
		visualConf2D {
			painter = WaveEnergyPainter(15f)
		}
		export {
			exportDir = File("D:/scienceFX/zonePlate")
			exportPrefix = "zonePlate_"
			exportTimeRange = 0f..800f
			isAutoModeOn = false
		}
	}
	
}