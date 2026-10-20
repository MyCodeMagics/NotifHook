package com.inibir.notifhook

import android.content.Context
import org.json.JSONObject
import java.util.concurrent.Executors

object Outbox {

	private const val BATCH_SIZE = 50
	private val executor = Executors.newSingleThreadExecutor()

	fun enqueue(context: Context, payload: JSONObject) {
		run(context) {
			NotificationStore.get(it).insert(payload.toString())
			flushOrSchedule(it)
		}
	}

	fun sync(context: Context) {
		run(context) { flushOrSchedule(it) }
	}

	fun flushAsync(context: Context, onDone: (Boolean) -> Unit) {
		run(context) { onDone(flush(it)) }
	}

	fun loadPending(context: Context, limit: Int, onResult: (Int, List<NotificationStore.Row>) -> Unit) {
		run(context) {
			val store = NotificationStore.get(it)
			onResult(store.count(), store.oldest(limit))
		}
	}

	private fun run(context: Context, block: (Context) -> Unit) {
		val appContext = context.applicationContext
		executor.execute { block(appContext) }
	}

	private fun flushOrSchedule(context: Context) {
		if (!flush(context)) SyncScheduler.schedule(context)
	}

	private fun flush(context: Context): Boolean {
		val url = HookConfig(context).url
		if (url.isEmpty()) return true

		val store = NotificationStore.get(context)
		while (true) {
			val batch = store.oldest(BATCH_SIZE)
			if (batch.isEmpty()) return true
			for (row in batch) {
				if (!HttpSender.post(url, row.payload)) return false
				store.delete(row.id)
			}
		}
	}
}
