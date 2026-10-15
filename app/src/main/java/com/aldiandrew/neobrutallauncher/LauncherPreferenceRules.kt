package com.aldiandrew.neobrutallauncher

/**
 * Pure preference rules shared by persistence and backup/restore code.
 */
internal object LauncherPreferenceRules {
    fun normalizePinnedCount(value: Int): Int = when (value) {
        in Int.MIN_VALUE..3 -> 3
        else -> 5
    }
}
