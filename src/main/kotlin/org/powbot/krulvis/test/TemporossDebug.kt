package org.powbot.krulvis.test

import org.powbot.api.Color.BLACK
import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.ORANGE
import org.powbot.api.Color.RED
import org.powbot.api.InteractableEntity
import org.powbot.api.Nameable
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.Leaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.input.Touchscreen

@ScriptManifest(name = "Tempoross - Debug", description = "Some testing", version = "1.0")
class TemporossDebug : ATScript() {

    lateinit var tempoross: Tempoross

    var bucket: GameObject? = null
    var bossPool: Npc? = null
    var ammo: Npc? = null
    var tether: GameObject? = null
    var cookSpot = Tile.Nil


    fun checkPaths(vararg entities: InteractableEntity?) {
        entities.forEach {
            if (it != null) {
                tempoross.hasDangerousPath(it.tile())
            }
        }
    }

    override fun createPainter(): ATPaint<*> = TemporossDebugPainter(this)

    fun debugComp(c: Component?) {
        if (c == null) {
            log.info("Comp == null")
            return
        }
        val parent = c.parent()
        log.info("Widget=[${c.widgetId()}], Parent=[${parent} id=${c.parentId()}], $c, Hidden=${c.hidden()}, Valid=${c.valid()}, Text=${c.text()}, Boundsindex=${c.boundsIndex()}")
        if (parent != null) debugComp(parent)
    }

    override val rootComponent: TreeComponent<*> = object : Leaf<TemporossDebug>(this, "TestLeaf") {
        override fun execute() {
//            val c =
//                Components.stream(Data.PARENT_WIDGET).filtered { it.text().contains("Energy") }.firstOrNull()
//            debugComp(c)
            if (tempoross.side == Side.UNKNOWN) {
                log.info("Figuring out side...")
                if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
                    val mast = Objects.stream().name("Mast").nearest().first()
                    log.info("Mast found: $mast, orientation: ${mast.orientation()}")
                    tempoross.side = if (mast.orientation() == 4) Side.SOUTH else Side.NORTH
                    tempoross.side.mastLocation = mast.tile()
                }
            } else if (tempoross.energy == -1) {
                log.info("Not in game...")
                tempoross.side = Side.UNKNOWN
            } else {

                tempoross.burningTiles.clear()
                tempoross.triedPaths.clear()
                tempoross.detectDangerousTiles()


                val dest = Movement.destination()
                val destination = if (dest != Tile.Nil) dest else me.tile()
                val validTiles = listOf(tempoross.side.totemLocation, tempoross.side.mastLocation)
//                val tetherpoles = Objects.stream().filtered {
//                    validTiles.contains(it.tile())
//                }.forEach { log.info("Found ${it.name()}: ${it.actions().joinToString()}") }
                cookSpot = tempoross.side.cookLocation
                // In game
                bucket = tempoross.getBucketCrate()
                ammo = tempoross.getAmmoCrate()
                bossPool = tempoross.getBossPool()
                tether = tempoross.getTetherPole()

                tempoross.collectFishSpots()
                tempoross.bestFishSpot = tempoross.getClosestFishSpot(tempoross.fishSpots)
//                checkPaths(bucket, tether)
//                checkPaths(ammo, tempoross.bestFishSpot)
//                tempoross.hasDangerousPath(tempoross.side.bossWalkLocation)

            }
            sleep(1000)
        }
    }

    init {
        tempoross = Tempoross()
    }

}

class TemporossDebugPainter(script: TemporossDebug) : ATPaint<TemporossDebug>(script) {

    override fun paintCustom(g: Rendering) {
        if (script.tempoross.side != Side.UNKNOWN) {
            drawSide(g)
        }
    }

    fun drawSide(g: Rendering) {
        drawEntity(script.bucket)
        drawEntity(script.bossPool)
        drawEntity(script.ammo)
        drawEntity(script.tether)
        drawEntity(script.tempoross.bestFishSpot)

        script.cookSpot.drawOnScreen(null, CYAN)
        script.tempoross.side.anchorLocation.drawOnScreen(null, CYAN)
        script.tempoross.side.bossPoolLocation.drawOnScreen(null, CYAN)
        script.tempoross.side.totemLocation.drawOnScreen(null, CYAN)
        script.tempoross.side.bossWalkLocation.drawOnScreen(null, CYAN)
        script.tempoross.side.mastLocation.drawOnScreen(null, CYAN)

        val blockedTiles = script.tempoross.burningTiles.toList()
        val paths = script.tempoross.triedPaths.toList()

        blockedTiles.forEach {
            val t = it
            if (t != Tile.Nil) {
                it.drawOnScreen(null, RED)
            }
        }
        if (paths.isNotEmpty()) {
            paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                val containsBadTile = tiles.any { blockedTiles.contains(it) }
                val color = if (containsBadTile) ORANGE else GREEN
                tiles.forEach { tile ->
                    tile.drawOnScreen(

                        null,
                        if (blockedTiles.contains(tile)) BLACK else color
                    )
                }
            }
        }
    }

    fun drawEntity(e: InteractableEntity?) {
        if (e != null) {
            if (e.inViewport()) {
                val matrix = Touchscreen.scaleFromGame(e.tile().matrix().bounds())
                Rendering.drawPolygon(matrix)
                Rendering.drawString(
                    (e as Nameable).name() ?: "null",
                    matrix.getBounds().centerX,
                    matrix.getBounds().centerY
                )
            }
        }
    }

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.addString("Side:") { script.tempoross.side.name }
            .addString("Animation: ") { me.animation().toString() }
            .addString("Destination: ") { Movement.destination().toString() }
            .addString("Tethering: ") { script.tempoross.isTethering().toString() }
            .addString({ "Energy: ${script.tempoross.energy}" }, { "Health: ${script.tempoross.health}" })
            .build()
    }

}

fun main() {
    TemporossDebug().startScript()
}