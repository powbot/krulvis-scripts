package org.powbot.krulvis.runecrafting

import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class RCPainter(script: Runecrafter) : ATPaint<Runecrafter>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.trackSkill(Skill.Runecrafting)
                .trackInventoryItems(Rune.ASTRAL.id)

        return paintBuilder.build()
    }
}