package com.autobook.app.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.autobook.app.data.local.AppDatabase
import com.autobook.app.util.NotificationHelper
import com.autobook.app.util.ReminderStatus
import com.autobook.app.util.computeReminderStatus

/**
 * Daily background check: walks all active service reminders and posts a
 * notification when any are due soon or overdue.
 */
class ReminderCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = AppDatabase.getInstance(applicationContext)
        val reminders = db.serviceReminderDao().getAllActiveReminders()

        for (reminder in reminders) {
            val vehicle = db.vehicleDao().getVehicleById(reminder.vehicleId) ?: continue
            val status = computeReminderStatus(
                currentOdometer = vehicle.currentOdometer,
                nextKm = reminder.nextKm,
                nextDate = reminder.nextDate
            )

            val message = when (status) {
                ReminderStatus.DUE_SOON ->
                    applicationContext.getString(
                        com.autobook.app.R.string.notif_due_soon, vehicle.nickname
                    )
                ReminderStatus.OVERDUE ->
                    applicationContext.getString(
                        com.autobook.app.R.string.notif_overdue, vehicle.nickname
                    )
                ReminderStatus.ON_TRACK -> null
            }

            if (message != null) {
                NotificationHelper.showReminderNotification(
                    context = applicationContext,
                    notificationId = reminder.id,
                    vehicleNickname = vehicle.nickname,
                    message = message
                )
            }
        }

        return Result.success()
    }
}
