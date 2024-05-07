package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getWalkableNeighbors
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : ATScript() {
    override fun createPainter(): ATPaint<*> = TestWebPainter(this)

    var patchTile = Tile(1267, 3727, floor = 0)
    var neighbor: Tile? = Tile(0, 0, 0) //Amathyst mine spot

    var patch: GameObject? = null
    var collisionMap: TransientGetter2D<Int>? = null

    fun getPatchObj() = Objects.stream().type(GameObject.Type.INTERACTIVE).at(Tile(1269, 3727))
            .action("Inspect")
            .nearest().firstOrNull()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        collisionMap = Movement.collisionMap(me.tile().floor).flags()
        patch = getPatchObj()
//        neighbor = patch?.getWalkableNeighbor(allowSelf = true, checkForWalls = true)
        val walkableNeighbors = patch?.getWalkableNeighbors()?: emptyList()

        log.info("Patch=$patch, neighbors=${walkableNeighbors.joinToString()}")
        neighbor = walkableNeighbors.firstOrNull()

        sleep(2000)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }
}


class TestWebPainter(script: TestWeb) : ATPaint<TestWeb>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
                .addString("Reachable") { script.neighbor?.reachable().toString() }
                .build()
    }

    override fun paintCustom(g: Rendering) {
        val collisionMap = script.collisionMap
        if (collisionMap != null) {
            val neighbor = script.neighbor
//            Players.local().tile().drawCollisions(collisionMap)
            val colorTile = if (script.patchTile.blocked(collisionMap)) Color.RED else Color.GREEN
            val colorPatch = if (script.patch?.tile?.blocked(collisionMap) == true) Color.ORANGE else Color.GREEN
            val colorNeighbor = if (script.neighbor?.blocked(collisionMap) == true) Color.RED else Color.GREEN
//            script.patchTile.drawOnScreen(outlineColor = colorTile)
            script.patch?.tile?.drawOnScreen(outlineColor = colorPatch)
            neighbor?.drawOnScreen(outlineColor = colorNeighbor)
        }

    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestWeb().startScript("127.0.0.1", "banned", true)
}
