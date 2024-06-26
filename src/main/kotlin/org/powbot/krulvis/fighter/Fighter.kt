package org.powbot.krulvis.fighter

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.api.teleports.*
import org.powbot.krulvis.api.teleports.poh.LUNAR_ISLE_HOUSE_PORTAL
import org.powbot.krulvis.api.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.FEROX_ENCLAVE_JEWELLERY_BOX
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.fighter.Defender.currentDefenderIndex
import org.powbot.krulvis.fighter.tree.branch.ShouldStop
import org.powbot.mobile.rscache.loader.ItemLoader


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere. Supports defender collecting.",
    author = "Krulvis",
    version = "1.4.7",
    markdownFileName = "Fighter.md",
    scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
    category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            WARRIOR_GUILD_OPTION, "Collect defenders in the warrior guild",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            INVENTORY_OPTION, "What should your inventory look like?",
            optionType = OptionType.INVENTORY
        ),
        ScriptConfiguration(
            EQUIPMENT_OPTION, "What do you want to wear?",
            optionType = OptionType.EQUIPMENT
        ),
        ScriptConfiguration(
            MONSTERS_OPTION,
            "Click the NPC's you want to kill",
            optionType = OptionType.NPC_ACTIONS
        ),
        ScriptConfiguration(
            MONSTER_AUTO_DESTROY_OPTION, "Select if monster is finished off (Gargoyles)",
            OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            RADIUS_OPTION, "Kill radius", optionType = OptionType.INTEGER, defaultValue = "10"
        ),
        ScriptConfiguration(
            HOP_FROM_PLAYERS_OPTION, "Do you want to hop from players?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            PLAYER_HOP_COUNT_OPTION, "At how many players in radius do you want to hop?",
            optionType = OptionType.INTEGER, defaultValue = "1", visible = false
        ),
        ScriptConfiguration(
            USE_SAFESPOT_OPTION, "Do you want to force a safespot?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            WALK_BACK_TO_SAFESPOT_OPTION,
            "Walk to safespot after attacking?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false",
            visible = false
        ),
        ScriptConfiguration(
            CENTER_TILE_OPTION, "Get safespot/center tile",
            optionType = OptionType.TILE
        ),
        ScriptConfiguration(
            WAIT_FOR_LOOT_OPTION, "Wait for loot after kill?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            IRONMAN_DROPS_OPTION, description = "Only pick up your own drops.",
            optionType = OptionType.BOOLEAN, defaultValue = "true"
        ),
        ScriptConfiguration(
            LOOT_PRICE_OPTION, "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
        ),
        ScriptConfiguration(
            LOOT_OVERRIDES_OPTION,
            "Separate items with \",\" Start with \"!\" to never loot",
            optionType = OptionType.STRING,
            defaultValue = "Long bone, curved bone, clue, totem, !blue dragon scale, Scaly blue dragonhide, toadflax, irit, avantoe, kwuarm, snapdragon, cadantine, lantadyme, dwarf weed, torstol"
        ),
        ScriptConfiguration(
            BURY_BONES_OPTION, "Bury, Scatter or Offer bones&ashes.",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            BANK_TELEPORT_OPTION,
            "Teleport to bank",
            optionType = OptionType.STRING,
            defaultValue = EDGEVILLE_MOUNTED_GLORY,
            allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX]
        ),
        ScriptConfiguration(
            MONSTER_TELEPORT_OPTION,
            "Teleport to Monsters",
            optionType = OptionType.STRING,
            defaultValue = EDGEVILLE_MOUNTED_GLORY,
            allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX, LUNAR_ISLE_HOUSE_PORTAL]
        )
    ]
)
//</editor-fold>
class Fighter : ATScript() {

