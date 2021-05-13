package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.extensions.walking.local.LocalPath.Companion.getNext
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.walking.Walking
import org.powerbot.bot.rt4.client.internal.IActor
import org.powerbot.script.Condition
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Actor
import java.awt.Graphics2D

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript() {


    val tile = Tile(3277, 3178, 0)

    override val painter: ATPainter<*>
        get() = Painter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestScript>(this, "TestLeaf") {
        override fun execute() {

        }
    }

    override fun startGUI() {
        debugComponents = true
        started = true
    }

    override fun stop() {
        painter.saveProgressImage()
    }

}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        val ct = script.ctx.players.local().tile()
        if (ct != null) {
            ct.drawOnMap(g)
            ct.drawCircleOnMap(g, 5)
        }
    }

    fun realOrientation(): Int {
        val player = script.ctx.players.local()
        val method = Actor::class.java.getDeclaredMethod("getActor")
        method.isAccessible = true
        val actor = method.invoke(player) as IActor
        return actor.orientation
    }

    fun correctOrientation(): Int {
        val ori = realOrientation()
        return when (ori % 256) {
            255 -> ori + 1
            1 -> ori - 1
            else -> ori
        }
    }

    fun facingTile(): Tile {
        val orientation = realOrientation()
        val t: Tile = script.ctx.players.local().tile()
        when (orientation) {
            4 -> return Tile(t.x(), t.y() + 1, t.floor())
            6 -> return Tile(t.x() + 1, t.y(), t.floor())
            0 -> return Tile(t.x(), t.y() - 1, t.floor())
            2 -> return Tile(t.x() - 1, t.y(), t.floor())
        }
        return Tile.NIL
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        g.drawString("Test string", x, y)
        y += yy
    }
}