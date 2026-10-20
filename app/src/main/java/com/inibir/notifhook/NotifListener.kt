package com.inibir.notifhook

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotifListener : NotificationListenerService() {

	override fun onListenerConnected() {
		Outbox.sync(applicationContext)
	}

	override fun onNotificationPosted(sbn: StatusBarNotification) {
		if (sbn.notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

		val config = HookConfig(applicationContext)
		if (config.url.isEmpty()) return
		val isOwn = sbn.packageName == packageName
		if (!isOwn && !PackageFilter.accepts(config.packagesText, sbn.packageName)) return

		Outbox.enqueue(applicationContext, NotificationPayload.from(sbn))
	}
}
