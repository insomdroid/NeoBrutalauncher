package com.aldiandrew.neobrutallauncher

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class DesignPreset(val label: String) {
    NEO_BRUTAL_CLASSIC("NEO BRUTAL DEFAULT"),
    VIOLET_GRID("VIOLET GRID")
}

class LauncherPreferences(context: Context) {

    internal val prefs = context.getSharedPreferences(
        "neo_brutal_launcher_preferences",
        Context.MODE_PRIVATE
    )

    fun theme(): ThemePreference {
        return when (prefs.getString(KEY_THEME, ThemePreference.SYSTEM.name)) {
            ThemePreference.LIGHT.name -> ThemePreference.LIGHT
            ThemePreference.DARK.name -> ThemePreference.DARK
            else -> ThemePreference.SYSTEM
        }
    }

    fun setTheme(value: ThemePreference) {
        prefs.edit().putString(KEY_THEME, value.name).apply()
    }

    fun designPreset(): DesignPreset {
        return runCatching {
            DesignPreset.valueOf(
                prefs.getString(KEY_DESIGN_PRESET, DesignPreset.NEO_BRUTAL_CLASSIC.name)
                    ?: DesignPreset.NEO_BRUTAL_CLASSIC.name
            )
        }.getOrDefault(DesignPreset.NEO_BRUTAL_CLASSIC)
    }

    fun setDesignPreset(value: DesignPreset) {
        prefs.edit().putString(KEY_DESIGN_PRESET, value.name).apply()
    }

    fun use24Hour(): Boolean {
        return prefs.getBoolean(KEY_24_HOUR, true)
    }

    fun setUse24Hour(value: Boolean) {
        prefs.edit().putBoolean(KEY_24_HOUR, value).apply()
    }

    fun showAmPm(): Boolean {
        return prefs.getBoolean(KEY_SHOW_AM_PM, true)
    }

