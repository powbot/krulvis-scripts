package org.powbot.krulvis.api.teleports.poh

import org.powbot.api.Notifications
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.House.Pool.Companion.pool
import org.powbot.krulvis.api.teleports.Teleport
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance

interface HouseTeleport : Teleport {
	override fun execute(): Boolean {
		logger.info("Executing ${toString()}")
		if (!House.isInside()) {
			val cape = Equipment.stream().nameContains("Construction cape").first()
			val tab = Inventory.stream().name("Teleport to house").first()
			val portal = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Portal").action("Home").first()
			if (portal.valid()) {
				if (walkAndInteract(portal, "Home")) {
					waitFor(5000) { House.isInside() }
				}
			} else if (cape.valid()) {
				if (cape.interact("Inside")) {
					waitFor(5000) { House.isInside() }
				}
			} else if (tab.valid()) {
				if (tab.interact("Break")) {
					waitFor(5000) { House.isInside() }
				}
			} else if (Magic.Spell.TELEPORT_TO_HOUSE.canCast()) {
				if (Magic.Spell.TELEPORT_TO_HOUSE.cast()) {
					waitFor(5000) { House.isInside() }
				}
			} else {
				Notifications.showNotification("Can't find method to get to POH")
				sleep(600)
			}
		} else if (useRestorePool()) {
			return insideHouseTeleport()
		}
		return false
	}

	fun shouldRestorePool(poolObj: GameObject): Boolean {
		val pool = poolObj.pool() ?: return false

		if (pool.ordinal >= 1 && Movement.energyLevel() <= 90) return true
		if (pool.ordinal >= 2 && Skills.level(Skill.Prayer) < Skills.realLevel(Skill.Prayer)) return true
		if (pool.ordinal >= 4 && currentHP() < maxHP()) return true

		return Combat.specialPercentage() < 100
	}

	fun useRestorePool(): Boolean {
		val pool = House.getPool()
		if (!shouldRestorePool(pool)) {
			logger.info("Shouldn't use pool.")
			return true
		}
		return walkAndInteract(pool, "Drink") && waitForDistance(pool) { !shouldRestorePool(pool) }
	}

	fun insideHouseTeleport(): Boolean
}

