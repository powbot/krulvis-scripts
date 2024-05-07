package org.powbot.krulvis.tempoross

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Varpbits
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class TemporossPaint(script: Tempoross) : ATPaint<Tempoross>(script, 110, 210) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.addString("Reward credits:") { "${script.rewardGained}, ${script.timer.getPerHour(script.rewardGained)}/hr" }
            .addString("Points obtained:") { "${if (script.rounds > 0) script.pointsObtained / script.rounds else "-"}/round" }
            .trackSkill(Skill.Fishing)
            .addCheckbox("Last game:", "lastGame", false)
            .addCheckbox("Paint debug:", "paintDebug", false)
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {
        if (script.debugPaint) {
            g.setColor(Color.CYAN)
            val polygon = script.side.area.getPolygon()
            val xpoints = polygon.xpoints
            val ypoints = polygon.ypoints
            for (i in 0..polygon.npoints - 2) {
                val t = Tile(xpoints[i], ypoints[i]).mapPoint()
                val t2 = Tile(xpoints[i + 1], ypoints[i + 1]).mapPoint()
                g.drawLine(t.x, t.y, t2.x, t2.y)
            }
            val blockedTiles = script.burningTiles.toList()
            val paths = script.triedPaths.toList()
            blockedTiles.forEach {
                val t = it
                if (t != Tile.Nil) {
                    it.drawOnScreen(null, Color.RED)
                }
            }
            val lc = Npcs.stream().name("Lightning Cloud").nearest().firstOrNull()
            lc?.tile()?.drawOnScreen(lc.orientation().toString())
            g.drawString("Tethered=${script.isTethering()}, varp=${Varpbits.varpbit(2933)}", 50, 250)
            g.drawString("Blocked Tiles = ${blockedTiles.size}", 50, 260)
            if (blockedTiles.isNotEmpty() && paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val dangerous = tiles.any { script.burningTiles.contains(it) }
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            null,
                            if (blockedTiles.contains(tile)) Color.BLACK else if (dangerous) Color.ORANGE else Color.GREEN
                        )
                    }
                }
            }
        }
    }

}