package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Walk(script: Woodcutter) : Leaf<Woodcutter>(script, "Walk to Trees") {
    override fun execute() {
        val locs = script.trees
        script.chopDelay.forceFinish()
        if (locs.isEmpty()) {
            script.log.info("Script requires at least 1 Tree GameObject set in the Configuration")
            ScriptManager.stop()
        } else {
            val tile = locs.minByOrNull { it.distance() }
            if (script.forceWeb) {
                Movement.builder(tile).setForceWeb(true).move()
                return
            }
            if (tile != null && tile.distance() < 15) {
                Movement.step(tile)
                waitFor(long()) { tile.distance() < 5 }
            } else {
                Movement.walkTo(tile?.getStraightNeighor())
            }
        }
    }

    /**
     * Returns: [Tile] nearest neighbor or self as which is walkable
     */
    fun Tile.getStraightNeighor(
    ): Tile? {
        return getStraightNeighors().minByOrNull { it.distance() }
    }

    fun Tile.getStraightNeighors(
    ): List<Tile> {

        val t = tile()
        val x = t.x()
        val y = t.y()
        val f = t.floor()
        val cm = Movement.collisionMap(f).flags()

        val neighbors = mutableListOf<Tile>()
        for (yy in 1..2) neighbors.add(Tile(x, y + yy, f))
        for (yy in 1..2) neighbors.add(Tile(x, y - yy, f))
        for (xx in 1..2) neighbors.add(Tile(x + xx, y, f))
        for (xx in 1..2) neighbors.add(Tile(x - xx, y, f))

        return neighbors.filter { !it.blocked(cm) }
    }
}