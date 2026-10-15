package com.aldiandrew.neobrutallauncher

/**
 * Stable, side-effect-free layout selection for an app tile.
 */
internal object HomeTileLayoutRules {
    fun variant(packageName: String, activityName: String, tileSizeName: String): Int {
        val layoutKey = "$packageName/$activityName:$tileSizeName"
        return Math.floorMod(layoutKey.hashCode(), 5)
    }
}
