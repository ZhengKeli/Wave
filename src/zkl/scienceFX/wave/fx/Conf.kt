package zkl.scienceFX.wave.fx

import javafx.geometry.Rectangle2D
import javafx.scene.paint.Color
import zkl.scienceFX.wave.physics.abstracts.*
import java.io.File
import java.util.*


fun conf(body:Conf.()->Unit) = Conf().also { it.body() }
open class Conf {
	
	lateinit var physicsConf:PhysicsConf
	open fun physicsConf(body:PhysicsConf.()->Unit)=PhysicsConf().also {
		body.invoke(it)
		physicsConf=it
	}
	open class PhysicsConf {
		
		//属性
		lateinit var world: WaveWorld
		var timeUnit: Float = 0.2f
		var timeOffset:Float = 0.0f
		var processCount: Int = 5
		
		
		//draftConf
		lateinit var draftConf: DraftConf
		inline fun draftConf(body:DraftConf.()->Unit) = DraftConf(this).also {
			body.invoke(it)
			draftConf=it
		}
		inline fun lineDraftConf(body:LineDraftConf.()->Unit) = LineDraftConf(this).also {
			body.invoke(it)
			draftConf=it
		}
		
		open class DraftConf(val upperConf: PhysicsConf) {
			
			//world
			val processUnit: Float = 0.2f
			
			//unit
			var defaultUnitMass = 1.0f
			var defaultDamping = 0.0f
			var defaultUnitColor: Color?=null
			fun WaveUnitDraft.setAsWall(){
				this.mass = Float.MAX_VALUE
				this.color = Color.RED
			}
			
			//link
			var defaultLinkStrength = 0.3f
			
			//draft
			var onDraftWorld: (DraftConf.(WaveWorldDraft) -> WaveWorldDraft)? =null
			open fun draftWorld() = WaveWorldDraft().let {
				onDraftWorld?.invoke(this,it)?:it
			}
			
		}
		open class LineDraftConf(upperConf: PhysicsConf) : DraftConf(upperConf) {
			//修改默认值
			init {
				defaultUnitColor = Color.WHITE
			}
			
			var unitCount: Int = 100
			
			override fun draftWorld(): WaveWorldDraft {
				val draft=WaveWorldDraft()
				draft.units=Array<WaveUnitDraft>(unitCount){index-> draftUnit(index)}.asList()
				draft.links=Array<WaveLinkDraft>(unitCount-1){ index-> draftLink(index, index+1) }.asList()
				return onDraftWorld?.invoke(this,draft)?:draft
			}
			
			var onDraftUnit: (LineDraftConf.(unitId: Int, unit: WaveUnitDraft) -> WaveUnitDraft)? = null
			open fun draftUnit(unitId: Int): WaveUnitDraft
				= WaveUnitDraft(mass = defaultUnitMass, damping = defaultDamping).let {
				it.color=defaultUnitColor
				onDraftUnit?.invoke(this,unitId,it)?:it
			}
			
