package zkl.scienceFX.wave

import javafx.scene.paint.Color
import zkl.scienceFX.wave.conf.*
import zkl.scienceFX.wave.fx.*
import java.util.*
import kotlin.math.PI

val DEFAULT_CONF get() = DEFAULT_CONF_FOR_VIDEO
//val DEFAULT_CONF = Confs.diffraction()

object Confs {
	
	//一维
	
	/**
	 * 简单的一维示例
	 */
	fun simpleLine() = conf {
		physics {
			simpleWaveWorld()
			lineDraft { }
			sinInvoke { }
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 不同介质中传播
	 */
	fun multiMedia(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		physics {
			simpleWaveWorld()
			lineDraft {
				onCreateUnitDraft { index ->
					if ((index > unitCount / 2) == direction) {
						mass *= massScale
						color = Color.DARKTURQUOISE
					}
				}
			}
			sinInvoke { }
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 共振
	 */
	fun resonate(doResonate: Boolean = true) = conf {
		physics {
			val wavePeriod = 40f
			val resonateCount = 6
			
			simpleWaveWorld()
			lineDraft {
				val waveSpeed = Math.sqrt((defaultLinkStrength / defaultUnitMass).toDouble())
				val waveLength = waveSpeed * wavePeriod
				
				unitCount = Math.round(waveLength * resonateCount / 2.0).toInt() + 1
				if (!doResonate) unitCount += 4   //加上4就不共振了
			}
			cosInvoke {
				period = wavePeriod
				repeat = 1000f
			}
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 阻尼
	 */
	fun damping() = conf {
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 100
				defaultDamping = 0.02f
			}
			cosInvoke { }
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 不同阻尼中传播
	 */
	fun multiDamping(direction: Boolean = true, theDamping: Float = 0.05f) = conf {
		physics {
			simpleWaveWorld()
			lineDraft {
				unitCount = 80
				onCreateUnitDraft { index ->
					if (direction && index > unitCount * 3 / 4) {
						damping = theDamping
						color = Color.GREEN
					} else if (!direction && index < unitCount / 4) {
						damping = theDamping
						color = Color.GREEN
					}
				}
			}
			sinInvoke {
				scale = 70f
				period = 20f
			}
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 阻尼吸收
	 */
	fun dampingAbsorb() = conf {
		physics {
			simpleWaveWorld()
			lineDraft {
				val thick = 40
				val maxDamping = 0.3f
				
				val startIndex = unitCount - thick
				onCreateUnitDraft { index ->
					if (index == unitCount) {
						setAsWall()
					} else if (index > startIndex) {
						val rate: Float = (index - startIndex).toFloat() / thick
						damping = defaultDamping + maxDamping * rate
						color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
				}
			}
			sinInvoke { }
		}
		visual { painter = LinePainter() }
	}
	
	/**
	 * 方波
	 */
	fun squareWave() = conf {
		physics {
			timeUnit = 0.5f
			processCount = 5
			simpleWaveWorld()
			lineDraft {
				unitCount = 800
				val absorbThick = 400
				val absorbDamping = 0.01f
				
				onCreateUnitDraft { unitId ->
					val startIndex = unitCount - absorbThick
					if (unitId == unitCount) {
						setAsWall()
					} else if (unitId > startIndex) {
						val rate: Float = (unitId - startIndex).toFloat() / absorbThick
						damping = defaultDamping + absorbDamping * rate
						color = colorMix(Color.GREEN, Color.WHITE, rate.toDouble(), 1.0 - rate)
					}
				}
			}
			squareInvoke {
				scale = 150f
				period = 400f
				repeat = 10f
			}
		}
		visual { painter = LinePainter() }
		
	}
	
	/**
	 * 脉冲
	 */
	fun impact() = conf {
		physics {
			simpleWaveWorld()
			timeUnit = 0.01f
			lineDraft {
				unitCount = 300
				
				sinInvoke {
					targetUnitId = unitCount / 2
					period = 1.0f
					scale = 5000f
				}
			}
			
		}
		visual { painter = LinePainter() }
	}
	
	
	//二维
	
	/**
	 * 简单的二维示例
	 */
	fun simpleRect() = conf {
		physics {
			simpleWaveWorld()
			rectDraft {
				rowCount = 200
				columnCount = 200
			}
			cosInvoke {
				period = 20f
			}
		}
		visualConf2D { painter = ColorOffsetPainter() }
	}
	
	/**
	 * 水面模拟
	 */
	fun waterSurface() = conf {
		physics {
			simpleWaveWorld()
			timeUnit = 0.5f
			rectDraft {
				rowCount = 200
				columnCount = 200
				defaultDamping = 0.01f
				
				this@physics.onInvoke {
					SinInvokeConf().apply {
						targetUnitId = getUnitId(
							row = (rowCount * Math.random()).toInt(),
							column = (columnCount * Math.random()).toInt())
						period = 40.0f
						repeat = 3f
					}
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
	fun multiMedia2D(direction: Boolean = true, massScale: Float = 5.0f) = conf {
		physics {
			timeOffset = 320f
			aparapiWaveWorld()
			rectBoarderAbsorbedDraft {
				rowCount = 300
				columnCount = 300
				val sourceUnitId1 = getUnitId(rowCount / 3, columnCount / 3)
				val sourceUnitId2 = getUnitId(rowCount / 3, columnCount / 3 + 30)
				
				onCreateUnitDraft { row, _ ->
					if ((row > rowCount / 2) == direction) {
						mass *= massScale
					}
					if (row == rowCount / 2) {
						color = Color.YELLOW
					}
				}
				
				this@physics.cosInvoke {
					targetUnitId = sourceUnitId1
					period = 20f
					repeat = 100f
					scale = 5f
				}
				this@physics.cosInvoke {
					targetUnitId = sourceUnitId2
					period = 20f
					repeat = 100f
					scale = 5f
				}
			}
		}
		visualConf2D {
			painter = WaveEnergyPainter()
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
		visualConf2D { painter = WaveEnergyPainter(30f) }
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
				val sourceUnitId2 = getUnitId(rowCount / 2, columnCount / 2 + 3)
				
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
			painter = WaveEnergyPainter()
		}
	}
	
	/**
	 * 波束 & 反射和折射 【计算量大！！】
	 */
	fun planeWave() = conf {
		physics {
			
			
			aparapiWaveWorld()
			timeOffset = 4000f
			processCount = 100
			timeUnit = 1f
			
			rectBoarderAbsorbedDraft {
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
				
				//defaultDamping=0.3f
				onCreateUnitDraft { row, column ->
					val x = column - absorbThick
					val y = Math.round(row - rowCount / 2.0).toInt()
					
					if (x in 0..l) {
						if (x == Math.round(y * y / 4.0 / s).toInt()
							|| y == Math.round(Math.sqrt(x * 4.0 * s)).toInt()
							|| y == Math.round(-Math.sqrt(x * 4.0 * s)).toInt()) {
							setAsWall()
						}
					}
					//if ((row-rowCount/2)<2*(column-columnCount/2)) waveUnit.mass *= 2.0f
				}
				
				this@physics.cosInvoke {
					targetUnitId = getUnitId(rowCount / 2, s + absorbThick)
					repeat = 1000f
					period = 20f
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
	fun zonePlate() = conf {
		physics {
			aparapiWaveWorld()
			timeOffset = 320f
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
			painter = WaveEnergyPainter()
		}
	}
	
}

