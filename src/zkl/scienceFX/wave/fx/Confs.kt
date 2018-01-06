package zkl.scienceFX.wave.fx

import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.aparapi.WaveWorldAparapi
import zkl.scienceFX.wave.physics.simple.WaveWorldSimple
import java.util.*

val DEFAULT_CONF get() = DEFAULT_CONF_FOR_VIDEO
//val DEFAULT_CONF = Confs.diffraction()

object Confs {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun simpleLine() = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf { }
			sinInvokeConf { }
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				onDraftUnit = { unitId, unit ->
					if ((unitId > unitCount / 2) == direction) {
						unit.mass *= massScale
						unit.color = Color.DARKTURQUOISE
					}
					unit
				}
			}
			sinInvokeConf { }
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 共振
	 */
	fun resonate(doResonate: Boolean = true) = conf {
		physicsConf {
			val wavePeriod = 40f
			val resonateCount = 6
			
			world = WaveWorldSimple()
			lineDraftConf {
				val waveSpeed = Math.sqrt((defaultLinkStrength / defaultUnitMass).toDouble())
				val waveLength = waveSpeed * wavePeriod
				
				unitCount = Math.round(waveLength * resonateCount / 2.0).toInt() + 1
				if (!doResonate) unitCount += 4   //加上4就不共振了
				
			}
			cosInvokeConf {
				invokePeriod = wavePeriod
				invokeRepeat = 1000f
			}
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 阻尼
	 */
	fun damping() = conf {
		physicsConf {
			world = WaveWorldSimple()
			draftConf = lineDraftConf {
				unitCount = 100
				defaultDamping = 0.02f
			}
			cosInvokeConf { }
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 不同阻尼中传播
	 */
	fun multiDamping(direction: Boolean = true, damping: Float = 0.05f) = conf {
		physicsConf {
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount = 80
				onDraftUnit = { unitId, unit ->
					if (direction && unitId > unitCount * 3 / 4) {
						unit.damping = damping
						unit.color = Color.GREEN
					} else if (!direction && unitId < unitCount / 4) {
						unit.damping = damping
						unit.color = Color.GREEN
					}
					unit
				}
			}
			sinInvokeConf {
				invokeScale = 70f
				invokePeriod = 20f
			}
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 阻尼吸收
	 */
	fun dampingAbsorb() = conf {
		physicsConf {
			val thick: Int = 40
			val maxDamping: Float = 0.3f
			
			world = WaveWorldSimple()
			lineDraftConf {
				val startIndex = unitCount - thick
				onDraftUnit = { unitId, unit ->
					if (unitId == unitCount) {
						unit.setAsWall()
					} else if (unitId > startIndex) {
						val rate: Float = (unitId - startIndex).toFloat() / thick
						unit.damping = defaultDamping + maxDamping * rate
						unit.color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
					unit
				}
			}
			sinInvokeConf { }
		}
		visualConf { painter = LinePainter() }
	}
	
	/**
	 * 方波
	 */
	fun squareWave() = conf {
		physicsConf {
			val absorbThick: Int = 400
			val absorbDamping: Float = 0.01f
			
			timeUnit = 0.5f
			processCount = 5
			world = WaveWorldSimple()
			lineDraftConf {
				unitCount = 800
				onDraftUnit = { unitId, unit ->
					val startIndex = unitCount - absorbThick
					if (unitId == unitCount) {
						unit.setAsWall()
					} else if (unitId > startIndex) {
						val rate: Float = (unitId - startIndex).toFloat() / absorbThick
						unit.damping = defaultDamping + absorbDamping * rate
						unit.color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
					unit
				}
			}
			squareInvokeConf {
				invokeScale = 150f
				invokePeriod = 400f
				invokeRepeat = 10f
			}
		}
		visualConf { painter = LinePainter() }
		
	}
	
	/**
	 * 脉冲
	 */
	fun impact() =  conf {
		physicsConf {
			world = WaveWorldSimple()
			timeUnit = 0.01f
			lineDraftConf {
				unitCount = 300
				
				sinInvokeConf {
					invokeUnitId = unitCount / 2
					invokePeriod = 1.0f
					invokeScale = 5000f
				}
			}
			
		}
		visualConf { painter = LinePainter() }
	}
	
	
	//二维
	
	/**
	 * 简单的二维示例
	 */
	fun simpleRect() = conf2D {
		physicsConf2D {
			rowCount = 200
			columnCount = 200
			world = WaveWorldSimple()
			rectDraftConf {}
			cosInvokeConf {
				invokePeriod = 20f
			}
		}
		visualConf2D { painter = ColorOffsetPainter() }
	}
	
	/**
	 * 水面模拟
	 */
	fun waterSurface() = conf2D {
		physicsConf2D {
			world = WaveWorldSimple()
			rowCount = 200
			columnCount = 200
			timeUnit = 0.5f
			
			rectDraftConf {
				defaultDamping = 0.01f
			}
			dynamicInvokeConf {
				sinInvokeConf {
					invokeUnitId = getUnitId(
						row = (rowCount * Math.random()).toInt(),
						column = (columnCount * Math.random()).toInt())
					invokePeriod = 40.0f
					invokeRepeat = 3f
				}
			}
			
		}
		visualConf2D {
			painter = WaterSurfacePainter()
		}
	}
	
	/**
	 * 不同介质中的传播
	 */
	fun multiMedia2D(direction: Boolean = true, massScale: Float = 5.0f) = conf2D {
		
		physicsConf2D {
			rowCount = 300
			columnCount = 300
			val sourceUnitId1 = getUnitId(rowCount / 3, columnCount / 3)
			val sourceUnitId2 = getUnitId(rowCount / 3, columnCount / 3 + 30)
			
			world = WaveWorldAparapi()
			timeOffset = 320f
			boarderAbsorbedDraftConf {
				onDraftUnit = { row, _, unit ->
					if ((row > rowCount / 2) == direction) {
						unit.mass *= massScale
					}
					if (row == rowCount / 2) {
						unit.color = Color.YELLOW
					}
					unit
				}
			}
			
			groupedInvokeConf(
				cosInvokeConf {
					invokeUnitId = sourceUnitId1
					invokePeriod = 20f
					invokeRepeat = 100f
					invokeScale = 5f
				},
				cosInvokeConf {
					invokeUnitId = sourceUnitId2
					invokePeriod = 20f
					invokeRepeat = 100f
					invokeScale = 5f
				}
			)
		}
		visualConf2D {
			painter = WaveEnergyPainter()
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
		visualConf2D { painter = WaveEnergyPainter(30f) }
	}
	
	/**
	 * 干涉
	 */
	fun interference() = conf2D {
		
		physicsConf2D {
			rowCount = 200
			columnCount = 200
			val sourceUnitId1 = getUnitId(rowCount / 3, columnCount / 3)
			val sourceUnitId2 = getUnitId(rowCount / 2, columnCount / 2 + 3)
			
			world = WaveWorldAparapi()
			boarderAbsorbedDraftConf {}
			
			var invokeCount = 0
			val invokeConf1 = cosInvokeConf {
				invokeUnitId = sourceUnitId1
				invokePeriod = 20f
				invokeRepeat = 100f
				invokeScale = 5f
			}
			val invokeConf2 = cosInvokeConf {
				invokeUnitId = sourceUnitId2
				invokePeriod = 20f
				invokeRepeat = 50f
				invokeScale = 5f
			}
			dynamicInvokeConf {
				invokeCount++
				if (invokeCount % 2 == 1) invokeConf1 else invokeConf2
			}
			
		}
		visualConf2D {
			painter = WaveEnergyPainter()
		}
	}
	
	/**
	 * 波束 & 反射和折射 【计算量大！！】
	 */
	fun planeWave() = conf2D {
		physicsConf2D {
			rowCount = 600
			columnCount = 2500
			
			/**
			 * 凹面反射镜的焦距（s越小就越越弯）
			 */
			val s = 30
			/**
			 * 凹面反射镜在水平方向上的长度
			 */
			val l = 150
			
			world = WaveWorldAparapi()
			timeOffset = 4000f
			processCount = 100
			timeUnit = 1f
			
			boarderAbsorbedDraftConf {
				rowCount = this@physicsConf2D.rowCount
				columnCount = this@physicsConf2D.columnCount
				//defaultDamping=0.3f
				
				onDraftUnit = { row, column, waveUnit ->
					val x = column - absorbThick
					val y = Math.round(row - rowCount / 2.0).toInt()
					
					if (x in 0..l) {
						if (x == Math.round(y * y / 4.0 / s).toInt()
							|| y == Math.round(Math.sqrt(x * 4.0 * s)).toInt()
							|| y == Math.round(-Math.sqrt(x * 4.0 * s)).toInt()) {
							
							waveUnit.setAsWall()
							
						}
					}
					
					//if ((row-rowCount/2)<2*(column-columnCount/2)) waveUnit.mass *= 2.0f
					
					waveUnit
				}
				
				this@physicsConf2D.cosInvokeConf {
					invokeUnitId = getUnitId(rowCount / 2, s + absorbThick)
					invokeRepeat = 1000f
					invokePeriod = 20f
				}
			}
			
		}
		visualConf2D {
			samplingSize = 2.0
			painter = WaveEnergyPainter()
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
			timeOffset = 320f
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
			painter = WaveEnergyPainter()
		}
	}
	
}

