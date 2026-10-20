package com.inibir.notifhook

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TestNotifier {

	private const val CHANNEL_ID = "test"

	fun send(context: Context) {
		val manager = context.getSystemService(NotificationManager::class.java)
		manager.createNotificationChannel(
			NotificationChannel(CHANNEL_ID, "Prueba", NotificationManager.IMPORTANCE_DEFAULT)
		)

		val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
		val notification = Notification.Builder(context, CHANNEL_ID)
			.setSmallIcon(android.R.drawable.ic_dialog_info)
			.setContentTitle("NotifHook prueba")
			.setContentText("Notificacion de prueba $time")
			.setAutoCancel(true)
			.build()

		manager.notify((System.currentTimeMillis() % Int.MAX_VALUE).toInt(), notification)
	}
}
