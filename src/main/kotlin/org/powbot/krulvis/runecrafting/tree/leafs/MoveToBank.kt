package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.Runecrafter

class MoveToBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving To Bank") {
    override fun execute() {
        val teleport = script.alter.bankTeleport
        if (teleport != null) {
            if (teleport.cast()) {
                waitFor(5000) { Bank.getBank().valid() }
            }
        } else {
            Movement.moveToBank()
        }
    }
}