package com.inibir.notifhook

import android.content.Context

object GuidePreferences {

	private const val PREFS_NAME = "notifhook_guide"
	private const val KEY_AUTO_START = "auto_start_done"
	private const val KEY_MIUI_BATTERY = "miui_battery_done"
	private const val KEY_LOCK_RECENTS = "lock_recents_done"

	fun isAutoStartDone(context: Context): Boolean {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getBoolean(KEY_AUTO_START, false)
	}

	fun setAutoStartDone(context: Context, done: Boolean) {
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(KEY_AUTO_START, done)
			.apply()
	}

	fun isMiuiBatteryDone(context: Context): Boolean {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getBoolean(KEY_MIUI_BATTERY, false)
	}

	fun setMiuiBatteryDone(context: Context, done: Boolean) {
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(KEY_MIUI_BATTERY, done)
			.apply()
	}

	fun isLockRecentsDone(context: Context): Boolean {
		return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.getBoolean(KEY_LOCK_RECENTS, false)
	}

	fun setLockRecentsDone(context: Context, done: Boolean) {
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
			.edit()
			.putBoolean(KEY_LOCK_RECENTS, done)
			.apply()
	}
}
