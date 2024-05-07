package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter

class RepairPouches(script: Runecrafter) : Leaf<Runecrafter>(script, "Repairing pouches") {
    override fun execute() {
        Bank.close()
        val repairComp = repairComponent()
        if (!Chat.chatting()) {
            if (Magic.LunarSpell.NPC_CONTACT.cast("Dark Mage")) {
                waitFor(5000) { Chat.chatting() }
            }
        } else if (repairComp.visible() && repairComp.click()) {
            waitFor { Chat.canContinue() }
        } else if (Chat.clickContinue()) {
            waitFor { EssencePouch.inInventory().none { it.shouldRepair() } || repairComponent().visible() }
        }
    }

    fun repairComponent() = Components.stream(219, 1).text("Can you repair my pouches?").first()
}