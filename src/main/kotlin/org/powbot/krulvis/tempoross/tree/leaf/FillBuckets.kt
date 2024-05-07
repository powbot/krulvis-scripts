package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.WATER_ANIM
import org.powbot.krulvis.tempoross.Tempoross

class FillBuckets(script: Tempoross) : Leaf<Tempoross>(script, "Getting water") {
    override fun execute() {
        if (me.animation() != WATER_ANIM) {
            val waterPump = script.getWaterpump() ?: return
            val action = if (waterPump.actions().contains("Use")) "Use" else "Fill-bucket"
            if (walkAndInteract(waterPump, action) && waitFor(long()) { me.animation() == WATER_ANIM }
            ) {
                waitFor(long()) { !Inventory.containsOneOf(EMPTY_BUCKET) }
            }
        }
    }


}