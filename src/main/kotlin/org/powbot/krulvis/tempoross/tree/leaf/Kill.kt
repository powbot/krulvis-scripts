package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.KILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross
import kotlin.math.roundToInt

class Kill(script: Tempoross) : Leaf<Tempoross>(script, "Killing") {

    override fun execute() {
        val spirit = script.getBossPool()
        val killing = me.animation() != -1 && (spirit?.distance()?.roundToInt() ?: 4) <= 3

        Chat.canContinue()
        if (!killing && script.interactWhileDousing(spirit, "Harpoon", script.side.bossWalkLocation, true)) {
            waitFor(long()) { me.animation() == KILLING_ANIM }
        }
    }
}