			var onDraftLink: (LineDraftConf.(unitId1: Int, unitId2: Int, link: WaveLinkDraft) -> WaveLinkDraft)? = null
			open fun draftLink(unitId1: Int, unitId2: Int)
				= WaveLinkDraft(unitId1, unitId2, defaultLinkStrength).let {
				onDraftLink?.invoke(this,unitId1,unitId2,it)?:it
			}
			
		}
		
		
		//invokeConf
		var invokeConf: InvokeConf?=null
		inline fun customInvokeConf(crossinline onInvoke: InvokeConf.(world: WaveWorld)->Unit)
			= object : InvokeConf() {
			override fun invoke(world: WaveWorld) {
				onInvoke(world)
			}
		}.also { invokeConf = it }
		inline fun sinInvokeConf(body:InvokeConf.()->Unit)
			= customInvokeConf { world ->
			world.addInvoker(
				SinWaveInvoker(world.time+invokeDelay,InvokeType.force, invokeUnitId, invokeScale, invokePeriod, invokeRepeat, invokePhase)
			)
		}.also {
			body.invoke(it)
			invokeConf = it
		}
		inline fun cosInvokeConf(body:InvokeConf.()->Unit)
			= customInvokeConf { world ->
			world.addInvoker(
				SinWaveInvoker(world.time+invokeDelay,InvokeType.force, invokeUnitId, invokeScale, invokePeriod, invokeRepeat,
					invokePhase + (Math.PI/2.0).toFloat())
			)
		}.also {
			body.invoke(it)
			invokeConf = it
		}
		inline fun positionSinInvokeConf(body:InvokeConf.()->Unit)
			= customInvokeConf { world ->
			world.addInvoker(
				SinWaveInvoker(world.time+invokeDelay,InvokeType.position, invokeUnitId, invokeScale, invokePeriod, invokeRepeat,
					invokePhase)
			)
		}.also {
			body.invoke(it)
			invokeConf = it
		}
		inline fun positionCosInvokeConf(body:InvokeConf.()->Unit)
			= customInvokeConf { world ->
			world.addInvoker(
				SinWaveInvoker(world.time+invokeDelay,InvokeType.position, invokeUnitId, invokeScale, invokePeriod, invokeRepeat,
					invokePhase + (Math.PI/2.0).toFloat())
			)
		}.also {
			body.invoke(it)
			invokeConf = it
		}
		inline fun squareInvokeConf(body:InvokeConf.()->Unit)
			= customInvokeConf { world ->
			world.addInvoker(SquareWaveInvoker(world.time+invokeDelay,invokeUnitId, invokeScale, invokePeriod, invokeRepeat))
		}.also {
			body.invoke(it)
			invokeConf = it
		}
		fun dynamicInvokeConf(getter:()->InvokeConf)= customInvokeConf {
			getter().invoke(it)
			invokeConf = this
		}
		fun groupedInvokeConf(vararg invokeConfs:InvokeConf) = customInvokeConf { world->
			invokeConfs.forEach { subInvokeConf-> subInvokeConf.invoke(world) }
		}
		
		abstract class InvokeConf {
			var invokeUnitId: Int = 0
			var invokeScale: Float = 10.0f
			var invokePeriod: Float = 40.0f
			var invokeRepeat: Float = 1.0f
			var invokePhase: Float = 0.0f
			var invokeDelay:Float = 0.0f
			
			abstract fun invoke(world: WaveWorld)
		}
		
		
	}
	
	
	lateinit var visualConf:VisualConf
	open fun visualConf(body:VisualConf.()->Unit)=VisualConf().also{
		body.invoke(it)
		visualConf=it
	}
	open class VisualConf {
		
		lateinit var painter: WavePainter
		var canvasWidth: Double = 4000.0
		var canvasHeight: Double = 3000.0
		
		var drawAera:Rectangle2D? = null
		
		var framePeriod: Long = 40 //fps = 25
		var fps:Double
			get() = 1000.0/framePeriod
			set(value){ framePeriod = Math.round(1000.0/value) }
		
	}
	
	
	var exportConf: ExportConf? = null
	open fun exportConf(body: ExportConf.() -> Unit) = ExportConf().also{
		body.invoke(it)
		exportConf = it
	}
	class ExportConf {
		lateinit var exportDir: File
		var exportPrefix: String = ""
		
		var exportViewPort: Rectangle2D? = null
		
		var isAutoModeOn = false
		var exportTimeRange:ClosedRange<Float>? = null
	}
	
}

fun conf2D(body:Conf2D.()->Unit) = Conf2D().also { it.body() }
open class Conf2D: Conf() {
	
	open fun physicsConf2D(body:PhysicsConf2D.()->Unit)=PhysicsConf2D().also {
		body.invoke(it)
		physicsConf=it
	}
	open class PhysicsConf2D:PhysicsConf(){
		var rowCount:Int = 100
		var columnCount:Int = 100
		fun getUnitId(row: Int, column: Int) = row * columnCount + column
		
		inline fun rectDraftConf(body:RectDraftConf.()->Unit) = RectDraftConf(this).also {
			body.invoke(it)
			draftConf=it
		}
		inline fun boarderAbsorbedDraftConf(body:BoarderAbsorbedDraftConf.()->Unit)
			= BoarderAbsorbedDraftConf(this).also {
			body.invoke(it)
			draftConf=it
		}
		
		open class RectDraftConf(upperConf: PhysicsConf2D) : DraftConf(upperConf) {
			var rowCount: Int = upperConf.rowCount
			var columnCount: Int = upperConf.columnCount
			fun getUnitId(row: Int, column: Int) = row * columnCount + column
			