    fun setShowAmPm(value: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_AM_PM, value).apply()
    }

    fun homeAppCount(): Int {
        return normalizePinnedCount(prefs.getInt(KEY_HOME_APP_COUNT, 5))
    }

    fun homeAppsInitialized(): Boolean = prefs.getBoolean(KEY_HOME_APPS_INITIALIZED, false)

    fun setHomeAppsInitialized(value: Boolean) {
        prefs.edit().putBoolean(KEY_HOME_APPS_INITIALIZED, value).apply()
    }

    fun onboardingCompleted(): Boolean {
        if (prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)) return true

        // Existing installations already have launcher preferences, so do not interrupt
        // an upgrade with first-run onboarding.
        return prefs.contains(KEY_FAVORITES) ||
            prefs.contains(KEY_HOME_APP_COUNT) ||
            prefs.contains(KEY_HOME_APPS_INITIALIZED)
    }

    fun setOnboardingCompleted(value: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, value).apply()
    }

    fun lastMusicPackage(): String? =
        prefs.getString(KEY_LAST_MUSIC_PACKAGE, null)?.takeIf { it.isNotBlank() }

    fun setLastMusicPackage(value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_LAST_MUSIC_PACKAGE) else putString(KEY_LAST_MUSIC_PACKAGE, value)
        }.apply()
    }

    fun setHomeAppCount(value: Int) {
        prefs.edit().putInt(KEY_HOME_APP_COUNT, normalizePinnedCount(value)).apply()
    }

    private fun normalizePinnedCount(value: Int): Int =
        LauncherPreferenceRules.normalizePinnedCount(value)


    fun showWeather(): Boolean {
        return prefs.getBoolean(KEY_WEATHER, false)
    }

    fun setShowWeather(value: Boolean) {
        prefs.edit().putBoolean(KEY_WEATHER, value).apply()
    }

    fun chatNotificationPackages(): List<String> =
        prefs.getStringSet(KEY_CHAT_NOTIFICATION_PACKAGES, emptySet())
            ?.toList()
            .orEmpty()
            .filter { it.isNotBlank() }
            .take(1)

    fun setChatNotificationPackages(values: List<String>) {
        prefs.edit()
            .putStringSet(KEY_CHAT_NOTIFICATION_PACKAGES, values.filter { it.isNotBlank() }.distinct().take(1).toSet())
            .apply()
    }

    fun favorites(): Set<String> {
        return prefs.getStringSet(KEY_FAVORITES, emptySet())?.toSet().orEmpty()
    }

    fun setFavorites(values: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, values.toSet()).apply()
    }

    fun clearFavorites() {
        prefs.edit().remove(KEY_FAVORITES).apply()
    }

    fun tilePositions(): Map<String, NeoTilePosition> {
        return prefs.getStringSet(KEY_TILE_POSITIONS, emptySet())
            ?.mapNotNull { entry ->
                val parts = entry.split("|")
                if (parts.size != 3) {
                    return@mapNotNull null
                }

                val tileId = parts[0]
                val column = parts[1].toIntOrNull()
                val row = parts[2].toIntOrNull()

                if (
                    tileId.isBlank() ||
                    column == null ||
                    row == null ||
                    column < 0 ||
                    row < 0
                ) {
                    null
                } else {
                    tileId to NeoTilePosition(
                        column = column,
                        row = row
                    )
                }
            }
            ?.toMap()
            .orEmpty()
    }

    fun setTilePositions(values: Map<String, NeoTilePosition>) {
        val encoded = values.mapNotNull { (tileId, position) ->
            if (
                tileId.isBlank() ||
                position.column < 0 ||
                position.row < 0
            ) {
                null
            } else {
                tileId + "|" + position.column + "|" + position.row
            }
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_TILE_POSITIONS, encoded)
            .apply()
    }

    fun tileSizes(): Map<String, NeoTileSize> {
        return prefs.getStringSet(KEY_TILE_SIZES, emptySet())
            ?.mapNotNull { entry ->
                val index = entry.lastIndexOf("|")
                if (index <= 0) {
                    return@mapNotNull null
                }

                val tileId = entry.substring(0, index)
                val size = runCatching {
                    NeoTileSize.valueOf(entry.substring(index + 1))
                }.getOrNull()

                if (tileId.isBlank() || size == null) {
                    null
                } else {
                    tileId to size
                }
            }
            ?.toMap()
            .orEmpty()
    }

    fun setTileSizes(values: Map<String, NeoTileSize>) {
        val encoded = values.mapNotNull { (tileId, size) ->
            if (tileId.isBlank()) {
                null
            } else {
                tileId + "|" + size.name
            }
        }.toSet()

        prefs.edit()
            .putStringSet(KEY_TILE_SIZES, encoded)
            .apply()
    }

    fun iconPackPackage(): String? =
        prefs.getString(KEY_ICON_PACK_PACKAGE, null)?.takeIf { it.isNotBlank() }

    fun setIconPackPackage(value: String?) {
        prefs.edit().apply {
            if (value.isNullOrBlank()) remove(KEY_ICON_PACK_PACKAGE) else putString(KEY_ICON_PACK_PACKAGE, value)
        }.apply()
    }

    fun appTileContentMode(): TileContentMode {
        return runCatching {
            TileContentMode.valueOf(
                prefs.getString(KEY_APP_TILE_CONTENT_MODE, TileContentMode.ICON_TEXT.name)
                    ?: TileContentMode.ICON_TEXT.name
            )
        }.getOrDefault(TileContentMode.ICON_TEXT)
    }

    fun setAppTileContentMode(value: TileContentMode) {
        prefs.edit()
            .putString(KEY_APP_TILE_CONTENT_MODE, value.name)
            .apply()
    }


    fun typographyStyle(): TypographyStyle {
        return when (prefs.getString(KEY_CLOCK_STYLE, TypographyStyle.DEFAULT.name)) {
            TypographyStyle.CONDENSED.name -> TypographyStyle.CONDENSED
            else -> TypographyStyle.DEFAULT
        }
    }

    fun setTypographyStyle(value: TypographyStyle) {
        prefs.edit().putString(KEY_CLOCK_STYLE, value.name).apply()
    }

    fun wallpaperUri(): String? = prefs.getString(KEY_WALLPAPER_URI, null)

    fun setWallpaperUri(value: String?) {
        prefs.edit().apply {
            if (value == null) remove(KEY_WALLPAPER_URI) else putString(KEY_WALLPAPER_URI, value)
        }.apply()
    }

    fun homeAppOrder(): List<String> {
        val raw = prefs.getString(KEY_HOME_APP_ORDER, null)
        if (raw.isNullOrBlank()) return emptyList()

        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val key = array.optString(index).trim()
                    if (key.isNotEmpty()) add(key)
                }
            }
        }.getOrDefault(emptyList())
    }

    fun setHomeAppOrder(values: List<String>) {
        val array = JSONArray()
        values.filter { it.isNotBlank() }.distinct().forEach(array::put)
        prefs.edit().putString(KEY_HOME_APP_ORDER, array.toString()).apply()
    }

    fun excludedHomeApps(): Set<String> {
        return prefs.getStringSet(KEY_EXCLUDED_HOME_APPS, emptySet())?.toSet().orEmpty()
    }

    fun setExcludedHomeApps(values: Set<String>) {
        prefs.edit().putStringSet(KEY_EXCLUDED_HOME_APPS, values.toSet()).apply()
    }

    fun noteItems(): List<NeoListItem> = readItems(KEY_NOTE_ITEMS, KEY_NOTE_TEXT)

    fun setNoteItems(values: List<NeoListItem>) {
        writeItems(KEY_NOTE_ITEMS, values)
    }

    fun taskItems(): List<NeoListItem> = readItems(KEY_TASK_ITEMS, KEY_TASK_TEXT)

    fun setTaskItems(values: List<NeoListItem>) {
        writeItems(KEY_TASK_ITEMS, values)
    }

    fun motionSmoothness(): MotionSmoothness =
        runCatching {
            MotionSmoothness.valueOf(
                prefs.getString(KEY_MOTION_SMOOTHNESS, MotionSmoothness.BALANCED.name)
                    ?: MotionSmoothness.BALANCED.name
            )
        }.getOrDefault(MotionSmoothness.BALANCED)

    fun setMotionSmoothness(value: MotionSmoothness) {
        prefs.edit().putString(KEY_MOTION_SMOOTHNESS, value.name).apply()
    }

    fun reduceMotion(): Boolean = prefs.getBoolean(KEY_REDUCE_MOTION, false)

    fun setReduceMotion(value: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCE_MOTION, value).apply()
    }

    fun settingsSwipeEnabled(): Boolean = prefs.getBoolean(KEY_SETTINGS_SWIPE, true)
    fun setSettingsSwipeEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_SETTINGS_SWIPE, value).apply()
    }

    fun swipeDownNotifications(): Boolean =
        prefs.getBoolean(KEY_SWIPE_DOWN_NOTIFICATIONS, true)

    fun setSwipeDownNotifications(value: Boolean) {
        prefs.edit().putBoolean(KEY_SWIPE_DOWN_NOTIFICATIONS, value).apply()
    }

    fun homeClockDoubleTapEnabled(): Boolean = prefs.getBoolean(KEY_HOME_CLOCK_DOUBLE_TAP, true)
    fun setHomeClockDoubleTapEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_HOME_CLOCK_DOUBLE_TAP, value).apply()
    }

    fun homeClockLongPressEnabled(): Boolean = prefs.getBoolean(KEY_HOME_CLOCK_LONG_PRESS, true)
    fun setHomeClockLongPressEnabled(value: Boolean) {
        prefs.edit().putBoolean(KEY_HOME_CLOCK_LONG_PRESS, value).apply()
    }

    fun exportBackupJson(): String = LauncherBackupManager(this).exportBackupJson()

    fun importBackupJson(raw: String): Boolean = LauncherBackupManager(this).importBackupJson(raw)

    fun resetCustomizations() {
        prefs.edit().apply {
            remove(KEY_THEME)
            remove(KEY_DESIGN_PRESET)
            remove("show_date")
            remove("show_tagline")
            remove("show_app_count")
            remove(KEY_24_HOUR)
            remove(KEY_SHOW_AM_PM)
            remove(KEY_HOME_APP_COUNT)
            remove(KEY_WEATHER)
            remove(KEY_CHAT_NOTIFICATION_PACKAGES)
            remove(KEY_FAVORITES)
            remove(KEY_TILE_POSITIONS)
            remove(KEY_TILE_SIZES)
            remove(KEY_APP_TILE_CONTENT_MODE)
            remove(KEY_CLOCK_STYLE)
            remove(KEY_WALLPAPER_URI)
            remove(KEY_NOTE_TEXT)
            remove(KEY_NOTE_ITEMS)
            remove(KEY_TASK_ITEMS)
            remove(KEY_HOME_APP_ORDER)
            remove(KEY_EXCLUDED_HOME_APPS)
            remove(KEY_ICON_PACK_PACKAGE)
            remove(KEY_MOTION_SMOOTHNESS)
            remove(KEY_REDUCE_MOTION)
            remove(KEY_SETTINGS_SWIPE)
            remove(KEY_SWIPE_DOWN_NOTIFICATIONS)
            remove(KEY_HOME_CLOCK_DOUBLE_TAP)
            remove(KEY_HOME_CLOCK_LONG_PRESS)
            remove(KEY_HOME_APPS_INITIALIZED)
            apply()
        }
    }

    private fun readItems(key: String, legacyKey: String): List<NeoListItem> {
        val raw = prefs.getString(key, null)

        if (!raw.isNullOrBlank()) {
            return runCatching {
                val array = JSONArray(raw)
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val text = item.optString("text").trim()
                        if (text.isNotEmpty()) {
                            add(
                                NeoListItem(
                                    text = text,
                                    checked = item.optBoolean("checked", false)
                                )
                            )
                        }
                    }
                }
            }.getOrDefault(emptyList())
        }

        val legacy = prefs.getString(legacyKey, "")?.trim().orEmpty()
        return if (legacy.isBlank()) emptyList() else listOf(NeoListItem(legacy))
    }

    private fun writeItems(key: String, values: List<NeoListItem>) {
        val array = JSONArray()
        values.filter { it.text.isNotBlank() }.forEach { item ->
            array.put(
                org.json.JSONObject()
                    .put("text", item.text)
                    .put("checked", item.checked)
            )
        }
        prefs.edit().putString(key, array.toString()).apply()
    }

    companion object {
        internal const val KEY_THEME = "theme"
        internal const val KEY_DESIGN_PRESET = "design_preset"
        internal const val KEY_24_HOUR = "use_24_hour"
        internal const val KEY_SHOW_AM_PM = "show_am_pm"
        internal const val KEY_HOME_APP_COUNT = "home_app_count"
        internal const val KEY_WEATHER = "show_weather"
        internal const val KEY_CHAT_NOTIFICATION_PACKAGES = "chat_notification_packages"
        internal const val KEY_FAVORITES = "favorites"
        internal const val KEY_TILE_POSITIONS = "tile_positions"
        internal const val KEY_TILE_SIZES = "tile_sizes"
        internal const val KEY_APP_TILE_CONTENT_MODE = "app_tile_content_mode"
        internal const val KEY_CLOCK_STYLE = "clock_style"
        internal const val KEY_WALLPAPER_URI = "wallpaper_uri"
        internal const val KEY_NOTE_TEXT = "note_text"
        internal const val KEY_NOTE_ITEMS = "note_items"
        internal const val KEY_TASK_ITEMS = "task_items"
        internal const val KEY_HOME_APP_ORDER = "home_app_order"
        internal const val KEY_EXCLUDED_HOME_APPS = "excluded_home_apps"
        internal const val KEY_TASK_TEXT = "task_text"
        internal const val KEY_ICON_PACK_PACKAGE = "icon_pack_package"
        internal const val KEY_MOTION_SMOOTHNESS = "motion_smoothness"
        internal const val KEY_REDUCE_MOTION = "reduce_motion"
        internal const val KEY_SETTINGS_SWIPE = "settings_swipe_enabled"
        internal const val KEY_SWIPE_DOWN_NOTIFICATIONS = "swipe_down_notifications_enabled"
        internal const val KEY_HOME_CLOCK_DOUBLE_TAP = "home_clock_double_tap_enabled"
        internal const val KEY_HOME_CLOCK_LONG_PRESS = "home_clock_long_press_enabled"
        internal const val KEY_HOME_APPS_INITIALIZED = "home_apps_initialized"
        internal const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        internal const val KEY_LAST_MUSIC_PACKAGE = "last_music_package"
        internal const val MAX_BACKUP_ITEMS = 500
    }
}
