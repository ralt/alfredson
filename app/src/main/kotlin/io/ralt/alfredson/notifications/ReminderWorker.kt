package io.ralt.alfredson.notifications

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.ralt.alfredson.AlfredsonApp
import io.ralt.alfredson.MainActivity
import io.ralt.alfredson.R
import io.ralt.alfredson.domain.ProtocolState
import io.ralt.alfredson.domain.protocolStatus
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val app = applicationContext as AlfredsonApp
        val prefs = app.userPrefs.flow.first()
        val status = prefs.startDate?.let { sd ->
            protocolStatus(sd, LocalDate.now(), app.scheduleProvider.schedule().totalDays)
        }
        val period = inputData.getString(KEY_PERIOD) ?: PERIOD_MORNING

        if (prefs.remindersEnabled && status?.state == ProtocolState.IN_PROGRESS) {
            val entry = status.slot?.let { app.sessionLog.flow.first().entryFor(it.absoluteDayIndex) }
            val alreadyDone = when (period) {
                PERIOD_MORNING -> entry?.morning == true
                PERIOD_EVENING -> entry?.evening == true
                else -> false
            }
            if (!alreadyDone) {
                postNotification(applicationContext, period)
            }
        }

        // Always re-enqueue tomorrow's reminder for this period so the chain continues.
        ReminderScheduler.scheduleNext(applicationContext, period)
        return Result.success()
    }

    private fun postNotification(ctx: Context, period: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(ctx, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val title = ctx.getString(
            if (period == PERIOD_MORNING) R.string.notification_morning_title else R.string.notification_evening_title,
        )
        val body = ctx.getString(R.string.notification_body)
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pending = PendingIntent.getActivity(
            ctx,
            if (period == PERIOD_MORNING) 1 else 2,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
        val notification = NotificationCompat.Builder(ctx, AlfredsonApp.CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pending)
            .build()
        val nm = ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(if (period == PERIOD_MORNING) NOTIF_ID_MORNING else NOTIF_ID_EVENING, notification)
    }

    companion object {
        const val KEY_PERIOD = "period"
        const val PERIOD_MORNING = "morning"
        const val PERIOD_EVENING = "evening"
        const val NOTIF_ID_MORNING = 101
        const val NOTIF_ID_EVENING = 102
    }
}
