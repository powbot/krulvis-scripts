package org.powbot.krulvis.api.extensions.items

import org.powbot.api.event.MessageEvent

object GemBag : Item {

    val GEM_BAG_CLOSED = 12020
    val GEM_BAG_OPEN = 24481

    override val ids: IntArray = intArrayOf(GEM_BAG_CLOSED, GEM_BAG_OPEN)

    var shouldEmpty = true

    override fun hasWith(): Boolean {
        return inInventory()
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }

    @com.google.common.eventbus.Subscribe
    fun messageEvent(evt: MessageEvent) {
        if (evt.message == "The gem bag is now empty.") {
            shouldEmpty = false
        }
    }
}