package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.runecrafting.Runecrafter

class MoveToAltar(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving to Altar") {
    override fun execute() {
        script.alter.pathToAltar.traverse(1)
    }
}