package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Tempoross

class Fish(script: Tempoross) : Leaf<Tempoross>(script, "Fishing") {

    override fun execute() {
        val fishSpot = script.bestFishSpot
        if (fishSpot == null) {
            script.log.info("No safe fishing spot found!")
            if (script.burningTiles.contains(me.tile())) {
                val safeTile = findSaveTile(me.tile())
                script.log.info("We are standing on a dangerous tile! Walking to $safeTile")
                if (safeTile != null && Movement.step(safeTile)) {
                    waitFor { me.tile() == safeTile }
                }
            } else if (script.fishSpots.any { it.second.actions.last().destination.distance() <= 1 }) {
                script.log.info("Nearby blocked fishing spot found that is blocked")
                val blockedTile =
                        script.fishSpots.filter { it.second.actions.last().destination.distance() <= 1 }
                                .first().second.actions.last()
                val fireOptional =
                        Npcs.stream().name("Fire").within(blockedTile.destination, 2.0).nearest().findFirst()
                if (fireOptional.isPresent) {
                    script.log.info("Dousing nearby fire...")
                    val fire = fireOptional.get()
                    if (walkAndInteract(fire, "Douse")) {
                        waitFor { Npcs.stream().at(fire.tile()).name("Fire").isEmpty() }
                    }
                }
            } else {
                script.log.info("No fishing spot found, walking to Totem pole / anchor")
                var path = LocalPathFinder.findPath(script.side.totemLocation)
                if (path.isEmpty()) {
                    path = LocalPathFinder.findPath(script.side.anchorLocation)
                }
                script.walkWhileDousing(path, false)
            }
            return
        }
        val interacting = me.interacting()
        val currentSpot = if (interacting is Npc) interacting else null

        if (currentSpot?.name() == "Fishing spot") {
            if (script.burningTiles.contains(me.tile())
                    || (currentSpot.id() != DOUBLE_FISH_ID && fishSpot.id() == DOUBLE_FISH_ID)
            ) {
                script.log.info("Moving to double/save fish spot!")
                fishAtSpot(fishSpot)
            } else {
                val tetherPole = script.getTetherPole()
                if (tetherPole != null && !tetherPole.inViewport()) {
                    if (script.side.oddFishingSpot.distance() <= 1) {
                        script.log.info("Fishing at weird spot so using unique camera rotation")
                        Camera.pitch(Random.nextInt(1200, 1300))
                    } else {
                        Camera.turnTo(tetherPole)
                    }
                }
            }
        } else {
            script.log.info("Fishing at first spot")
            fishAtSpot(fishSpot)
        }
    }

    private fun findSaveTile(tile: Tile): Tile? {
        return tile.getWalkableNeighbor(diagonalTiles = true) {
            !script.burningTiles.contains(it)
        }
    }

    private fun fishAtSpot(spot: Npc) {
        if (walkAndInteract(spot, "Harpoon")) {
            waitFor(Random.nextInt(1000, 5000)) {
                me.interacting().name() == "Fishing spot" || !script.waveTimer.isFinished() || spot.hasLeftUs()
            }
        } else if (Movement.moving()) {
            waitFor(long()) { spot.distance() <= 2 || spot.hasLeftUs() }
        }
    }

    private fun Npc.hasLeftUs(): Boolean = Npcs.stream().at(tile()).name(name).isEmpty()

}
