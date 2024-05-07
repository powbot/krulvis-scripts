package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager
import org.powbot.mobile.service.ScriptUploader

@ScriptManifest(
        name = "krul Thiever",
        description = "Pickpockets any NPC",
        author = "Krulvis",
        version = "1.1.4",
        markdownFileName = "Thiever.md",
        scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
        category = ScriptCategory.Thieving
)
@ScriptConfiguration.List(
        [
            ScriptConfiguration(
                    name = "Targets",
                    description = "What NPC to pickpocket?",
                    optionType = OptionType.NPC_ACTIONS,
                    defaultValue = "{\"id\":0,\"interaction\":\"Pickpocket\",\"mouseX\":449,\"mouseY\":262,\"rawEntityName\":\"<col=ffff00>Master Farmer\",\"rawOpcode\":11,\"var0\":51,\"widgetId\":48,\"index\":79,\"level\":-1,\"name\":\"Master Farmer\",\"strippedName\":\"Master Farmer\"}"
            ),
            ScriptConfiguration(
                    name = "CenterTile",
                    description = "Where to walk to after banking?",
                    optionType = OptionType.TILE,
                    defaultValue = "{\"floor\":0,\"x\":1249,\"y\":3750,\"rendered\":true}"
            ),
            ScriptConfiguration(
                    name = "MaxDistance",
                    description = "How far can the npc be from center tile?",
                    defaultValue = "15",
                    optionType = OptionType.INTEGER,
            ),
            ScriptConfiguration(
                    name = "Food",
                    description = "What food to use?",
                    defaultValue = "SALMON",
                    allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
            ),
            ScriptConfiguration(
                    name = "Food amount",
                    description = "How much food to take from bank?",
                    defaultValue = "5",
                    optionType = OptionType.INTEGER
            ),
            ScriptConfiguration(
                    name = "Droppables",
                    description = "What loot should be dropped (split with ,)",
                    defaultValue = "potato, onion, cabbage, tomato, marigold, nasturtium, rosemary, redberry, cadavaberry, dwellberry, guam, marrentill, tarromin, harralander",
                    optionType = OptionType.STRING
            ),
            ScriptConfiguration(
                    name = "Left-click",
                    description = "Force left-click on pickpocket?",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = "Prepare menu",
                    description = "Open menu right after pickpocketing?",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = "Dodgy necklace",
                    description = "Equip dodgy necklace?",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = "Stop on wandering",
                    description = "Stop script when the NPC is further than max distance from center tile?",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),

        ]
)
class Thiever : ATScript() {
    override fun createPainter(): ATPaint<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    override fun onStart() {
        super.onStart()
//        NpcActionEvent()
        dodgyNeck = getOption("Dodgy necklace")
        log.info("Droppables=[${droppables.all { it.isBlank() }}] empty=${droppables.isEmpty()}")
    }

    val centerTile by lazy { getOption<Tile>("CenterTile") }
    val food by lazy { Food.valueOf(getOption("Food")) }
    val stopOnWander by lazy { getOption<Boolean>("Stop on wandering") }
    val maxDistance by lazy { getOption<Int>("MaxDistance") }
    val target by lazy { getOption<List<NpcActionEvent>>("Targets") }
    val foodAmount by lazy { (getOption<Int>("Food amount")) }
    val prepare by lazy { (getOption<Boolean>("Prepare menu")) }
    val useMenu by lazy { !getOption<Boolean>("Left-click") }
    val droppables by lazy { getOption<String>("Droppables").split(",").map { it.trim() }.filterNot { it.isBlank() } }

    //Allow setting dodgyNeck to false after we're out of necklaces
    var dodgyNeck = false


    val dodgy = Equipment(emptyList(), org.powbot.api.rt4.Equipment.Slot.NECK, 21143)
    var nextPouchOpening = Random.nextInt(1, 28)

    fun getTarget(): Npc? {
        return Npcs.stream().within(centerTile, maxDistance).name(*target.map { it.name }.toTypedArray())
                .nearest(centerTile).firstOrNull()
    }

    fun stunned() = Players.local().animation() == 424

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            if (ScriptManager.state() == ScriptState.Running && prepare && Game.singleTapEnabled())
                getTarget()?.click()
        }
    }

    @ValueChanged("Left-click")
    fun onLeftClick(leftClick: Boolean) {
        if (leftClick) {
            updateOption("Prepare menu", false, OptionType.BOOLEAN)
        }
        updateVisibility("Prepare menu", !leftClick)
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        if (evt.quantityChange > 0 && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
            painter.paintBuilder.trackInventoryItems(id)
            log.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding ${evt.quantityChange} as start")
            painter.paintBuilder.items.forEach { row ->
                val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
                if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
            }
        }
    }

    fun coinPouch(): org.powbot.api.rt4.Item? = Inventory.stream().name("Coin pouch").firstOrNull()

    fun coinPouchCount() = coinPouch()?.stack ?: 0
}

fun main() {
    ScriptUploader().uploadAndStart("krul Thiever", "GIM", "emulator-5554", true, false)
//    Thiever().startScript("127.0.0.1", "GIM", false)
}