package com.inibir.notifhook

import android.app.Notification
import android.service.notification.StatusBarNotification
import org.json.JSONObject

object NotificationPayload {

	fun from(sbn: StatusBarNotification): JSONObject {
		val extras = sbn.notification.extras
		return JSONObject()
			.put("package", sbn.packageName)
			.put("title", extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty())
			.put("text", extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty())
			.put("time", sbn.postTime)
	}
}
