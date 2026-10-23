package com.inibir.notifhook

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifListener : NotificationListenerService() {

	override fun onListenerConnected() {
		isConnected = true
		val registry = AppRegistry(applicationContext)
		activeNotifications?.forEach { registry.register(it.packageName) }
		Outbox.sync(applicationContext)
	}

	override fun onListenerDisconnected() {
		isConnected = false
		ServiceRebinder.rebind(applicationContext)
	}

	override fun onNotificationPosted(sbn: StatusBarNotification) {
		val registry = AppRegistry(applicationContext)
		registry.register(sbn.packageName)

		if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return
		if (HookConfig(applicationContext).url.isEmpty()) return

		val isOwn = sbn.packageName == packageName
		if (!isOwn && !registry.isEnabled(sbn.packageName)) return

		Outbox.enqueue(applicationContext, NotificationPayload.from(sbn))
	}

	companion object {
		@Volatile
		var isConnected: Boolean = false
			private set
	}
}
