package org.powbot.krulvis.api.script

import org.powbot.api.Preferences
import org.powbot.api.Random
import org.powbot.api.event.RenderEvent
import org.powbot.api.script.tree.TreeScript
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.randoms.BondPouch
import org.powbot.krulvis.api.extensions.randoms.RandomHandler
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.mobile.drawing.Rendering
import java.io.File

abstract class ATScript : TreeScript() {

    override fun onStart() {
        log.info("Starting..")
        addPaint(painter.buildPaint(painter.paintBuilder))
        val username = Preferences.getString("username")
        log.info("Username: $username")
    }

    val painter by lazy { createPainter() }

    abstract fun createPainter(): ATPaint<*>

    val timer = Timer()
    val oddsModifier = OddsModifier()
    val walkDelay = DelayHandler(500, 700, oddsModifier, "Walk Delay")
    var nextRun: Int = Random.nextInt(1, 6)
    val randomHandlers = mutableListOf<RandomHandler>(BondPouch())

    override fun poll() {
        val rh = randomHandlers.firstOrNull { it.validate() }
        if (rh != null) rh.execute() else super.poll()
    }

    @com.google.common.eventbus.Subscribe
    fun onRender(e: RenderEvent) {
        painter.paintCustom(Rendering)
    }

    /**
     * Called whenever the script is stopped, This doesn't actually stop the script
     */
    override fun onStop() {
//        if (timer.getElapsedTime() > 50 * 60 * 1000) {
//            val img = painter.saveProgressImage()
//            val link = Imgur.upload(img)
//            println("Uploaded proggy to imgur: $link")
//            Discord.upload(link)
//        }
    }

}
