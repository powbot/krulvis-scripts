package org.powbot.krulvis.orbcharger

import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.teleports.EDGEVILLE_GLORY
import org.powbot.krulvis.api.teleports.FALADOR_TELEPORT
import org.powbot.krulvis.api.teleports.SpellTeleport
import org.powbot.krulvis.api.teleports.Teleport
import org.powbot.krulvis.api.teleports.poh.HouseTeleport
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.FALADOR_TELEPORT_NEXUS
import org.powbot.krulvis.orbcharger.tree.branch.ShouldBank

@ScriptManifest(
	name = "krul Orbs",
	description = "Craft orbs",
	author = "Krulvis",
	version = "1.0.2",
	markdownFileName = "Orb.md",
	category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Orb",
			allowedValues = ["AIR", "WATER", "EARTH", "FIRE"],
			defaultValue = "FIRE",
			description = "Choose orb type"
		),
		ScriptConfiguration(
			name = "Fast charge",
			description = "Re-cast charge spell?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false"
		),
		ScriptConfiguration(
			name = "Antipoison",
			description = "Take antipoison?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
		ScriptConfiguration(
			name = "BankTeleport",
			description = "How to get to bank?",
			optionType = OptionType.STRING,
			allowedValues = [EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FALADOR_TELEPORT, FALADOR_TELEPORT_NEXUS],
			defaultValue = FALADOR_TELEPORT_NEXUS
		),
		ScriptConfiguration(
			name = "Food",
			description = "Which food to use?",
			optionType = OptionType.STRING,
			defaultValue = "LOBSTER",
			allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
		)
	]
)
class OrbCrafter : ATScript() {

	val orb by lazy { Orb.valueOf(getOption("Orb")) }
	val bankTeleport by lazy { Teleport.forName(getOption("BankTeleport"))!! }
	val fastCharge by lazy { getOption<Boolean>("Fast charge") }
	val antipoison by lazy { getOption<Boolean>("Antipoison") }
	val food by lazy { Food.valueOf(getOption("Food")) }

	override fun createPainter(): ATPaint<*> = OrbPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldBank(this)

	override fun onStart() {
		super.onStart()
		logger.info("BankTeleport = $bankTeleport")
	}

	val necessaries by lazy {
		intArrayOf(
			Orb.UNPOWERED,
			Orb.COSMIC,
			Rune.AIR.id,
			*Potion.ANTIPOISON.ids,
			*Potion.SUPER_ANTIPOISON.ids,
			*orb.requirements.flatMap { it.item.ids.toList() }.toIntArray()
		)
	}
}

fun main() {
	OrbCrafter().startScript("127.0.0.1", "GIM", true)
}