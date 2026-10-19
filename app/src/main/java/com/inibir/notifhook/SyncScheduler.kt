package com.inibir.notifhook

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context

object SyncScheduler {

	private const val JOB_ID = 1
	private const val BACKOFF_MS = 30_000L

	fun schedule(context: Context) {
		val scheduler = context.getSystemService(JobScheduler::class.java)
		if (scheduler.getPendingJob(JOB_ID) != null) return

		val job = JobInfo.Builder(JOB_ID, ComponentName(context, SyncJobService::class.java))
			.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
			.setBackoffCriteria(BACKOFF_MS, JobInfo.BACKOFF_POLICY_EXPONENTIAL)
			.build()
		scheduler.schedule(job)
	}
}
