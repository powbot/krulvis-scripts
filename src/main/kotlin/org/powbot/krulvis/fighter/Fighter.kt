package org.powbot.krulvis.fighter

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.powbot.api.Events
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.paint.TextPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.fighter.Defender.currentDefenderIndex
import org.powbot.krulvis.fighter.slayer.Master
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.krulvis.fighter.tree.branch.ShouldStop
import org.powbot.mobile.rscache.loader.ItemLoader
import kotlin.math.round

@ScriptManifest(
        name = "krul Fighter",
        description = "Fights anything, anywhere. Supports defender collecting.",
        author = "Krulvis",
        version = "1.4.4",
        markdownFileName = "Fighter.md",
        scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
        category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
        [
            ScriptConfiguration(
                    "Warrior guild", "Collect defenders in the warrior guild",
                    optionType = OptionType.BOOLEAN, defaultValue = "false"
            ),
            ScriptConfiguration(
                    "Slayer", "Do slayer tasks?", optionType = OptionType.BOOLEAN, defaultValue = "false", visible = false
            ),
            ScriptConfiguration(
                    name = "Slayer Master",
                    //Fuck "KRYSTILIA" for now
                    allowedValues = ["TURAEL", "SPRIA", "MAZCHNA", "VANNAKA", "CHAELDAR", "KONAR", "NIEVE", "STEVE", "DURADEL"],
                    description = "What slayer master do you want to use?",
                    defaultValue = "KONAR",
                    visible = false
            ),
            ScriptConfiguration(
                    "Inventory", "What should your inventory look like?",
                    optionType = OptionType.INVENTORY
            ),
            ScriptConfiguration(
                    "Equipment", "What do you want to wear?",
                    optionType = OptionType.EQUIPMENT
            ),
            ScriptConfiguration(
                    "Monsters",
                    "Click the NPC's you want to kill",
                    optionType = OptionType.NPC_ACTIONS
            ),
            ScriptConfiguration(
                    "Radius", "Kill radius", optionType = OptionType.INTEGER, defaultValue = "10"
            ),
            ScriptConfiguration(
                    "Use safespot", "Do you want to force a safespot?",
                    optionType = OptionType.BOOLEAN, defaultValue = "false"
            ),
            ScriptConfiguration(
                    "Safespot", "Get safespot / centertile",
                    optionType = OptionType.TILE
            ),
            ScriptConfiguration(
                    "WaitForLoot", "Wait for loot after kill?",
                    optionType = OptionType.BOOLEAN, defaultValue = "false"
            ),
            ScriptConfiguration(
                    "Ironman", description = "Only pick up your own drops.",
                    optionType = OptionType.BOOLEAN, defaultValue = "true"
            ),
            ScriptConfiguration(
                    "Loot price", "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
            ),
            ScriptConfiguration(
                    "Always loot",
                    "Separate items with \",\" Start with \"!\" to never loot",
                    optionType = OptionType.STRING,
                    defaultValue = "Long bone, curved bone, ensouled, rune, clue, totem, grimy, !blue dragon scale"
            ),
            ScriptConfiguration(
                    "Bury bones", "Bury, Scatter or Offer bones.",
                    optionType = OptionType.BOOLEAN, defaultValue = "false"
            ),
            ScriptConfiguration(
                    "Bank", "Choose bank", optionType = OptionType.STRING, defaultValue = "FEROX_ENCLAVE",
                    allowedValues = ["NEAREST", "ARDOUGNE_NORTH_BANK", "ARDOUGNE_SOUTH_BANK", "AL_KHARID_BANK", "BURTHORPE_BANK", "CANIFIS_BANK", "CATHERBY_BANK", "CASTLE_WARS_BANK", "DRAYNOR_BANK", "EDGEVILLE_BANK",
                        "FALADOR_WEST_BANK", "FALADOR_EAST_BANK", "FARMING_GUILD_85", "FARMING_GUILD_65", "FEROX_ENCLAVE", "GRAND_EXCHANGE", "HOSIDIUS_BEST_BANK_SPOT", "MISCELLANIA_BANK", "KOUREND_TOP_BUILDING", "LUMBRIDGE_TOP", "LUMBRIDGE_CASTLE_BANK",
                        "VARROCK_WEST_BANK", "VARROCK_EAST_BANK", "GNOME_STRONGHOLD_BANK", "FISHING_GUILD_BANK", "MINING_GUILD", "MOTHERLOAD_MINE", "MOTHERLOAD_MINE_DEPOSIT", "PRIFIDDINAS", "PORT_SARIM_DB", "SEERS_BANK",
                        "SHAYZIEN_NORTH_CHEST", "SHAYZIEN_SOUTH_BOOTH", "SHANTAY_PASS_BANK", "SHILO_GEM_MINE", "TZHAAR_BANK", "WOODCUTTING_GUILD", "WARRIORS_GUILD", "WINTERTODT", "YANILLE_BANK", "FOSSIL_ISLAND_CAMP"]
            ),
        ]
)
class Fighter : ATScript() {

