package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.tempoross.Tempoross

class GetItemsFromBank(script: Tempoross) : Leaf<Tempoross>(script, "Getting Items") {
	override fun execute() {
		val missingEquipment = getMissingEquipment()
		val equippables = getEquipablesInInventory()
		if (!Bank.opened() && equippables.isNotEmpty()) {
			equipEquipment()
		} else {
			if (missingEquipment.isNotEmpty() || equippables.isNotEmpty()) {
				if (Bank.openNearest()) {
					Bank.withdrawExact(equippables + missingEquipment)
				}
			} else {
				Bank.close()
			}
		}

	}

	private fun equipEquipment(): Boolean {
		val inventoryEquipment = Inventory.stream().id(*script.equipment.map { it.key }.toIntArray()).toList()
		return inventoryEquipment.all { it.click() }
	}

	private fun getMissingEquipment(): Map<Int, Int> {
		val equipped = Equipment.stream().map { it.id }
		val inventory = Inventory.stream().toList().map { it.id }
		return script.equipment.filterNot { equipped.contains(it.key) || inventory.contains(it.key) }
	}

	private fun getEquipablesInInventory(): Map<Int, Int> {
		val relevant = script.getRelevantInventoryItems()
		return script.inventoryBankItems.filter { relevant.getOrDefault(it.key, 0) < it.key }
	}
}