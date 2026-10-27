package com.inibir.notifhook

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

	override fun onReceive(context: Context, intent: Intent?) {
		val action = intent?.action ?: return
		if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
			context.startForegroundService(Intent(context, KeepAliveForegroundService::class.java))
			ServiceRebinder.rebind(context)
			Outbox.sync(context)
		}
	}
}
