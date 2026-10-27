package com.inibir.notifhook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder

class KeepAliveForegroundService : Service() {

	override fun onCreate() {
		super.onCreate()
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
			startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
		} else {
			startForeground(NOTIF_ID, buildNotification())
		}
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return START_STICKY
	}

	override fun onBind(intent: Intent?): IBinder? = null

	private fun buildNotification(): Notification {
		val channelId = "notifhook_keepalive"
		val manager = getSystemService(NotificationManager::class.java)
		manager?.createNotificationChannel(
			NotificationChannel(channelId, "NotifHook activo", NotificationManager.IMPORTANCE_MIN)
		)
		return Notification.Builder(this, channelId)
			.setContentTitle("NotifHook activo")
			.setSmallIcon(R.drawable.ic_notification)
			.build()
	}

	companion object {
		const val NOTIF_ID = 1
	}
}
