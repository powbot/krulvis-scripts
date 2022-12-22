package org.powbot.krulvis.giantsfoundry.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import org.powbot.krulvis.giantsfoundry.MouldType
import org.powbot.krulvis.giantsfoundry.tree.leaf.*

class HasAssignment(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has assignment?") {
    override val failedComponent: TreeComponent<GiantsFoundry> = GetAssignment(script)
    override val successComponent: TreeComponent<GiantsFoundry> = CanTakeSword(script)

    override fun validate(): Boolean {
        return script.hasCommission()
    }
}

class CanTakeSword(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Can take sword?") {
    override val failedComponent: TreeComponent<GiantsFoundry> = HasSetupMoulds(script)
    override val successComponent: TreeComponent<GiantsFoundry> =
        InteractWithObject(script, "Mould jig (Poured metal)", "Pick-up") { script.isSmithing() }

    override fun validate(): Boolean {
        return script.areBarsPoured()
    }
}

class HasSetupMoulds(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has best moulds?") {
    override val failedComponent: TreeComponent<GiantsFoundry> = SetupMoulds(script)
    override val successComponent: TreeComponent<GiantsFoundry> = IsCrucibleFull(script)

    override fun validate(): Boolean {
        return MouldType.selectedAll()
    }
}

class IsCrucibleFull(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Is crucible full?") {
    override val failedComponent: TreeComponent<GiantsFoundry> = HasBars(script)
    override val successComponent: TreeComponent<GiantsFoundry> =
        InteractWithObject(script, "Crucible (full)", "Pour") {
            script.areBarsPoured()
        }

    override fun validate(): Boolean {
        return script.getCrucibleBars().sumOf { it.second } >= 28
    }
}

class HasBars(script: GiantsFoundry) : Branch<GiantsFoundry>(script, "Has bars in inventory") {
    override val failedComponent: TreeComponent<GiantsFoundry> = TakeBarsFromBank(script)
    override val successComponent: TreeComponent<GiantsFoundry> = FillCrucible(script)

    override fun validate(): Boolean {
        return script.getInvBar() != null
    }
}

