package com.inibir.notifhook

import android.app.job.JobParameters
import android.app.job.JobService

class SyncJobService : JobService() {

	override fun onStartJob(params: JobParameters): Boolean {
		Outbox.flushAsync(applicationContext) { complete ->
			jobFinished(params, !complete)
		}
		return true
	}

	override fun onStopJob(params: JobParameters): Boolean = true
}
