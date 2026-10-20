package com.inibir.notifhook

import android.content.Context

class AppRegistry(context: Context) {

	private val prefs = context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

	fun register(packageName: String) = synchronized(LOCK) {
		val seen = read(KEY_SEEN)
		if (seen.add(packageName)) write(KEY_SEEN, seen)
	}

	fun seenPackages(): List<String> = synchronized(LOCK) { read(KEY_SEEN).toList() }

	fun isEnabled(packageName: String): Boolean = synchronized(LOCK) { packageName in read(KEY_ENABLED) }

	fun setEnabled(packageName: String, enabled: Boolean) = synchronized(LOCK) {
		val current = read(KEY_ENABLED)
		val changed = if (enabled) current.add(packageName) else current.remove(packageName)
		if (changed) write(KEY_ENABLED, current)
	}

	private fun read(key: String): MutableSet<String> =
		HashSet(prefs.getStringSet(key, emptySet()).orEmpty())

	private fun write(key: String, value: Set<String>) {
		prefs.edit().putStringSet(key, value).apply()
	}

	private companion object {
		const val FILE = "apps"
		const val KEY_SEEN = "seen"
		const val KEY_ENABLED = "enabled"
		val LOCK = Any()
	}
}
