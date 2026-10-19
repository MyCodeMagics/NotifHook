package com.inibir.notifhook

import java.net.HttpURLConnection
import java.net.URL

object HttpSender {

	private const val TIMEOUT_MS = 5000

	fun post(url: String, body: String): Boolean = runCatching {
		val connection = URL(url).openConnection() as HttpURLConnection
		try {
			connection.requestMethod = "POST"
			connection.connectTimeout = TIMEOUT_MS
			connection.readTimeout = TIMEOUT_MS
			connection.doOutput = true
			connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
			connection.outputStream.use { it.write(body.toByteArray()) }
			connection.responseCode in 200..299
		} finally {
			connection.disconnect()
		}
	}.getOrDefault(false)
}
