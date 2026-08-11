package com.indolearn.utils

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class DailyReminderWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        NotificationHelper.createChannel(applicationContext)
        NotificationHelper.showDailyReminder(applicationContext)
        return Result.success()
    }
}
