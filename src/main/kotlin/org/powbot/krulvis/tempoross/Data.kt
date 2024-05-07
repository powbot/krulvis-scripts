package org.powbot.krulvis.tempoross

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Widgets

object UI {
    const val LOOTING = "looting"
    const val EQUIPMENT = "equipment"
    const val INVENTORY = "inventory"
    const val COOK_FISH = "cook"
    const val SPECIAL_ATTACK = "special attack"
    const val SOLO_METHOD = "solo"
    const val BUCKETS = "buckets"
    const val IMCANDO_HAMMER = "imcando hammer"
}

object Data {


    val WAVE_TIMER = 9500
    val PARENT_WIDGET = 437

    var DOUBLE_FISH_ID = 10569
    val RAW = 25564
    val COOKED = 25565
    val CRYSTAL = 25566

    val TETHER_ANIM = 3705
    val FILLING_ANIM = 896
    val WATER_ANIM = 832
    val KILLING_ANIM = 618

    val BOAT_AREA = Area(Tile(3129, 2831), Tile(3135, 2843))

    val HARPOON = 311
    val BARB_TAIL_HARPOON = 10129
    val SPEC_HARPOONS = intArrayOf(21028, 21031, 21033, 23762, 23764, 25059, 25373)
    val WEARABLE_HARPOONS = intArrayOf(BARB_TAIL_HARPOON, *SPEC_HARPOONS)
    val HARPOONS = intArrayOf(HARPOON, *WEARABLE_HARPOONS)

    fun getEnergy(): Int {
        val text = Widgets.widget(PARENT_WIDGET).firstOrNull {
            it != null && it.visible() && it.text().contains("Energy")
        }?.text() ?: return -1
        return text.substring(8, text.indexOf("%")).toInt()
    }


    fun getIntensity(): Int {
        val text = Widgets.widget(PARENT_WIDGET).firstOrNull {
            it != null && it.visible() && it.text().contains("Storm Intensity")
        }?.text() ?: return -1
        return text.substring(17, text.indexOf("%")).toInt()
    }

    fun getHealth(): Int {
        val text = Widgets.widget(PARENT_WIDGET).firstOrNull {
            it != null && it.visible() && it.text().contains("Essence")
        }?.text() ?: return -1
        return text.substring(9, text.indexOf("%")).toInt()
    }

}

enum class Side {
    UNKNOWN,
    NORTH,
    SOUTH;

    var mastLocation = Tile(9618, 2817, 0)
    val oddFishingSpot: Tile get() = Tile(mastLocation.x() - 21, mastLocation.y() - 15, 0)

    val cookLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 5, mastLocation.y() + 22, 0)
        else Tile(mastLocation.x() - 22, mastLocation.y() - 21, 0)

    val northCookSpot: Tile get() = Tile(cookLocation.x() + 1, cookLocation.y() - 3, 0)

    val bossPoolLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 11, mastLocation.y() + 5, 0)
        else Tile(mastLocation.x() - 11, mastLocation.y() - 5, 0)

    val bossWalkLocation: Tile
        get() = if (this == NORTH)
            Tile(bossPoolLocation.x(), bossPoolLocation.y() + 2, 0)
        else
            Tile(bossPoolLocation.x(), bossPoolLocation.y() - 2, 0)

    val totemLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 18, 0)
        else Tile(mastLocation.x() - 15, mastLocation.y() - 16, 0)

    val anchorLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 9, 0)
        else
            Tile(mastLocation.x() - 8, mastLocation.y() - 9, 0)

    val area: Area
        get() {
            val x = mastLocation.x()
            val y = mastLocation.y()
            return if (this == NORTH) {
                Area(
                    Tile(x - 4, y - 5),
                    Tile(x + 5, y - 5),
                    Tile(x + 10, y + 6),
                    Tile(x + 15, y + 10),
                    Tile(x + 16, y + 20),
                    Tile(x + 16, y + 30),
                    Tile(x - 5, y + 30)
                )
            } else {
                Area(
                    Tile(x + 4, y + 5),
                    Tile(x - 3, y + 5),
                    Tile(x - 10, y - 5),
                    Tile(x - 20, y - 10),
                    Tile(x - 20, y - 20),
                    Tile(x + 2, y - 20),
                    Tile(x + 4, y - 5)
                )
            }
        }
}