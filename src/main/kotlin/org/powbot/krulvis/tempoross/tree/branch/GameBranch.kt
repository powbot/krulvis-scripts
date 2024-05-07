package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.CRYSTAL
import org.powbot.krulvis.tempoross.Data.HARPOON
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Data.SPEC_HARPOONS
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.*
import kotlin.math.roundToInt

class ShouldSpec(script: Tempoross) : Branch<Tempoross>(script, "Should Spec") {
    override fun validate(): Boolean {
        return script.spec &&
                Combat.specialPercentage() == 100
                && Equipment.stream().id(*SPEC_HARPOONS).isNotEmpty()
                && (script.lastLeaf is Fish || script.lastLeaf is Kill)
    }


    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Special Attack") {
        if (Combat.specialAttack(true)) {
            waitFor(5000) { Combat.specialPercentage() < 100 }
        }
    }
    override val failedComponent: TreeComponent<Tempoross> = ShouldDouse(script)
}


class ShouldDouse(script: Tempoross) :
        Branch<Tempoross>(script, "Should douse fire") {
    override fun validate(): Boolean {
        if (!Inventory.containsOneOf(BUCKET_OF_WATER)) {
            return false
        }
        fire = script.getNearestFire() ?: return false
        return if (script.solo) true else fire!!.distance() <= 2
    }


    var fire: Npc? = null

    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Douse") {
        if (fire!!.interact("Douse")) {
            waitFor(600) { me.interacting() == fire }
            waitFor(5000) { script.getNearestFire() != fire || me.interacting() != fire }
        }
    }
    override val failedComponent: TreeComponent<Tempoross> = ShouldKill(script)
}

class ShouldKill(script: Tempoross) : Branch<Tempoross>(script, "Should Kill") {

    override fun validate(): Boolean {
        if (!script.isVulnerable()) return false
        val hp = script.health
        val lowestAllowedHP = getLowestHPAllowed()
        if (hp <= lowestAllowedHP) {
            script.log.info("We started killing at=${script.vulnerableStartHP}, hp=${hp} lower than allowed hp=$lowestAllowedHP")
            //If we are below a certain threshold compared to when we started, return to fishing/cooking
            return false
        }

        //Make sure that we keep shoot the leftovers
        val count = Inventory.stream().id(RAW, COOKED, CRYSTAL).count()
        val lowHp = script.isLowHP()
        if (count >= 1 && lowHp && script.atAmmoCrate()) {
            //If we are shooting and have fish left while tempoross is below a certain hp, keep shooting
            script.log.info("Don't kill yet LOW HP, shooting last fish at tempoross, count=$count, hp=$hp, energy=${script.energy}")
            return script.solo && script.energy >= 30
        }
        return !script.solo || !Inventory.containsOneOf(COOKED)
    }

    private fun getLowestHPAllowed(): Int {
        if (!script.solo) return 0
        val startHp = script.vulnerableStartHP
        return when {
            startHp >= 95 -> 65
            startHp >= 60 -> 33
            else -> 0
        }
    }


    override val successComponent: TreeComponent<Tempoross> = Kill(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetWater(script)
}


class ShouldGetWater(script: Tempoross) :
        Branch<Tempoross>(script, "Should get water") {
    override fun validate(): Boolean {
        if (script.intensity >= 90 || script.isVulnerable()) return false
        val filledBuckets = script.getFilledBuckets()
        val nearestFire = script.getNearestFire()
        if (nearestFire != null && filledBuckets == 0) {
            return true
        } else if (filledBuckets >= script.buckets || script.energy <= 10 || Inventory.isFull()) {
            return false
        } else if (script.intensity == 0 && script.health == 100) {
            return true
        }
        return (script.getBucketCrate()?.distance()?.roundToInt() ?: 7) <= 6
    }

    override val successComponent: TreeComponent<Tempoross> = CanFillEmptyBucket(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetRope(script)
}

class CanFillEmptyBucket(script: Tempoross) :
        Branch<Tempoross>(script, "Can fill empty buckets") {
    override fun validate(): Boolean {
        return script.getEmptyBuckets() > 0
    }

    override val successComponent: TreeComponent<Tempoross> = FillBuckets(script)
    override val failedComponent: TreeComponent<Tempoross> = GetBuckets(script)
}

class ShouldGetRope(script: Tempoross) : Branch<Tempoross>(script, "Should get rope") {
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetHarpoon(script)
    override val successComponent: TreeComponent<Tempoross> = GetRope(script)

    override fun validate(): Boolean {
        script.burningTiles.clear()
        script.triedPaths.clear()
        script.detectDangerousTiles()

        return !script.hasSpiritOutfit && script.intensity < 90 && !Inventory.containsOneOf(ROPE)
    }
}

class ShouldGetHarpoon(script: Tempoross) : Branch<Tempoross>(script, "Should get harpoon") {
    override fun validate(): Boolean {
        Game.tab(Game.Tab.INVENTORY)
        return script.inventory.contains(HARPOON) && !Inventory.containsOneOf(HARPOON)
    }

    override val successComponent: TreeComponent<Tempoross> = GetHarpoon(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetHammer(script)
}

class ShouldGetHammer(script: Tempoross) : Branch<Tempoross>(script, "Should get hammer") {
    override val failedComponent: TreeComponent<Tempoross> = ShouldShoot(script)
    override val successComponent: TreeComponent<Tempoross> = GetHammer(script)

    override fun validate(): Boolean {
        return !script.hasHammer() && script.intensity < 90 && (script.getHammerContainer()?.distance()
                ?.roundToInt() ?: 7) <= 6
    }
}




