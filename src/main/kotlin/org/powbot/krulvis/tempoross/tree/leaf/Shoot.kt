package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross


class Shoot(script: Tempoross) : Leaf<Tempoross>(script, "Shooting") {
    override fun execute() {
        val ammo = script.getAmmoCrate()
        val dest = Movement.destination()
        val nearAmmoBox = ammo != null && (ammo.distance() <= 2 || ammo.tile().distanceTo(dest) <= 2)
        val shooting = me.animation() == FILLING_ANIM && nearAmmoBox
        val offensiveLCTile = Npcs.stream().name("Lightning Cloud").nearest().first().tile().derive(0, -1)
        val myTile = me.tile()
        val safeTile = getSafeTile()
        if (shooting && offensiveLCTile == myTile && myTile != safeTile) {
            if (Movement.step(safeTile)) {
                waitFor { me.tile() == safeTile }
            }
        }
        if (!shooting && script.interactWhileDousing(ammo, "Fill", script.side.mastLocation, false)) {
            waitFor(long()) { me.animation() == FILLING_ANIM }
        }
    }

    fun getSafeTile(): Tile {
        return if (script.side == Side.NORTH) {
            script.side.mastLocation.derive(2, 0)
        } else {

            script.side.mastLocation.derive(-2, 0)
        }
    }

}