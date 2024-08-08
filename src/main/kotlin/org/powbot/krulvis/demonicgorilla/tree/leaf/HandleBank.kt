package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.demonicgorilla.DemonicGorilla
import org.powbot.krulvis.fighter.Defender
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Handle bank") {
	override fun execute() {
		if (script.lastTrip) {
			Notifications.showNotification("Stopping because this was the last trip")
			ScriptManager.stop()
			return
		}
		script.bankTeleport.executed = false
		val ids = script.requiredInventory.flatMap { it.item.ids.toList() }.toIntArray()
		val edibleFood = foodToEat()
		val defender = Defender.defender()
		if (!Inventory.emptyExcept(Defender.defenderId(), *ids)) {
			Bank.depositInventory()
		} else if (edibleFood != null) {
			edibleFood.eat()
			waitFor { foodToEat() == null }
		} else if (handleEquipment() && foodToEat() == null) {

			script.requiredInventory.forEach {
				if (!it.withdraw(true) && !it.item.inBank()) {
					script.logger.info("Stopped because no ${it.item.name} in bank")
					ScriptManager.stop()
				}
			}

			script.forcedBanking = script.requiredInventory.all { it.meets() }
			if (!script.forcedBanking) Bank.close()
		}

	}

	fun foodToEat() = Food.values().firstOrNull { it.inInventory() && it.healing <= missingHP() }

	fun handleEquipment(): Boolean {
		script.equipment.forEach {
			if (!it.meets()) {
				it.withdrawAndEquip(true)
			}
		}
		script.allEquipmentItems.forEach {
			if (!it.hasWith()) {
				it.withdrawExact(1, true, wait = true)
			}
		}
		script.teleportEquipments.forEach {
			if (!it.hasWith()) {
				it.withdrawExact(1, true, wait = true)
			}
		}
		return script.allEquipmentItems.all { it.hasWith() } && script.teleportEquipments.all { it.hasWith() }
	}
}