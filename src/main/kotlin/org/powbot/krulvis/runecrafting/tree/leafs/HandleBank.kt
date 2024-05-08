package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.*
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Handling Bank") {
    override fun execute() {
        val invPouches = EssencePouch.inInventory()
        if (invPouches.all { it.filled() } && Bank.depositAllExcept(*keep) && Inventory.isFull()) {
            Bank.close()
        } else if (Bank.stream().name(script.essence).count() <= 0) {
            Notifications.showNotification("Out of essence, stopping script")
            ScriptManager.stop()
        } else if (invPouches.any { !it.filled() }) {
            if (!Inventory.isFull() && withdrawEssence()) {
                waitFor { Inventory.isFull() }
            }
            invPouches.forEach { it.fill() }
        } else if (Bank.depositAllExcept(*keep)) {
            withdrawEssence()
        }
    }

    val keep = arrayOf("Rune pouch", "Small pouch", "Medium pouch", "Large pouch", "Giant pouch", "Colossal pouch",
            RUNE_ESSENCE, PURE_ESSENCE, DAEYALT_ESSENCE, "Air rune", "Earth rune", "Cosmic rune", "Law rune")

    private fun withdrawEssence(): Boolean = Bank.withdraw(script.essence, Bank.Amount.ALL)

}