package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.CraftRunes
import org.powbot.krulvis.runecrafting.tree.leafs.MoveToAltar

class AtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "At altar?") {
    override val failedComponent: TreeComponent<Runecrafter> = MoveToAltar(script)
    override val successComponent: TreeComponent<Runecrafter> = CraftRunes(script)

    override fun validate(): Boolean {
        return (script.alter.getAltar()?.distance() ?: 99.0) < 10
    }
}