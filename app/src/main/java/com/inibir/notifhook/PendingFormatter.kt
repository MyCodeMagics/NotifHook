package com.inibir.notifhook

import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PendingFormatter {

	private val timeFormat = SimpleDateFormat("dd/MM HH:mm:ss", Locale.getDefault())

	fun format(row: NotificationStore.Row): String = runCatching {
		val json = JSONObject(row.payload)
		val time = timeFormat.format(Date(json.getLong("time")))
		"$time  ${json.getString("package")}\n${json.getString("title")}: ${json.getString("text")}"
	}.getOrDefault(row.payload)
}
