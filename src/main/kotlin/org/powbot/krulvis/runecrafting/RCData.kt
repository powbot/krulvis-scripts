package org.powbot.krulvis.runecrafting

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects

const val FOOD_CONFIGURATION = "Food Config"
const val ESSENCE_TYPE_CONFIGURATION = "Essence Config"
const val DAEYALT_ESSENCE = "Deayalt essence"
const val RUNE_ESSENCE = "Rune Essence"
const val PURE_ESSENCE = "Pure essence"
const val RUNE_ALTAR_CONFIGURATION = "Rune Altar Config"
const val ASTRAL = "ASTRAL"

enum class RuneAltar(val tile: Tile, val bankTeleport: Magic.MagicSpell?, val pathToAltar: List<Tile>) {
    ASTRAL(Tile(2158, 3864, 0), Magic.LunarSpell.MOONCLAN_TELEPORT,
            pathToAltar = listOf(Tile(2099, 3919, 0), Tile(2103, 3915, 0), Tile(2109, 3915, 0), Tile(2112, 3910, 0), Tile(2112, 3904, 0), Tile(2113, 3898, 0), Tile(2114, 3892, 0), Tile(2114, 3886, 0), Tile(2117, 3880, 0), Tile(2122, 3876, 0), Tile(2127, 3872, 0), Tile(2132, 3870, 0), Tile(2134, 3865, 0), Tile(2139, 3861, 0), Tile(2145, 3861, 0), Tile(2150, 3863, 0), Tile(2155, 3864, 0))), ;

    fun getAltar(): GameObject? = Objects.stream().at(tile).name("Altar").firstOrNull()
}