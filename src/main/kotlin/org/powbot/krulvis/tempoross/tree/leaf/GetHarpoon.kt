package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.HARPOON
import org.powbot.krulvis.tempoross.Tempoross


class GetHarpoon(script: Tempoross) : Leaf<Tempoross>(script, "Getting harpoon") {
    override fun execute() {
        val harpoons =
                Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Harpoons").filtered { it.tile().distanceTo(script.side.mastLocation) <= 6 }.firstOrNull()
        if (harpoons == null || harpoons.distance() >= 25) {
            script.walkWhileDousing(script.side.anchorLocation, true)
        } else if (script.interactWhileDousing(harpoons, "Take", script.side.mastLocation, true)) {
            waitFor(long()) { Inventory.containsOneOf(HARPOON) }
        }
    }
}