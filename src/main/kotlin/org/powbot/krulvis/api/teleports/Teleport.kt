package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.teleports.poh.HousePortal
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.OpenableHouseTeleport
import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface Teleport {

	val logger: Logger
	val action: String
	val requirements: List<Requirement>
	fun execute(): Boolean

	companion object {

		fun forName(name: String): Teleport? {
			LoggerFactory.getLogger("Teleport")!!.info("Getting teleport for name=$name")
			return SpellTeleport.forName(name) ?: ItemTeleport.forName(name) ?: HousePortal.forName(name) ?: OpenableHouseTeleport.find(name)
		}
	}
}