package com.inibir.notifhook

import android.content.Context

class HookConfig(context: Context) {

	private val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)

	var url: String
		get() = prefs.getString(KEY_URL, "").orEmpty()
		set(value) = prefs.edit().putString(KEY_URL, value.trim()).apply()

	private companion object {
		const val FILE = "notifhook"
		const val KEY_URL = "url"
	}
}
