package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.Game
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "PathBuilder", version = "1.0.0", description = "", priv = true)
class PathBuilder : ATScript() {
    override fun createPainter(): ATPaint<*> = PathBuilderPainter(this)

    val path: MutableList<Tile> = mutableListOf()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        val currentPosition = me.tile()
        val distanceLast = path.lastOrNull()?.distance() ?: 6.0
        if (distanceLast >= 5.0) {
            path.add(currentPosition)
        }
        log.info("CURRENTLY WE HAVE")
        log.info(path.joinToString(", ") { "Tile(${it.x}, ${it.y}, ${it.floor})" })
        sleep(500)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }
}


class PathBuilderPainter(script: PathBuilder) : ATPaint<PathBuilder>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
                .build()
    }

    override fun paintCustom(g: Rendering) {
        script.path.draw()
    }

    fun List<Tile>.draw() {
        forEachIndexed { index, tile ->
            tile.drawOnScreen(index.toString())
        }
    }


    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    PathBuilder().startScript("127.0.0.1", "banned", true)
}
