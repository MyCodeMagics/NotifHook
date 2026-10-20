package com.inibir.notifhook

import android.content.Context

object AppLabel {

	fun of(context: Context, packageName: String): String = runCatching {
		val manager = context.packageManager
		manager.getApplicationLabel(manager.getApplicationInfo(packageName, 0)).toString()
	}.getOrDefault(packageName)
}
