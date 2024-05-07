package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Skills
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import kotlin.system.measureTimeMillis

class Pickpocket(script: Thiever) : Leaf<Thiever>(script, "Pickpocket") {


    override fun execute() {
        val target = script.getTarget()
        if (target != null && Bank.close()) {
            val xp = Skills.experience(Constants.SKILLS_THIEVING)
            if (walkAndInteract(target, "Pickpocket", script.useMenu)) {
                if (waitFor(Random.nextInt(1000, 1250)) {
                        Skills.experience(Constants.SKILLS_THIEVING) > xp ||
                                script.stunned()
                    }) {
                    if (script.stunned()) {
                        script.log.info("Waiting to repickpocket...")
                        if (script.coinPouchCount() >= script.nextPouchOpening) {
                            script.nextPouchOpening = Random.nextInt(1, 28)
                            script.coinPouch()?.interact("Open-all")
                        }
                        script.log.info(
                            "Waited for: ${
                                measureTimeMillis {
                                    sleep(Random.nextInt(4500, 5000))
                                }
                            }"
                        )
                    }
                }
            }
        }
    }
}