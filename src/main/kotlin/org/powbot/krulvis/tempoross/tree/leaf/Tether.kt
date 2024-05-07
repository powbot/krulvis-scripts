package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbors
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item.Companion.HAMMER
import org.powbot.krulvis.api.extensions.items.Item.Companion.IMCANDO_HAMMER
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class Tether(script: Tempoross) : Leaf<Tempoross>(script, "Tethering") {
    override fun execute() {
        if (script.isTethering()) {
            script.log.info("Waiting for wave to pass..")
            if (waitForWave()) {
                script.log.info("Done tethering...")
                script.waveTimer.stop()
                sleep(Random.nextInt(650, 750))
            }
            return
        }
        val pole = script.getTetherPole() ?: return
        val poleTiles = pole.tile().getWalkableNeighbors(diagonalTiles = true)
        val nearestTile = poleTiles.minByOrNull { it.distance() } ?: return
        val safeTile = poleTiles.filterNot { script.burningTiles.contains(it) }.minByOrNull { it.distance() }
        if (safeTile == nearestTile && !script.waveTimer.isFinished()) {
            if (pole.actions().contains("Repair") && script.hasHammer()) {
                if (walkAndInteract(pole, "Repair")) {
                    waitFor {
                        script.waveTimer.isFinished() || !(script.getTetherPole()?.actions()?.contains("Repair")
                                ?: true)
                    }
                }
            }
            if (walkAndInteract(pole, "Tether")) {
                waitFor(2500 + 350 * pole.distance().toInt()) { script.waveTimer.isFinished() || script.isTethering() }
            }
        } else {
            script.walkWhileDousing(nearestTile, true)
        }
    }

    private fun waitForWave(): Boolean {
        return waitFor(script.waveTimer.getRemainder().toInt() + 1000) {
            script.waveTimer.isFinished() && !script.isTethering()
        }
    }

}