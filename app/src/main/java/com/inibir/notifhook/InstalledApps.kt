package com.inibir.notifhook

import android.content.Context
import android.content.Intent

object InstalledApps {

	fun launchable(context: Context): List<String> {
		val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
		return context.packageManager.queryIntentActivities(intent, 0)
			.map { it.activityInfo.packageName }
			.distinct()
	}
}
