package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.ModelInteractionType
import org.powbot.api.Point
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "", priv = true)
@ScriptConfiguration.List(
        [
            ScriptConfiguration(
                    name = "rocks",
                    description = "Click som rocks",
                    optionType = OptionType.GAMEOBJECT_ACTIONS,
            ),
            ScriptConfiguration(
                    name = "Extra info",
                    description = "Here comes extra info \n new lines?",
                    optionType = OptionType.INFO
            ),
            ScriptConfiguration(
                    name = "tile",
                    description = "Get Tile?",
                    optionType = OptionType.TILE,
                    defaultValue = "{\"floor\":0,\"x\":1640,\"y\":3944,\"rendered\":true}"
            ),
            ScriptConfiguration(
                    name = "rocks1",
                    description = "NPCS?",
                    optionType = OptionType.NPC_ACTIONS,
            ),
            ScriptConfiguration(
                    name = "rocks1",
                    description = "ALL ACTIONS?",
                    optionType = OptionType.GAME_ACTIONS,
            ),
            ScriptConfiguration(
                    name = "rocks2",
                    description = "Want to have 0?",
                    optionType = OptionType.BOOLEAN,
                    defaultValue = "true"
            ),
            ScriptConfiguration(
                    name = "rocks3",
                    description = "Select",
                    optionType = OptionType.STRING,
                    defaultValue = "2",
                    allowedValues = ["1", "2", "3"]
            ),
        ]
)
class TestScript : ATScript() {
    override fun createPainter(): ATPaint<*> = TestPainter(this)

    var origin = Tile(3212, 3216, 0) //varrock mine

    var collisionMap: Array<IntArray> = emptyArray()

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3231, 3207, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks") }
    var path = emptyList<Edge<*>?>()
    var obj: GameObject? = null
    var npc: Npc? = null
    var lastLoop = System.currentTimeMillis()

    val points = mutableListOf<Point>()


    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        val ladder = Objects.stream().name("Altar").first()
//        points.clear()
        log.info("Ladder= ${ladder}, tile=${ladder.tile}")
    }

    //Tile(x=3635, y=3362, floor=0)
    //Tile(x=3633, y=3359, floor=0)

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }

    @com.google.common.eventbus.Subscribe
    fun onMsg(e: MessageEvent) {
        log.info("MSG: \n Type=${e.type}, msg=${e.message}")
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        if (!painter.paintBuilder.trackingInventoryItem(evt.itemId)) {
//            painter.paintBuilder.trackInventoryItem(evt)
        }
    }

}

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {

    fun combatWidget(): Widget? {
        return Widgets.stream().firstOrNull { it.components().any { c -> c.text() == "Bloodveld" } }
    }


    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
                .addString("Container") {
                    val comp = Widgets.component(718, 9)
                    "scrollY=${comp.scrollY()}, screenPoint=${comp.screenPoint()}"
                }
                .addString("Component1") {
                    val comp = Widgets.component(718, 9).component(85)
                    "screenPoint=${comp.screenPoint()}"
                }
                .build()
    }

    override fun paintCustom(g: Rendering) {
        g.setColor(Color.RED)
//        val ladder = script.ladder ?: return
//        ladder.model()?.draw(ladder.localX(), ladder.localY())
//        script.points.forEach {
//            g.drawRect(it.x - 1, it.y - 1, 2, 2)
//        }
//        script.log.info("Points=${script.points.size}")
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestScript().startScript("127.0.0.1", "GIM", true)
}