			override fun draftWorld(): WaveWorldDraft {
				val draft=WaveWorldDraft()
				draft.units = object : AbstractList<WaveUnitDraft>() {
					override val size = rowCount * columnCount
					override fun get(index: Int) = draftUnit(index / columnCount, index % columnCount)
					
				}
				draft.links = object : AbstractList<WaveLinkDraft>() {
					override val size: Int
						get() = rowCount * (columnCount - 1) + columnCount * (rowCount - 1)
					
					//横向的链接和纵向连接的分界线（纵向连接的开始处）
					val s1 = rowCount * (columnCount - 1)
					
					override fun get(index: Int): WaveLinkDraft {
						val row1: Int
						val column1: Int
						val row2: Int
						val column2: Int
						when {
							index < s1 -> {
								row1 = index % rowCount
								column1 = index / rowCount
								row2 = row1
								column2 = column1 + 1
							}
							else -> {
								row1 = (index - s1) / columnCount
								column1 = (index - s1) % columnCount
								row2 = row1 + 1
								column2 = column1
							}
						}
						return draftLink(getUnitId(row1, column1), getUnitId(row2, column2))
					}
					
				}
				return onDraftWorld?.invoke(this,draft)?:draft
			}
			
			var onDraftUnit: (RectDraftConf.(row: Int, column: Int, unit: WaveUnitDraft) -> WaveUnitDraft)? = null
			open fun draftUnit(row: Int, column: Int): WaveUnitDraft
				= WaveUnitDraft(mass = defaultUnitMass,damping = defaultDamping).let {
				onDraftUnit?.invoke(this,row,column,it)?:it
			}
			
			var onDraftLink: (RectDraftConf.(unitId1: Int, unitId2: Int, link: WaveLinkDraft) -> WaveLinkDraft)? = null
			open fun draftLink(unitId1: Int, unitId2: Int): WaveLinkDraft
				= WaveLinkDraft(unitId1, unitId2, defaultLinkStrength).let {
				onDraftLink?.invoke(this,unitId1,unitId2,it)?:it
			}
			
		}
		open class BoarderAbsorbedDraftConf(upperConf: PhysicsConf2D) : RectDraftConf(upperConf) {
			val absorbThick: Int = 30
			var absorbDamping: Float = 0.2f
			override fun draftUnit(row: Int, column: Int): WaveUnitDraft {
				val unit = WaveUnitDraft(mass = defaultUnitMass,damping = defaultDamping)
				fun setAsSoftWall(rate:Float){
					unit.damping=defaultDamping+absorbDamping*rate
					//waveUnit.visualExtra2D.color = Color.GREEN
				}
				when{
					row<absorbThick && column<absorbThick ->{
						val rate = Math.max(absorbThick - row, absorbThick - column).toFloat()/absorbThick
						setAsSoftWall(rate)
					}
					row>rowCount-absorbThick && column>columnCount-absorbThick ->{
						val rate = Math.max(row-(rowCount-absorbThick), column-(columnCount-absorbThick)).toFloat()/absorbThick
						setAsSoftWall(rate)
					}
					row<absorbThick -> setAsSoftWall((absorbThick - row).toFloat()/absorbThick)
					column<absorbThick->setAsSoftWall((absorbThick - column).toFloat()/absorbThick)
					row>rowCount-absorbThick -> setAsSoftWall((row-(rowCount-absorbThick)).toFloat()/absorbThick)
					column>columnCount-absorbThick -> setAsSoftWall((column-(columnCount-absorbThick)).toFloat()/absorbThick)
				}
				return onDraftUnit?.invoke(this,row,column,unit)?:unit
			}
		}
		
	}
	
	
	fun visualConf2D(body: VisualConf2D.() -> Unit): VisualConf2D {
		return VisualConf2D().also { visualConf2D ->
			body.invoke(visualConf2D)
			(physicsConf as? PhysicsConf2D)?.let { physicsConf2D ->
				visualConf2D.canvasHeight = physicsConf2D.rowCount.toDouble()/visualConf2D.samplingSize
				visualConf2D.canvasWidth = physicsConf2D.columnCount.toDouble()/visualConf2D.samplingSize
			}
			visualConf=visualConf2D
		}
	}
	open class VisualConf2D:VisualConf(){
		var samplingSize:Double=1.0
	}
	
	
}



