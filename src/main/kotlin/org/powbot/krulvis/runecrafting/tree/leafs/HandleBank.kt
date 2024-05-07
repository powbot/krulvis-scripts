package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.*

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Handling Bank") {
    override fun execute() {
        Bank.depositAllExcept(*keep)
        if (EssencePouch.inInventory().all { it.filled() } && Inventory.isFull()) {
            Bank.close()
        } else {
            if (!Inventory.isFull() && withdrawEssence()) {
                waitFor { Inventory.isFull() }
            }
            EssencePouch.inInventory().forEach { it.fill() }
            withdrawEssence()
        }
    }

    val keep = arrayOf("Rune pouch", "Small pouch", "Medium pouch", "Large pouch", "Giant pouch", "Colossal pouch",
            RUNE_ESSENCE, PURE_ESSENCE, DAEYALT_ESSENCE, "Air rune", "Earth rune", "Cosmic rune", "Law rune")

    private fun withdrawEssence(): Boolean = Bank.withdraw(script.essence, Bank.Amount.ALL)

}