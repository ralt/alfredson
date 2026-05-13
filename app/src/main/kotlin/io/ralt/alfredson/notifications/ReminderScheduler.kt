package io.ralt.alfredson.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import io.ralt.alfredson.AlfredsonApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    private const val WORK_MORNING = "reminder-am"
    private const val WORK_EVENING = "reminder-pm"

    fun scheduleAll(context: Context) {
        val app = context.applicationContext as AlfredsonApp
        val prefs = runBlocking { app.userPrefs.flow.first() }
        val wm = WorkManager.getInstance(context.applicationContext)
        if (!prefs.remindersEnabled) {
            wm.cancelUniqueWork(WORK_MORNING)
            wm.cancelUniqueWork(WORK_EVENING)
            return
        }
        scheduleAt(context.applicationContext, ReminderWorker.PERIOD_MORNING, prefs.morningReminder)
        scheduleAt(context.applicationContext, ReminderWorker.PERIOD_EVENING, prefs.eveningReminder)
    }

    fun scheduleNext(context: Context, period: String) {
        val app = context.applicationContext as AlfredsonApp
        val prefs = runBlocking { app.userPrefs.flow.first() }
        if (!prefs.remindersEnabled) return
        val time = if (period == ReminderWorker.PERIOD_MORNING) prefs.morningReminder else prefs.eveningReminder
        scheduleAt(context.applicationContext, period, time)
    }

    private fun scheduleAt(context: Context, period: String, time: LocalTime) {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(time)
        if (!target.isAfter(now)) target = target.plusDays(1)
        val delayMillis = Duration.between(now, target).toMillis().coerceAtLeast(60_000)

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf(ReminderWorker.KEY_PERIOD to period))
            .addTag(period)
            .build()

        val workName = if (period == ReminderWorker.PERIOD_MORNING) WORK_MORNING else WORK_EVENING
        WorkManager.getInstance(context).enqueueUniqueWork(workName, ExistingWorkPolicy.REPLACE, request)
    }
}
