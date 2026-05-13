package io.ralt.alfredson.data

import android.content.Context
import io.ralt.alfredson.domain.Schedule
import kotlinx.serialization.json.Json

class ScheduleProvider(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true }

    private var cached: Schedule? = null

    fun schedule(): Schedule {
        cached?.let { return it }
        val text = context.assets.open("schedule.json").bufferedReader().use { it.readText() }
        val parsed = json.decodeFromString(Schedule.serializer(), text)
        cached = parsed
        return parsed
    }
}
