package org.powbot.krulvis.miner.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Worlds
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.api.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.Mine
import org.powbot.krulvis.miner.tree.leaf.WalkToSpot
import org.powbot.mobile.script.ScriptManager

class AtSpot(script: Miner) : Branch<Miner>(script, "AtSpot") {

    override fun validate(): Boolean {
        val nearest = script.rockLocations.minByOrNull { it.distance() }
        if (nearest == null) {
            Notifications.showNotification("Select at least one rock to mine at!")
            ScriptManager.stop()
            return false
        }
        return nearest.distance() <= 10 && nearest.getWalkableNeighbor(checkForWalls = false)?.reachable() == true
    }

    override val successComponent: TreeComponent<Miner> = ShouldHop(script)
    override val failedComponent: TreeComponent<Miner> = WalkToSpot(script)
}

class ShouldHop(script: Miner) : Branch<Miner>(script, "ShouldHop") {

    val hopDelay = DelayHandler(2000, script.oddsModifier, "Hop delay")
    override fun validate(): Boolean {
        if (!script.hopFromPlayers) {
            return false
        }
        val nearByPlayers = Players.stream().filtered {
            it.name() != Players.local().name() && it.tile()
                .distanceTo(script.rockLocations[Random.nextInt(0, script.rockLocations.size)]) <= 5
        }
        if (nearByPlayers.isNotEmpty()) {
            if (hopDelay.isFinished()) {
                return true
            }
        } else {
            hopDelay.resetTimer()
        }
        return false
    }

    override val successComponent: TreeComponent<Miner> = SimpleLeaf(script, "Hopping") {
        val worlds = Worlds.get()
        if (worlds.isNotEmpty()) {
            worlds[Random.nextInt(0, worlds.size)].hop()
        }
    }
    override val failedComponent: TreeComponent<Miner> = IsMining(script)
}

class IsMining(script: Miner) : Branch<Miner>(script, "IsMining") {

    fun facingTile(): Tile {
        val orientation: Int = Players.local().orientation()
        val t: Tile = Players.local().tile()
        when (orientation) {
            4 -> return Tile(t.x(), t.y() + 1, t.floor())
            6 -> return Tile(t.x() + 1, t.y(), t.floor())
            0 -> return Tile(t.x(), t.y() - 1, t.floor())
            2 -> return Tile(t.x() - 1, t.y(), t.floor())
        }
        return Tile.Nil
    }


    override fun validate(): Boolean {
        if (Objects.stream().at(facingTile()).noneMatch { it.hasOre() }) {
            return false
        }
        return if (me.animation() > 0) {
            lastAnim = System.currentTimeMillis()
            true
        } else {
            System.currentTimeMillis() - lastAnim < 3000
        }
    }

    var lastAnim: Long = 0

    override val successComponent: TreeComponent<Miner> =
        SimpleLeaf(script, "Chilling") {
            if (Random.nextBoolean())
                sleep(Random.nextInt(500, 800))
            else if (!waitFor(1500) {
                    Objects.stream().at(facingTile()).noneMatch { it.hasOre() }
                }) {
                if (me.animation() > 0) {
                    lastAnim = System.currentTimeMillis()
                }
            }
        }
    override val failedComponent: TreeComponent<Miner> = Mine(script)
}