    override fun createPainter(): ATPaint<*> = FighterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this, ShouldStop(this))


    @ValueChanged("Warrior guild")
    fun onWGChange(inWG: Boolean) {
        if (inWG) {
            updateOption("Safespot", Defender.killSpot(), OptionType.TILE)
            val npcAction = NpcActionEvent(
                    0, 0, 10, 13729,
                    "Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
                    447, 447, -1
            )
            updateOption("Monsters", listOf(npcAction), OptionType.NPC_ACTIONS)
            updateOption("Radius", 25, OptionType.INTEGER)
            updateOption("Bank", "WARRIORS_GUILD", OptionType.STRING)
        }
    }

    @ValueChanged("Slayer")
    fun onSlayerChange(slayer: Boolean) {
        updateVisibility("Slayer Master", slayer)
        updateVisibility("Monsters", !slayer)
        updateVisibility("Safespot", !slayer)
        updateVisibility("Use safespot", !slayer)
        updateVisibility("Radius", !slayer)
    }

    @com.google.common.eventbus.Subscribe
    fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
        if (pcce.checkboxId == "stopAfterTask") {
            lastTask = pcce.checked
            val painter = painter as FighterPainter
            if (pcce.checked && !painter.paintBuilder.items.contains(painter.slayerTracker)) {
                val index =
                        painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem && it.id == "stopAfterTask" } }
                painter.paintBuilder.items.add(index, painter.slayerTracker)
            } else if (!pcce.checked && painter.paintBuilder.items.contains(painter.slayerTracker)) {
                painter.paintBuilder.items.remove(painter.slayerTracker)
            }

        }
    }


    override fun onStart() {
        super.onStart()
        Defender.lastDefenderIndex = currentDefenderIndex()
        if (doSlayer) {
            slayer = Slayer(Master.valueOf(getOption("Slayer Master")), log)
            Events.register(slayer)
            val taskCheckBoxIndex =
                    painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem } }
            painter.paintBuilder.add(
                    listOf(
                            TextPaintItem { "Task remainder:" },
                            TextPaintItem { Slayer.taskRemainder().toString() }),
                    taskCheckBoxIndex
            )
            painter.paintBuilder.addString("Current Task") { slayer.currentTask?.target?.name?.lowercase() }
        }
    }

    //Slayer
    val doSlayer by lazy { getOption<Boolean>("Slayer") }
    lateinit var slayer: Slayer
    fun killItem() = if (doSlayer) slayer.killItem() else null

    //Warrior guild
    val warriorTokens = 8851
    val warriorGuild by lazy { getOption<Boolean>("Warrior guild") }

    val useSafespot by lazy { getOption<Boolean>("Use safespot") }
    private val safespot by lazy { getOption<Tile>("Safespot") }
    val buryBones by lazy { getOption<Boolean>("Bury bones") }

    //Inventory
    private val inventoryOptions by lazy { getOption<Map<Int, Int>>("Inventory") }
    val inventory by lazy { inventoryOptions.filterNot { Potion.isPotion(it.key) } }
    val potions by lazy {
        inventoryOptions.filter { Potion.isPotion(it.key) }
                .mapNotNull { Pair(Potion.forId(it.key), it.value) }
                .groupBy {
                    it.first
                }.map { it.key!! to it.value.sumOf { pair -> pair.second } }
    }
    val hasPrayPots by lazy { potions.any { it.first == Potion.PRAYER } }

    //Equipment
    private val equipmentOptions by lazy { getOption<Map<Int, Int>>("Equipment") }
    val equipment by lazy {
        equipmentOptions.filterNot { TeleportItem.isTeleportItem(it.key) }.map {
            Equipment(
                    emptyList(),
                    Slot.forIndex(it.value),
                    it.key
            )
        }
    }
    val teleportItems by lazy {
        equipmentOptions.keys.mapNotNull {
            TeleportItem.getTeleportItem(it)
        }
    }

    //Killing spot
    val monsters by lazy {
        getOption<List<NpcActionEvent>>("Monsters").map { it.name }
    }
    val radius by lazy { getOption<Int>("Radius") }
    val waitForLootAfterKill by lazy { getOption<Boolean>("WaitForLoot") }
    var currentTarget: Npc? = null


    //Loot
    var waitingForLootTile: Tile? = null

    fun isWaitingForLoot() = waitForLootJob?.isActive == true
    var waitForLootJob: Job? = null
    val lootList = mutableListOf<GroundItem>()
    val ironman by lazy { getOption<Boolean>("Ironman") }
    val minLoot by lazy { getOption<Int>("Loot price") }
    val lootNameOptions by lazy {
        val names = getOption<String>("Always loot").split(",")
        val trimmed = mutableListOf<String>()
        names.forEach { trimmed.add(it.trim().lowercase()) }
        trimmed
    }
    var ammoId: Int = -1
    val lootNames by lazy {
        val names = lootNameOptions.filterNot { it.startsWith("!") }.toMutableList()
        val ammo = equipment.firstOrNull { it.slot == Slot.QUIVER }
        if (ammo != null) {
            ammoId = ammo.id
            names.add(ItemLoader.lookup(ammoId)?.name()?.lowercase() ?: "nulll")
        }
        names.add("brimstone key")
        names.add("ancient shard")
        log.info("Looting: [${names.joinToString()}]")
        names.toList()
    }
    val neverLoot by lazy {
        val trimmed = mutableListOf<String>()
        lootNameOptions
                .filter { it.startsWith("!") }
                .forEach { trimmed.add(it.replace("!", "")) }
        log.info("Not looting: [${trimmed.joinToString()}]")
        trimmed
    }

    val bank by lazy {
        val b = getOption<String>("Bank")
        if (b == "NEAREST") {
            Bank.getNearestBank()
        } else {
            BankLocation.valueOf(b)
        }
    }
    var forcedBanking = false


    fun centerTile() = if (doSlayer) slayer.currentTask!!.location.centerTile else safespot

    fun nearbyMonsters(): List<Npc> {
        return if (doSlayer) {
            slayer.currentTask!!.nearbyMonsters()
        } else Npcs.stream().within(centerTile(), radius.toDouble()).name(*monsters.toTypedArray()).nearest().list()
    }


    fun target(): Npc? {
        val local = Players.local()
        val nearbyMonsters =
                nearbyMonsters().filterNot { it.healthBarVisible() && (it.interacting() != local || it.healthPercent() == 0) }
        val attackingMe = nearbyMonsters.firstOrNull { it.interacting() == local && it.reachable() }
        return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() }
    }

    fun taskRemainder() = Varpbits.varpbit(394)


    fun watchLootDrop(tile: Tile) {
        if (waitForLootJob?.isActive != true) {
            log.info("Waiting for loot at $tile")
            val startMilis = System.currentTimeMillis()
            waitingForLootTile = tile
            waitForLootJob = GlobalScope.launch {
                val watcher = LootWatcher(tile, ammoId, isLoot = { it.isLoot() })
                val loot = watcher.waitForLoot()
                log.info("Waiting for loot took: ${round((System.currentTimeMillis() - startMilis) / 100.0) / 10.0} seconds")
                lootList.addAll(loot)
                watcher.unregister()
                waitingForLootTile = null
            }
        } else {
            log.info("Already watching loot at tile: $tile for loot")
        }
    }

    fun GroundItem.isLoot(): Boolean {
        if (warriorGuild && id() in Defender.defenders) return true
        val name = name().lowercase()
        return !neverLoot.contains(name) &&
                (lootNames.any { ln -> name.contains(ln) } || getPrice() * stackSize() >= minLoot)
    }


    fun loot(): List<GroundItem> =
            if (ironman) lootList else GroundItems.stream().within(centerTile(), radius).filter { it.isLoot() }

    var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        val interacting = me.interacting()
        if (interacting is Npc && interacting != Npc.Nil) {
            currentTarget = interacting
            val watcher = npcDeathWatchers.firstOrNull { it.npc == interacting }
            if (watcher == null || !watcher.active) {
                npcDeathWatchers.add(NpcDeathWatcher(interacting) { watchLootDrop(interacting.tile()) })
            }
        }
        npcDeathWatchers.removeAll { !it.active }
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        val pot = Potion.forId(evt.itemId)
        val isTeleport = TeleportItem.isTeleportItem(id)
        if (evt.quantityChange > 0 && id != VIAL
                && id !in Defender.defenders
                && !inventory.containsKey(id) && !equipmentOptions.containsKey(id)
                && !isTeleport && potions.none { it.first == pot }
        ) {
            if (painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
                painter.paintBuilder.trackInventoryItems(id)
                log.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding ${evt.quantityChange} as start")
                painter.paintBuilder.items.forEach { row ->
                    val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
                    if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
                }
            }
        }
    }

    @com.google.common.eventbus.Subscribe
    fun messageReceived(msg: MessageEvent) {
        if (msg.message.contains("so you can't take ")) {
            lootList.clear()
        }
    }

    var lastTask = false

}


fun main() {
    Fighter().startScript("127.0.0.1", "GIM", false)
}