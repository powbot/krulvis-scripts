package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross


class Cook(script: Tempoross) : Leaf<Tempoross>(script, "Cooking") {

    override fun execute() {
        val walkSpot = script.side.cookLocation
        val cookShrine = Objects.stream(50)
            .type(GameObject.Type.INTERACTIVE)
            .within(script.side.cookLocation, 5.0).name("Shrine").firstOrNull()
        if (cookShrine == null) {
            script.log.info("Walking to totem because cooking spot too far..")
            script.walkWhileDousing(script.side.totemLocation, false)
        } else if (cookShrine.distance() >= 10) {
            script.log.info("Walking to cooking spot because far away")
            val cookTile = if (script.side == Side.NORTH) script.side.cookLocation else cookShrine.tile
            script.walkWhileDousing(cookTile, false)
        } else if (me.animation() != FILLING_ANIM) {
            if (script.interactWhileDousing(cookShrine, "Cook-at", walkSpot, false)) {
                waitFor(long()) { me.animation() == FILLING_ANIM }
            }
        } else if (me.animation() == FILLING_ANIM) {
            script.log.info("Already cooking, turning camera to tether pole")
            val tetherPole = script.getTetherPole()
            if (tetherPole != null && !tetherPole.inViewport()) {
                Camera.turnTo(tetherPole)
            }
        }
    }

}