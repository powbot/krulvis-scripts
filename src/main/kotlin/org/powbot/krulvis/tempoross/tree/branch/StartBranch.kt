package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.*

class ShouldEnterBoat(script: Tempoross) : Branch<Tempoross>(script, "Should enter boat") {
    override fun validate(): Boolean {
        if (Game.clientState() != 30) {
            return !waitFor(10000) { script.energy > -1 }
        }
        return script.energy == -1 && !BOAT_AREA.contains(me.tile())
                && Npcs.stream().name("Ammunition crate").firstOrNull() == null
                && Npcs.stream().noneMatch { it.actions().contains("Leave") }
    }

    override val successComponent: TreeComponent<Tempoross> = HasAllItemsFromBank(script)
    override val failedComponent: TreeComponent<Tempoross> = WaitingForStart(script)
}

class HasAllItemsFromBank(script: Tempoross) : Branch<Tempoross>(script, "Has All Items From Bank") {
    override val failedComponent: TreeComponent<Tempoross> = GetItemsFromBank(script)
    override val successComponent: TreeComponent<Tempoross> = EnterBoat(script)

    override fun validate(): Boolean {
        val equipment = Equipment.stream().toList().map { it.id }
        val inventory = script.getRelevantInventoryItems()
        return script.equipment.all { equipment.contains(it.key) } && script.inventoryBankItems.all {
            it.value <= inventory.getOrDefault(it.key, 0)
        }
    }


}

class WaitingForStart(script: Tempoross) : Branch<Tempoross>(script, "Waiting For Game Start") {
    override fun validate(): Boolean {
        if (script.side == Side.UNKNOWN) {
            if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
                script.log.info("Getting Side of mini-game")
                val mast = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Mast").nearest().first()
                script.log.info("Mast found: $mast, orientation: ${mast.orientation()}")
                script.side = if (mast.orientation() == 4) Side.SOUTH else Side.NORTH
                script.side.mastLocation = mast.tile()
            } else {
                script.log.info("Couldn't find ammunition crate")
                return true
            }
        }
        return false
    }


    override val successComponent: TreeComponent<Tempoross> = ShouldFillBuckets(script)

    override val failedComponent: TreeComponent<Tempoross> = ShouldLeave(script)
}

class ShouldFillBuckets(script: Tempoross) : Branch<Tempoross>(script, "Should Fill Buckets") {
    override fun validate(): Boolean {
        return Inventory.containsOneOf(EMPTY_BUCKET)
    }

    override val successComponent: TreeComponent<Tempoross> = FillBuckets(script)
    override val failedComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Wait for game to start...") {
        waitFor(60000) { script.energy > -1 }
    }
}

class ShouldLeave(script: Tempoross) : Branch<Tempoross>(script, "Should leave") {
    override fun validate(): Boolean {
        return Npcs.stream().action("Leave").isNotEmpty()
    }

    override val successComponent: TreeComponent<Tempoross> = HasAllBuckets(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldTether(script)
}

class HasAllBuckets(script: Tempoross) : Branch<Tempoross>(script, "Should get buckets before leaving") {
    override val failedComponent: TreeComponent<Tempoross> = GetBuckets(script)
    override val successComponent: TreeComponent<Tempoross> = Leave(script)

    override fun validate(): Boolean {
        return script.getTotalBuckets() >= script.buckets
    }

}