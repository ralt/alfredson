package io.ralt.alfredson

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import io.ralt.alfredson.data.ScheduleProvider
import io.ralt.alfredson.data.SessionLogRepository
import io.ralt.alfredson.data.UserPrefsRepository

class AlfredsonApp : Application() {

    val userPrefs: UserPrefsRepository by lazy { UserPrefsRepository(this) }
    val sessionLog: SessionLogRepository by lazy { SessionLogRepository(this) }
    val scheduleProvider: ScheduleProvider by lazy { ScheduleProvider(this) }

    override fun onCreate() {
        super.onCreate()
        createReminderChannel()
    }

    private fun createReminderChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_REMINDERS,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = getString(R.string.notification_channel_description)
            }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_REMINDERS = "alfredson_reminders"
    }
}