    override fun createPainter(): ATPaint<*> = FighterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this, ShouldStop(this))
    override fun onStart() {
        super.onStart()
        Defender.lastDefenderIndex = currentDefenderIndex()
    }

    //<editor-fold desc="UISubscribers">
    @ValueChanged(WARRIOR_GUILD_OPTION)
    fun onWGChange(inWG: Boolean) {
        if (inWG) {
            updateOption(USE_SAFESPOT_OPTION, Defender.killSpot(), OptionType.TILE)
            val npcAction = NpcActionEvent(
                0, 0, 10, 13729, 0,
                "Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
                447, 447, -1
            )
            updateOption(MONSTERS_OPTION, listOf(npcAction), OptionType.NPC_ACTIONS)
            updateOption(RADIUS_OPTION, 25, OptionType.INTEGER)
        }
    }

    @ValueChanged(USE_SAFESPOT_OPTION)
    fun onSafeSpotChange(useSafespot: Boolean) {
        updateVisibility(WALK_BACK_TO_SAFESPOT_OPTION, useSafespot)
    }

    @ValueChanged(HOP_FROM_PLAYERS_OPTION)
    fun onHopFromPlayersChange(hopFromPlayers: Boolean) {
        updateVisibility(PLAYER_HOP_COUNT_OPTION, hopFromPlayers)
    }


    //</editor-fold desc="Configuration">


    //Warrior guild
    val warriorTokens = 8851
    val warriorGuild by lazy { getOption<Boolean>(WARRIOR_GUILD_OPTION) }

    //Inventory
    private val inventoryOptions by lazy { getOption<Map<Int, Int>>(INVENTORY_OPTION) }
    val requiredInventory by lazy { inventoryOptions.filterNot { Potion.isPotion(it.key) } }
    val requiredPotions by lazy {
        inventoryOptions.filter { Potion.isPotion(it.key) }
            .mapNotNull { Pair(Potion.forId(it.key), it.value) }
            .groupBy {
                it.first
            }.map { it.key!! to it.value.sumOf { pair -> pair.second } }
    }
    val hasPrayPots by lazy { requiredPotions.any { it.first.skill == Constants.SKILLS_PRAYER } }

    //Equipment
    private val equipmentOptions by lazy { getOption<Map<Int, Int>>(EQUIPMENT_OPTION) }
    val equipment by lazy {
        equipmentOptions.filterNot { TeleportItem.isTeleportItem(it.key) }.map {
            Equipment(
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

    //Loot
    fun isLootWatcherActive() = lootWachter?.active == true
    var lootWachter: LootWatcher? = null
    val lootList = mutableListOf<GroundItem>()
    val ironman by lazy { getOption<Boolean>(IRONMAN_DROPS_OPTION) }
    val waitForLootAfterKill by lazy { getOption<Boolean>(WAIT_FOR_LOOT_OPTION) }
    val minLootPrice by lazy { getOption<Int>(LOOT_PRICE_OPTION) }
    val lootNameOptions by lazy {
        val names = getOption<String>(LOOT_OVERRIDES_OPTION).split(",")
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
        logger.info("Looting: [${names.joinToString()}]")
        names.toList()
    }
    val neverLoot by lazy {
        val trimmed = mutableListOf<String>()
        lootNameOptions
            .filter { it.startsWith("!") }
            .forEach { trimmed.add(it.replace("!", "")) }
        logger.info("Not looting: [${trimmed.joinToString()}]")
        trimmed
    }

    fun watchLootDrop(tile: Tile) {
        if (!isLootWatcherActive()) {
            logger.info("Waiting for loot at $tile")
            lootWachter = LootWatcher(tile, ammoId, lootList = lootList, isLoot = { it.isLoot() })
        } else {
            logger.info("Already watching loot at tile: $tile for loot")
        }
    }

    fun GroundItem.isLoot(): Boolean {
        if (warriorGuild && id() in Defender.defenders) return true
        val name = name().lowercase()
        return !neverLoot.contains(name) &&
                (lootNames.any { ln -> name.contains(ln) } || getPrice() * stackSize() >= minLootPrice)
    }

    fun loot(): List<GroundItem> =
        if (ironman) lootList else GroundItems.stream().within(centerTile(), radius).filter { it.isLoot() }

    var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

    //Banking option
    var forcedBanking = false
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }


    //monsters Killing spot
    private val monsters by lazy {
        getOption<List<NpcActionEvent>>(MONSTERS_OPTION).map { it.name }
    }
    val monsterDestroyed by lazy { getOption<Boolean>(MONSTER_AUTO_DESTROY_OPTION) }
    val monsterTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }
    var currentTarget: Npc? = null
    val aggressionTimer = Timer(10 * 60 * 1000)
    fun centerTile() = centerTile

    private val monsterNames: List<String> get() = if (superiorAppeared) SUPERIORS + monsters else monsters
    fun nearbyMonsters(): List<Npc> =
        Npcs.stream().within(centerTile(), radius.toDouble()).name(*monsterNames.toTypedArray()).nearest().list()

    fun target(): Npc? {
        val local = Players.local()
        val nearbyMonsters =
            nearbyMonsters().filterNot { it.healthBarVisible() && (it.interacting() != local || it.healthPercent() == 0) }
        val attackingMe = nearbyMonsters.firstOrNull { it.interacting() == local && it.reachable() }
        return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() }
    }

    //Safespot options
    val radius by lazy { getOption<Int>(RADIUS_OPTION) }
    val useSafespot by lazy { getOption<Boolean>(USE_SAFESPOT_OPTION) }
    val walkBack by lazy { getOption<Boolean>(WALK_BACK_TO_SAFESPOT_OPTION) }
    private val centerTile by lazy { getOption<Tile>(CENTER_TILE_OPTION) }
    val buryBones by lazy { getOption<Boolean>(BURY_BONES_OPTION) }
    fun shouldReturnToSafespot() =
        useSafespot && centerTile() != Players.local().tile() && (walkBack || Players.local().healthBarVisible())

    //Hop from players options
    val hopFromPlayers by lazy { getOption<Boolean>(HOP_FROM_PLAYERS_OPTION) }
    val playerHopAmount by lazy { getOption<Int>(PLAYER_HOP_COUNT_OPTION) }

    //Prayer options
    fun canActivatePrayer() = hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0
    fun canDeactivatePrayer() =
        Prayer.quickPrayer() && (!Players.local().healthBarVisible() || aggressionTimer.isFinished())


    //Custom slayer options
    var lastTask = false
    var superiorAppeared = false
    fun taskRemainder() = Varpbits.varpbit(394)

    @Subscribe
    fun onTickEvent(_e: TickEvent) {
        val interacting = me.interacting()
        if (interacting is Npc && interacting != Npc.Nil) {
            currentTarget = interacting
            val deathWatcher = npcDeathWatchers.firstOrNull { it.npc == interacting }
            if (deathWatcher == null || !deathWatcher.active) {
                npcDeathWatchers.add(
                    NpcDeathWatcher(
                        interacting,
                        monsterDestroyed
                    ) { watchLootDrop(interacting.tile()) })
            }
        }
        npcDeathWatchers.removeAll { !it.active }
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        val pot = Potion.forId(evt.itemId)
        val isTeleport = TeleportItem.isTeleportItem(id)
        if (evt.quantityChange > 0 && id != VIAL
            && id !in Defender.defenders
            && !requiredInventory.containsKey(id) && !equipmentOptions.containsKey(id)
            && !isTeleport && requiredPotions.none { it.first == pot }
        ) {
            painter.trackItem(id, evt.quantityChange)
        }
    }

    @Subscribe
    fun messageReceived(msg: MessageEvent) {
        if (msg.messageType != MessageType.Game) return
        if (msg.message.contains("so you can't take ")) {
            logger.info("Ironman message CANT TAKE type=${msg.messageType}")
            lootList.clear()
        }
        if (msg.message.contains("A superior foe has appeared")) {
            logger.info("Superior appeared message received: type=${msg.messageType}")
            superiorAppeared = true
        }
    }

    @Subscribe
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
}


fun main() {
    Fighter().startScript("127.0.0.1", "GIM", false)
}