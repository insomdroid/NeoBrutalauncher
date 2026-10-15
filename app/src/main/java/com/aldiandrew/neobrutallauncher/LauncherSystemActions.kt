package com.aldiandrew.neobrutallauncher



@android.annotation.SuppressLint("WrongConstant", "PrivateApi")
internal fun expandNotificationPanel(context: android.content.Context) {
    try {
        val statusBarService = context.getSystemService("statusbar")
            ?: throw IllegalStateException("Android status bar service is unavailable")
        Class.forName("android.app.StatusBarManager")
            .getMethod("expandNotificationsPanel")
            .invoke(statusBarService)
    } catch (error: Exception) {
        android.util.Log.w(
            "NeoBrutalLauncher",
            "Cannot expand notification shade on this device",
            error
        )
    }
}
