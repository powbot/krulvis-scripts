package org.powbot.krulvis.spices.tree.leafs

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Widgets
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.spices.Spices

class Enter(script: Spices) : Leaf<Spices>(script, "Enter") {

    override fun execute() {
        val curtain = Objects.stream().name("Curtain").action("Enter").nearest().firstOrNull() ?: return
        script.log.info("Found curtain at tile=${curtain.tile}")
        if (curtain.interact("Enter")) {
            waitFor { script.enterComponent()?.visible() == true }
        }
    }
}