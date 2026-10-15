package com.aldiandrew.neobrutallauncher

import org.json.JSONArray
import org.json.JSONObject

/**
 * Reads and restores launcher backups independently from everyday preference accessors.
 *
 * Import remains a single SharedPreferences commit so invalid backups cannot partially apply.
 */
internal class LauncherBackupManager(
    private val preferences: LauncherPreferences
) {
    private val prefs = preferences.prefs

    fun exportBackupJson(): String {
        val root = JSONObject()
            .put("schemaVersion", 1)
            .put("theme", preferences.theme().name)
            .put("designPreset", preferences.designPreset().name)
            .put("use24Hour", preferences.use24Hour())
            .put("showAmPm", preferences.showAmPm())
                        .put("homeAppCount", preferences.homeAppCount())
                                    .put("showWeather", preferences.showWeather())
            .put("chatNotificationPackages", JSONArray(preferences.chatNotificationPackages()))
            .put("favorites", JSONArray(preferences.favorites().toList()))
            .put("tilePositions", JSONObject().apply {
                preferences.tilePositions().forEach { (id, position) ->
                    put(id, JSONObject().put("column", position.column).put("row", position.row))
                }
            })
            .put("tileSizes", JSONObject().apply {
                preferences.tileSizes().forEach { (id, tileSize) -> put(id, tileSize.name) }
            })
            .put("appTileContentMode", preferences.appTileContentMode().name)
            .put("typographyStyle", preferences.typographyStyle().name)
            .put("wallpaperUri", preferences.wallpaperUri())
            .put("homeAppOrder", JSONArray(preferences.homeAppOrder()))
            .put("excludedHomeApps", JSONArray(preferences.excludedHomeApps().toList()))
            .put("noteItems", itemsToJson(preferences.noteItems()))
            .put("taskItems", itemsToJson(preferences.taskItems()))
            .put("iconPackPackage", preferences.iconPackPackage())
            .put("motionSmoothness", preferences.motionSmoothness().name)
            .put("reduceMotion", preferences.reduceMotion())
        return root.toString()
    }

    fun importBackupJson(raw: String): Boolean {
        if (raw.length > 512 * 1024) return false
        return runCatching {
            val root = JSONObject(raw)
            require(root.optInt("schemaVersion", -1) == 1)
            val theme = runCatching { ThemePreference.valueOf(root.optString("theme")) }.getOrDefault(ThemePreference.SYSTEM)
            val designPreset = runCatching {
                DesignPreset.valueOf(root.optString("designPreset", DesignPreset.NEO_BRUTAL_CLASSIC.name))
            }.getOrDefault(DesignPreset.NEO_BRUTAL_CLASSIC)
            val contentMode = runCatching { TileContentMode.valueOf(root.optString("appTileContentMode")) }.getOrDefault(TileContentMode.ICON_TEXT)
            val typography = when (root.optString("typographyStyle")) {
                TypographyStyle.CONDENSED.name -> TypographyStyle.CONDENSED
                else -> TypographyStyle.DEFAULT
            }
            val count = LauncherPreferenceRules.normalizePinnedCount(root.optInt("homeAppCount", 5))
            val motionSmoothness = runCatching {
                MotionSmoothness.valueOf(
                    root.optString("motionSmoothness", MotionSmoothness.BALANCED.name)
                )
            }.getOrDefault(MotionSmoothness.BALANCED)
            val reduceMotion = root.optBoolean("reduceMotion", false)

            fun safeArray(name: String, max: Int): JSONArray {
                val array = root.optJSONArray(name) ?: JSONArray()
                require(array.length() <= max)
                return array
            }

            val favoritesArray = safeArray("favorites", LauncherPreferences.MAX_BACKUP_ITEMS)
            val restoredFavorites = buildSet {
                for (i in 0 until favoritesArray.length()) {
                    val value = favoritesArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }
            val chatArray = safeArray("chatNotificationPackages", 1)
            val restoredChat = buildList {
                for (i in 0 until chatArray.length()) {
                    val value = chatArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }.distinct().take(1)
            val orderArray = safeArray("homeAppOrder", LauncherPreferences.MAX_BACKUP_ITEMS)
            val restoredOrder = buildList {
                for (i in 0 until orderArray.length()) {
                    val value = orderArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }.distinct()
            val excludedArray = safeArray("excludedHomeApps", LauncherPreferences.MAX_BACKUP_ITEMS)
            val restoredExcluded = buildSet {
                for (i in 0 until excludedArray.length()) {
                    val value = excludedArray.optString(i).trim()
                    if (value.length in 1..256) add(value)
                }
            }

            val positionsObject = root.optJSONObject("tilePositions") ?: JSONObject()
            val restoredPositions = mutableSetOf<String>()
            val positionKeys = positionsObject.keys().asSequence().toList()
            require(positionKeys.size <= LauncherPreferences.MAX_BACKUP_ITEMS)
            positionKeys.forEach { id ->
                val item = positionsObject.optJSONObject(id) ?: return@forEach
                val column = item.optInt("column", -1)
                val row = item.optInt("row", -1)
                if (id.length in 1..128 && column in 0..99 && row in 0..999) {
                    restoredPositions.add(id + "|" + column + "|" + row)
                }
            }

            val sizesObject = root.optJSONObject("tileSizes") ?: JSONObject()
            val restoredSizes = mutableSetOf<String>()
            val sizeKeys = sizesObject.keys().asSequence().toList()
            require(sizeKeys.size <= LauncherPreferences.MAX_BACKUP_ITEMS)
            sizeKeys.forEach { id ->
                val tileSize = runCatching { NeoTileSize.valueOf(sizesObject.optString(id)) }.getOrNull()
                if (id.length in 1..128 && tileSize != null) restoredSizes.add(id + "|" + tileSize.name)
            }

            val wallpaper = root.optString("wallpaperUri", "").takeIf { it.startsWith("content://") && it.length <= 2048 }
            val iconPack = root.optString("iconPackPackage", "").takeIf {
                it.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))
            }

            val editor = prefs.edit()
                .putString(LauncherPreferences.KEY_THEME, theme.name)
                .putString(LauncherPreferences.KEY_DESIGN_PRESET, designPreset.name)
                .putBoolean(LauncherPreferences.KEY_24_HOUR, root.optBoolean("use24Hour", true))
                .putBoolean(LauncherPreferences.KEY_SHOW_AM_PM, root.optBoolean("showAmPm", true))
                                .putInt(LauncherPreferences.KEY_HOME_APP_COUNT, count)
                                                .putBoolean(LauncherPreferences.KEY_WEATHER, root.optBoolean("showWeather", false))
                .putStringSet(LauncherPreferences.KEY_CHAT_NOTIFICATION_PACKAGES, restoredChat.toSet())
                .putStringSet(LauncherPreferences.KEY_FAVORITES, restoredFavorites)
                .putStringSet(LauncherPreferences.KEY_TILE_POSITIONS, restoredPositions)
                .putStringSet(LauncherPreferences.KEY_TILE_SIZES, restoredSizes)
                .putString(LauncherPreferences.KEY_APP_TILE_CONTENT_MODE, contentMode.name)
                .putString(LauncherPreferences.KEY_CLOCK_STYLE, typography.name)
                .putString(LauncherPreferences.KEY_MOTION_SMOOTHNESS, motionSmoothness.name)
                .putBoolean(LauncherPreferences.KEY_REDUCE_MOTION, reduceMotion)
                .putString(LauncherPreferences.KEY_HOME_APP_ORDER, JSONArray(restoredOrder).toString())
                .putStringSet(LauncherPreferences.KEY_EXCLUDED_HOME_APPS, restoredExcluded)
            if (wallpaper == null) editor.remove(LauncherPreferences.KEY_WALLPAPER_URI) else editor.putString(LauncherPreferences.KEY_WALLPAPER_URI, wallpaper)
            if (iconPack == null) editor.remove(LauncherPreferences.KEY_ICON_PACK_PACKAGE) else editor.putString(LauncherPreferences.KEY_ICON_PACK_PACKAGE, iconPack)
            editor.putString(LauncherPreferences.KEY_NOTE_ITEMS, itemsToJson(jsonToItems(root.optJSONArray("noteItems"))).toString())
            editor.putString(LauncherPreferences.KEY_TASK_ITEMS, itemsToJson(jsonToItems(root.optJSONArray("taskItems"))).toString())
            editor.putBoolean(LauncherPreferences.KEY_HOME_APPS_INITIALIZED, true)
            editor.commit()
        }.getOrDefault(false)
    }

    private fun itemsToJson(items: List<NeoListItem>): JSONArray =
        JSONArray().apply {
            items.take(LauncherPreferences.MAX_BACKUP_ITEMS).forEach {
                put(JSONObject().put("text", it.text.take(1000)).put("checked", it.checked))
            }
        }

    private fun jsonToItems(array: JSONArray?): List<NeoListItem> {
        if (array == null || array.length() > LauncherPreferences.MAX_BACKUP_ITEMS) return emptyList()
        return buildList {
            for (i in 0 until array.length()) {
                val item = array.optJSONObject(i) ?: continue
                val value = item.optString("text").trim()
                if (value.isNotEmpty() && value.length <= 1000) {
                    add(NeoListItem(value, item.optBoolean("checked", false)))
                }
            }
        }
    }
}
