package org.powbot.krulvis.runecrafting

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.krulvis.api.utils.Utils.waitFor

enum class EssencePouch(val capacity: Int, val perfectId: Int) {
    SMALL(3, 5509),
    MEDIUM(6, 5510),
    LARGE(9, 5512),
    GIANT(12, 5514),
    COLOSSAL(40, 26784),
    ;

    private var count: Int = 0
        set(value) {
            value.coerceIn(0, capacity)
            field = value
        }

    fun getCount() = count

    fun getItem(): Item = Inventory.stream().nameContains(name).first()

    fun filled(): Boolean = count >= capacity
    fun fill(): Boolean {
        val pouch = getItem()
        val invEssence = essenceCount()
        if (!pouch.valid()) return false
        if (pouch.interact("Fill")) {
            waitFor(1000) { invEssence != essenceCount() }
            count += invEssence - essenceCount()
        } else {
            count = capacity
        }
        return filled()
    }

    fun empty(): Boolean {
        if (Inventory.isFull()) return false
        val pouch = getItem()
        val invEssence = essenceCount()
        if (!pouch.valid()) return false
        if (pouch.interact("Empty")) {
            waitFor { invEssence < essenceCount() }
            val essenceCount = essenceCount()
            if (essenceCount == 0) count = 0
            else count -= essenceCount - invEssence
            return invEssence < essenceCount()
        }
        return false
    }

    fun shouldRepair(): Boolean = getItem().id != perfectId

    companion object {
        fun essenceCount() = Inventory.stream().nameContains("essence").count().toInt()

        fun inInventory() = values().filter { it.getItem().valid() }
    }
}