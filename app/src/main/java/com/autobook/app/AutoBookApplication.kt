package com.autobook.app

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.autobook.app.util.NotificationHelper
import com.autobook.app.worker.ReminderCheckWorker
import java.util.concurrent.TimeUnit

class AutoBookApplication : Application() {

    /** App-wide DI container, available to ViewModelFactories via the application context. */
    val container: AppContainer by lazy { AppContainer(this) }

    override fun onCreate() {
        super.onCreate()
        // Create the notification channel up front (see Step 12).
        NotificationHelper.createChannel(this)
        scheduleReminderCheck()
    }

    /** Schedules the once-per-day reminder check (idempotent via KEEP policy). */
    private fun scheduleReminderCheck() {
        val request = PeriodicWorkRequestBuilder<ReminderCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "reminder_check",
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
