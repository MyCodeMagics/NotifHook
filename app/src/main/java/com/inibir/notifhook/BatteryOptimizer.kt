package com.inibir.notifhook

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings

object BatteryOptimizer {

	fun isIgnoring(context: Context): Boolean {
		val powerManager = context.getSystemService(PowerManager::class.java) ?: return true
		return powerManager.isIgnoringBatteryOptimizations(context.packageName)
	}

	fun requestIgnore(context: Context) {
		val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
			data = Uri.parse("package:${context.packageName}")
			addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
		}
		runCatching { context.startActivity(intent) }
			.onFailure {
				val fallback = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
					addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
				}
				runCatching { context.startActivity(fallback) }
			}
	}
}
