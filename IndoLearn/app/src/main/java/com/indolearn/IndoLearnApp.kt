package com.indolearn

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.indolearn.utils.DailyReminderWorker
import com.indolearn.utils.NotificationHelper
import dagger.hilt.android.HiltAndroidApp
import java.util.concurrent.TimeUnit
import java.util.Calendar

@HiltAndroidApp
class IndoLearnApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)
        scheduleDailyReminder()
    }

    private fun scheduleDailyReminder() {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()
        
        // Set Execution around 8:00 PM
        dueDate.set(Calendar.HOUR_OF_DAY, 20)
        dueDate.set(Calendar.MINUTE, 0)
        dueDate.set(Calendar.SECOND, 0)
        
        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }
        
        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis
        
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyReminderWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .build()
            
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "DailyReminderWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }
}