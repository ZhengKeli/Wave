package zkl.scienceFX.wave.fx

import javafx.geometry.Rectangle2D
import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.aparapi.WaveWorldAparapi
import zkl.scienceFX.wave.physics.simple.WaveWorldSimple
import java.io.File
import java.util.*

val DEFAULT_CONF_FOR_VIDEO = ConfsForVideo.zonePlate()

/**
 * 用于拍摄视频的Conf
 */
object ConfsForVideo{
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun firstOutput() = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount=100
			}
			sinInvokeConf { }
		}
		visualConf{
			canvasWidth = 4000.0
			canvasHeight = 1000.0
			painter = LinePainter()
		}
		exportConf {
			exportDir = File("D:/scienceFX/firstOutput")
			exportTimeRange = 0.0f..100.0f
		}
	}
	
	/**
	 * 波的独立传播
	 */
	fun independentPropagation() = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount=200
				defaultUnitMass = 1.1f
				groupedInvokeConf(
					sinInvokeConf {
						invokeUnitId = 0
					},
					sinInvokeConf {
						invokeUnitId = 0
						invokeScale *= 2.5f
						invokeDelay = invokePeriod*2.0f/3.0f
					},
					sinInvokeConf {
						invokeUnitId = unitCount - 1
						invokeScale *= -1.5f
					}
				)
			}
		}
		visualConf{
			canvasWidth = 4000.0
			canvasHeight = 1500.0
			painter = LinePainter()
		}
		exportConf {
			exportDir = File("D:/scienceFX/independentPropagation")
			exportTimeRange = 50f..400f
		}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia1() = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount = 140
				onDraftUnit = { unitId,unit->
					if (unitId > unitCount / 2) {
						unit.mass *= 10.0f
						unit.color = Color.DARKTURQUOISE
					}
					unit
				}
			}
			sinInvokeConf {  }
		}
		visualConf{
			canvasWidth = 2400.0
			canvasHeight = 1200.0
			painter = LinePainter(1.3)
			drawAera = Rectangle2D(100.0-canvasWidth/2.0,0.0,canvasWidth*2.0,canvasHeight)
		}
		exportConf {
			exportDir = File("D:/scienceFX/multiMedia1")
			exportTimeRange = 0f..300f
			
		}
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia2() = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount = 140
				onDraftUnit = { unitId,unit->
					if (unitId < unitCount / 2) {
						unit.mass *= 10.0f
						unit.color = Color.DARKTURQUOISE
					}
					unit
				}
			}
			sinInvokeConf {  }
		}
		visualConf{
			canvasWidth = 2000.0
			canvasHeight = 1500.0
			painter = LinePainter(1.5)
			drawAera = Rectangle2D(-canvasWidth/2.0,0.0,canvasWidth*2.0,canvasHeight)
		}
		exportConf {
			exportDir = File("D:/scienceFX/multiMedia1")
			exportTimeRange = 0f..300f
			
		}
	}
	
	/**
	 * 方波和阻尼吸收
	 */
	fun squareWave() = conf {
		physicsConf{
			val absorbThick:Int=200
			val absorbDamping:Float = 0.05f
			
			world = WaveWorldSimple()
			timeUnit = 0.5f
			processCount = 6
			lineDraftConf{
				unitCount = 420
				onDraftUnit={unitId,unit->
					val startIndex = unitCount-absorbThick
					if (unitId == unitCount) {
						unit.setAsWall()
					}
					else if (unitId > startIndex) {
						val rate:Float = (unitId-startIndex).toFloat()/absorbThick
						unit.damping = defaultDamping+absorbDamping*rate
						unit.color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(),1.0-rate)
					}
					unit
				}
			}
			squareInvokeConf {
				invokeScale = 150f
				invokePeriod = 240f
				invokeRepeat = 2f
			}
		}
		visualConf {
			canvasWidth = 1800.0
			canvasHeight = 600.0
			painter = LinePainter()
		}
		exportConf {
			exportDir = File("D:/scienceFX/squareWave")
			exportTimeRange = 0f..1300f
		}
	}
	
	/**
	 * 拍频
	 */
	fun beatFrequency() = conf {
		physicsConf {
			world = WaveWorldSimple()
			timeUnit = 0.47f
			lineDraftConf {
				unitCount=1000
				groupedInvokeConf(
					cosInvokeConf {
						invokeUnitId = 0
						invokePeriod = 50f
						invokeRepeat = 15f
						invokeScale=10f
					},
					cosInvokeConf {
						invokeUnitId = unitCount - 1
						invokePeriod = 55f
						invokeRepeat = 14f
						invokeScale=-10f*50f/55f
					}
				)
			}
		}
		visualConf{
			painter = LinePainter()
			canvasWidth = 2000.0
			canvasHeight = 1200.0
			drawAera = Rectangle2D(-canvasWidth/2.0,0.0,canvasWidth*2.0,canvasHeight)
		}
		exportConf {
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
	fun simple2D() = conf2D {
		physicsConf2D {
			world = WaveWorldSimple()
			columnCount = 200
			rowCount = 150
			timeUnit = 0.5f
			
			rectDraftConf {
//				defaultDamping=0.01f
			}
			dynamicInvokeConf {
				groupedInvokeConf(
					sinInvokeConf {
						invokeUnitId = getUnitId(0, 0)
//						invokeDelay = 50f
						invokePeriod = 80.0f
						invokeRepeat = 3f
					}
				)
			}
			
		}
		visualConf2D {
			painter=WaterSurfacePainter()
		}
		exportConf {
			exportDir = File("D:/scienceFX/simple2D")
			exportPrefix = "simple2D_"
			exportTimeRange = 0f..1300f
		}
	}
	
	/**
	 * 衍射
	 */
	fun diffraction() = conf2D {
		physicsConf2D {
			rowCount = 300
			columnCount = 400
			val wavelength = 20.0
			val slitWidth = wavelength * 3.0
			val sourceUnitId = getUnitId(rowCount / 2 + 3, columnCount / 5)
			
			world = WaveWorldAparapi()
			boarderAbsorbedDraftConf {
				onDraftUnit = { row, column, unit ->
					if (column == columnCount / 2 && !(row > rowCount / 2 - slitWidth / 2 && row < rowCount / 2 + slitWidth / 2)) {
						unit.mass = Float.MAX_VALUE
						unit.color = Color.RED
					}
					unit
				}
				
				this@physicsConf2D.cosInvokeConf {
					invokeUnitId = sourceUnitId
					invokePeriod = (wavelength / Math.sqrt(defaultLinkStrength.toDouble())).toFloat()
					invokeRepeat = 100f
				}
			}
			
		}
		visualConf2D { painter = WaveEnergyPainter(60f) }
		exportConf {
			exportDir = File("D:/scienceFX/diffraction")
			exportPrefix = "diffraction_"
			exportTimeRange = 0f..1000f
			isAutoModeOn = true
		}
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf2D {
		
		physicsConf2D {
			rowCount = 200
			columnCount = 200
			val sourceUnitId1 = getUnitId(rowCount / 3, columnCount / 3)
			val sourceUnitId2 = getUnitId(rowCount / 3+30, rowCount / 3+33)
			
			world = WaveWorldAparapi()
			boarderAbsorbedDraftConf {}
			
			var invokeCount = 0
			val invokeConf1 = cosInvokeConf {
				invokeUnitId = sourceUnitId1
				invokePeriod = 30f
				invokeRepeat = 100f
				invokeScale = 10f
			}
			val invokeConf2 = cosInvokeConf {
				invokeUnitId = sourceUnitId2
				invokePeriod = 30f
				invokeRepeat = 50f
				invokeScale = 10f
			}
			dynamicInvokeConf {
				invokeCount++
				if (invokeCount % 2 == 1) invokeConf1 else invokeConf2
			}
			
		}
		visualConf2D {
			painter = WaveEnergyPainter(7f)
		}
		exportConf {
			exportDir = File("D:/scienceFX/interference")
			exportPrefix = "interference_"
			exportTimeRange = 0f..1000f
			isAutoModeOn = true
		}
	}
	
	/**
	 * 波带片
	 */
	fun zonePlate() = conf2D {
		physicsConf2D {
			rowCount = 250
			columnCount = 400
			
			/**
			 * 波源和接收点到波带片的距离
			 */
			val distance: Float = 53.0f
			val wavelength: Float = 21.0f
			
			val sourceUnitId = getUnitId(rowCount / 2, columnCount / 2 - distance.toInt())
			
			world = WaveWorldAparapi()
			timeUnit = 0.1f
			processCount = 10
			boarderAbsorbedDraftConf {
				val waveSpeed = Math.sqrt((defaultLinkStrength / defaultUnitMass).toDouble()).toFloat()
				val zeroPoints = ArrayList<Int>().apply {
					//此句应放在rowCount等句的后面
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
				onDraftUnit = { row, column, unit ->
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
							unit.setAsWall()
						}
					}
					unit
				}
				
				cosInvokeConf {
					invokeUnitId = sourceUnitId
					invokePeriod = wavelength / waveSpeed
					invokeRepeat = 1000f
				}
				
			}
			
		}
		visualConf2D {
			painter = WaveEnergyPainter(15f)
		}
		exportConf {
			exportDir = File("D:/scienceFX/zonePlate")
			exportPrefix = "zonePlate_"
			exportTimeRange = 0f..800f
			isAutoModeOn = false
		}
	}
	
}