package com.inibir.notifhook

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object XiaomiHelper {

	fun isXiaomi(): Boolean {
		val manufacturer = Build.MANUFACTURER.lowercase()
		val brand = Build.BRAND.lowercase()
		return manufacturer.contains("xiaomi") || manufacturer.contains("redmi") || manufacturer.contains("poco") ||
			brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("poco")
	}

	fun openAutoStart(context: Context) {
		val candidates = listOf(
			Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
			Intent("miui.intent.action.OP_AUTO_START").addCategory(Intent.CATEGORY_DEFAULT),
			Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity"))
				.putExtra("extra_pkgname", context.packageName),
			createAppDetailsIntent(context)
		)
		startFirstAvailable(context, candidates)
	}

	fun openBatterySaver(context: Context) {
		val candidates = listOf(
			Intent().setComponent(ComponentName("com.miui.powerkeeper", "com.miui.powerkeeper.ui.HiddenAppsConfigActivity"))
				.putExtra("package_name", context.packageName)
				.putExtra("package_label", context.applicationInfo.loadLabel(context.packageManager).toString()),
			Intent("miui.intent.action.POWER_HIDE_MODE_APP")
				.putExtra("package_name", context.packageName),
			createAppDetailsIntent(context)
		)
		startFirstAvailable(context, candidates)
	}

	private fun createAppDetailsIntent(context: Context): Intent {
		return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
			data = Uri.parse("package:${context.packageName}")
		}
	}

	private fun startFirstAvailable(context: Context, intents: List<Intent>) {
		for (intent in intents) {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
			val canResolve = intent.resolveActivity(context.packageManager) != null
			if (canResolve) {
				val success = runCatching { context.startActivity(intent) }.isSuccess
				if (success) return
			}
		}
		runCatching { context.startActivity(createAppDetailsIntent(context).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
	}
}
