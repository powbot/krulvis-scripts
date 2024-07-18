package org.powbot.krulvis.dagannothkings

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.krulvis.api.extensions.items.Item.Companion.RARE_DROP_TABLE
import org.powbot.krulvis.api.utils.requirements.EquipmentRequirement

object Data {

	const val KILL_PREFIX_OPTION = "Kill"
	const val EQUIPMENT_PREFIX_OPTION = "Equipment"
	const val OFFENSIVE_PRAY_PREFIX_OPTION = "OffensivePrayer"

	val PEEK_TILE = Tile(1917, 4363, 0)
	val ROOT_TILE = Tile(1918, 4366, 0)
	val KINGS_LADDER_UP_TILE = Tile(1911, 4367, 0)

	fun getRoot() = Objects.stream(ROOT_TILE).type(GameObject.Type.INTERACTIVE).name("Root").first()
	fun getKingsLadderUp() = Objects.stream(KINGS_LADDER_UP_TILE).type(GameObject.Type.INTERACTIVE).name("Kings' ladder").first()
	fun getKingsLadderDown() = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Kings' ladder").action("Climb-up").first()

	enum class King(val protectionPrayer: Prayer.Effect, var offensivePrayer: Prayer.Effect?, var equipment: List<EquipmentRequirement>, var kill: Boolean) {
		Supreme(Prayer.Effect.PROTECT_FROM_MISSILES, Prayer.Effect.PIETY, emptyList(), false),
		Rex(Prayer.Effect.PROTECT_FROM_MELEE, Prayer.Effect.MYSTIC_MIGHT, emptyList(), true),
		Prime(Prayer.Effect.PROTECT_FROM_MAGIC, Prayer.Effect.EAGLE_EYE, emptyList(), false),
		;

		companion object {
			fun Npc.king() = values().firstOrNull { it.name.lowercase() in name.lowercase() }
		}
	}

	val LOOT = arrayOf(
		"Berserker ring",
		"Warrior ring",
		"Dragon axe",
		"Dagannoth bones",
		"Rock-shell plate",
		"Rock-shell legs",
		"Iron ore",
		"Coal",
		"Mithril ore",
		"Steel bar",
		*RARE_DROP_TABLE
	)
}