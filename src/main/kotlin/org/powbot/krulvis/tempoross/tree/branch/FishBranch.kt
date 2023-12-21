package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Cook
import org.powbot.krulvis.tempoross.tree.leaf.Fish
import org.powbot.krulvis.tempoross.tree.leaf.Shoot
import kotlin.math.roundToInt

class ShouldShoot(script: Tempoross) : Branch<Tempoross>(script, "Should Shoot") {
    override fun validate(): Boolean {

        val cooked = Inventory.getCount(COOKED)
        val raw = Inventory.getCount(RAW)

        val fish = if (script.solo) cooked else cooked + raw
        //Forced shooting happens after a tether attempt

        val energy = script.getEnergy()
        val hp = script.getHealth()
        val lowEnoughEnergy = energy / 2.5 < fish

        //If we are close to the ammo-box and have some fish, shoot em
        if (fish > 0) {
            val ammoCrate = script.getAmmoCrate()
            if ((ammoCrate?.distance()?.roundToInt() ?: 8) < 7) {
                script.log.info("Shooting because close and hasShootableFish")
                return true
            } else if (script.solo) {
                val requiredToSubdue = script.cookedToSubdue()
                //If its near failure, just make sure we get the energy to 0
                if (script.getIntensity() >= 90) {
                    return cooked >= requiredToSubdue
                }
                val requiredFish = if (energy <= 10) 19 else requiredToSubdue - 2
                script.log.info("Shooting fish energy=$energy, requiredFish=$requiredFish cooked in inventory")
                if (cooked >= requiredFish) {
                    return true
                }
            } else if (hp <= 75 && lowEnoughEnergy && energy > 13) {
                script.log.info("Shooting to empty inventory before last group harpoon")
                return true
            } else if (Inventory.isFull() && (!script.cookFish || raw <= 0)) {
                script.log.info("Shooting fish because inventory is full")
                return true
            }
        }

        return false
    }

    override val successComponent: TreeComponent<Tempoross> = Shoot(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldCook(script)
}


class ShouldCook(script: Tempoross) : Branch<Tempoross>(script, "Should Cook") {
    override fun validate(): Boolean {
        if (!script.cookFish) return false

        val raw = Inventory.getCount(RAW)
        val cooked = Inventory.getCount(COOKED)
        val cookedTo10 = script.cookedToSubdue() - 2
        script.collectFishSpots()
        script.bestFishSpot = script.getClosestFishSpot(script.fishSpots)

        val cooking = me.animation() == FILLING_ANIM

        val doubleSpot = script.bestFishSpot?.id() == DOUBLE_FISH_ID
        val cookLocation = script.side.cookLocation
        if (script.solo) {
            if (cookedTo10 > cooked && (cooked + raw) >= cookedTo10) {
                debug("Cooking because need to bring to 10% energy")
                return true
            } else if (raw >= 19 && script.getIntensity() >= 85 - (19 - cooked)) {
                debug("Cooking because have more than 19 fish")
                return true
            }
        }
        if (doubleSpot && Inventory.emptySlotCount() > if (cooking) 1 else 0) {
            //If we are prioritizing fishing at double spot over cooking, we need to have at least 2 spaces
            debug("Fishing because double spot available")
            return false
        } else if (raw > 0 && cookLocation.distance() <= 2) {
            debug("Cooking because already there are no good fishing spots available...")
            return true
        } else if (raw >= 8 && !script.hasDangerousPath(cookLocation)) {
            debug("Start early cooking since there is no double spot!")
            return true
        }

        val energy = script.getEnergy()
        val lowEnergy = if (script.solo) false else energy / 4 < Inventory.getCount(true, RAW, COOKED)
        val fullHealth = script.getHealth() == 100
        script.log.info("fullHealth=$fullHealth, energy=$energy, lowEnergy=$lowEnergy, rawCount=$raw")
        return raw > 0 && (Inventory.isFull() || (lowEnergy && !fullHealth) || script.bestFishSpot == null)
    }

    override val successComponent: TreeComponent<Tempoross> = Cook(script)
    override val failedComponent: TreeComponent<Tempoross> = Fish(script)
}
