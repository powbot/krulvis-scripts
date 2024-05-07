package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross

class GetHammer(script: Tempoross) : Leaf<Tempoross>(script, "Getting Hammer") {
    override fun execute() {
        val hammers = script.getHammerContainer() ?: return
        if (script.interactWhileDousing(hammers, "Take", script.side.anchorLocation, false)) {
            waitFor { script.hasHammer() }
        }
    }
}