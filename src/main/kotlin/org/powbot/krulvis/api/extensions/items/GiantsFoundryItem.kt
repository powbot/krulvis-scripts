package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Varpbits

enum class GiantsFoundryItem(override val ids: IntArray) : Item {
    BRONZE(intArrayOf(Bar.BRONZE.id)),
    IRON(intArrayOf(Bar.IRON.id, Item.IRON_WARHAMMER)),
    STEEL(intArrayOf(Bar.STEEL.id, Item.STEEL_WARHAMMER)),
    GOLD(intArrayOf(Bar.GOLD.id,)),
    MITHRIL(intArrayOf(Bar.MITHRIL.id,)),
    ADAMANTITE(intArrayOf(Bar.ADAMANTITE.id, Item.ADAMANT_PLATELEGS, Item.ADAMANT_PLATESKIRT, Item.ADAMANT_PLATEBODY,
        Item.ADAMANT_FULLHELM, Item.ADAMANT_KITESHIELD, Item.ADAMANT_SCIMITAR, Item.ADAMANT_BOOTS)),
    RUNITE(intArrayOf(Bar.RUNITE.id,)
    );

    val giantsFoundryCount: Int
        get() = Varpbits.varpbit(GIANTS_FOUNDRY_VARP, 5 * METAL_ITEMS.indexOf(this), 31)

    override fun hasWith(): Boolean {
        return getCount(false) >= 1
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }

    companion object {
        val METAL_ITEMS = arrayOf(BRONZE, IRON, STEEL, GOLD, MITHRIL, ADAMANTITE, RUNITE)
        val GIANTS_FOUNDRY_VARP = 3431

        fun forId(id: Int): GiantsFoundryItem? {
            val test = values().firstOrNull { it.id == id }
            return test;
        }
    }
}