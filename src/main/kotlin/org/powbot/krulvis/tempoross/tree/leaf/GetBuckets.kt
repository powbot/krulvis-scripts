package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross

class GetBuckets(script: Tempoross) : Leaf<Tempoross>(script, "Getting Buckets") {
    override fun execute() {
        var bucketCount = script.getTotalBuckets()
        val bucketCrate = script.getBucketCrate()
        val timer = Timer(5000)
        script.log.info("Getting ${script.buckets} buckets, currently have: $bucketCount")
        while (!timer.isFinished() && bucketCount < script.buckets) {
            if (script.interactWhileDousing(
                    bucketCrate,
                    getBucketInteraction(bucketCount),
                    script.side.anchorLocation,
                    true
                )
            ) {
                waitFor(3000 + (500 * (bucketCrate?.distance() ?: 10).toInt())) {
                    bucketCount != script.getTotalBuckets().also { bucketCount = it }
                }
            }
        }
    }

    private fun getBucketInteraction(bucketCount: Int): String {
        val required = script.buckets - bucketCount
        return if (required >= 5) "Take-5" else "Take"
    }


}