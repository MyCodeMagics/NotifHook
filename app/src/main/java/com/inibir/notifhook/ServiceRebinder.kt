package com.inibir.notifhook

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService

object ServiceRebinder {

	fun isAccessGranted(context: Context): Boolean {
		val enabledListeners = Settings.Secure.getString(
			context.contentResolver,
			"enabled_notification_listeners"
		) ?: return false
		val target = ComponentName(context, NotifListener::class.java).flattenToString()
		return enabledListeners.split(":").any {
			it == target || ComponentName.unflattenFromString(it)?.packageName == context.packageName
		}
	}

	fun rebind(context: Context) {
		val component = ComponentName(context, NotifListener::class.java)
		val pm = context.packageManager
		pm.setComponentEnabledSetting(
			component,
			PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
			PackageManager.DONT_KILL_APP
		)
		pm.setComponentEnabledSetting(
			component,
			PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
			PackageManager.DONT_KILL_APP
		)
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			runCatching { NotificationListenerService.requestRebind(component) }
		}
	}
}
