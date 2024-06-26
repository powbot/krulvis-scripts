package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.mobile.script.ScriptManager
import java.io.Serializable

interface EquipmentItem : Item {

	val slot: Equipment.Slot?
		get() = null

	override fun hasWith(): Boolean = inInventory() || inEquipment()

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(countNoted) + Equipment.stream().id(*ids).count(true).toInt()
	}

	fun withdrawAndEquip(stopIfOut: Boolean = false): Boolean {
		if (inEquipment()) {
			return true
		} else if (!inInventory()) {
			if (Bank.withdrawModeNoted(false) && inBank()
				&& withdrawExact(1, true)
			) {
				waitFor(5000) { inInventory() }
			} else if (!inBank() && stopIfOut) {
				ScriptManager.script()!!.logger.warn("Stopping script due to being out of: ${id}")
				ScriptManager.stop()
			}
		}
		if (inInventory()) {
			return equip(true)
		}
		return inEquipment()
	}

	fun inEquipment(): Boolean {
		return Equipment.get().any { it.id() in ids }
	}

	fun equip(wait: Boolean = true): Boolean {
		if (inInventory()) {
			val item = Inventory.stream().id(*ids).first()
			val action = item.actions().first { it in listOf("Wear", "Wield", "Equip") }
			ScriptManager.script()!!.logger.info("Equipping ${id} action=$action")
			if (item.interact(action)) {
				if (wait) waitFor(2000) { inEquipment() } else return true
			}
		}
		return inEquipment()
	}

	fun dequip(): Boolean {
		if (!inEquipment()) {
			return true
		}
		val equipped = Equipment.stream().id(*ids).first()
		return equipped.click()
	}
}

class Equipment(
	override val slot: Equipment.Slot? = null,
	override vararg val ids: Int
) : EquipmentItem, Serializable