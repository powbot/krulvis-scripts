package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.House.useRestorePool
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter

class OpenBank(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Opening Bank") {
	override fun execute() {
		if (script.goToHouse) {
			if (!House.isInside()) {
				if (Magic.Spell.TELEPORT_TO_HOUSE.cast()) {
					waitFor(5000) { House.isInside() }
				}
			}
			if (House.isInside()) {
				useRestorePool()
			}
		}
		if (script.orb.bank == BankLocation.FALADOR_WEST_BANK && script.orb.bank.tile.distance() >= 50) {
			Magic.Spell.FALADOR_TELEPORT.cast()
			waitFor(long()) { script.orb.bank.tile.distance() <= 50 }
		} else {
			script.orb.bank.open()
		}
	}
}