package io.ralt.alfredson.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class SessionLogRepository(context: Context) {

    private val file: File = File(context.filesDir, "sessions.json")
    private val json = Json { ignoreUnknownKeys = true }
    private val mutex = Mutex()

    private val state = MutableStateFlow(loadInitial())

    val flow: Flow<SessionLog> = state.asStateFlow()

    private fun loadInitial(): SessionLog {
        if (!file.exists()) return SessionLog()
        return runCatching {
            json.decodeFromString(SessionLog.serializer(), file.readText())
        }.getOrDefault(SessionLog())
    }

    suspend fun setMorning(absoluteDayIndex: Int, value: Boolean) {
        mutate { it.withEntry(absoluteDayIndex, it.entryFor(absoluteDayIndex).copy(morning = value)) }
    }

    suspend fun setEvening(absoluteDayIndex: Int, value: Boolean) {
        mutate { it.withEntry(absoluteDayIndex, it.entryFor(absoluteDayIndex).copy(evening = value)) }
    }

    suspend fun reset() {
        mutate { SessionLog() }
    }

    private suspend fun mutate(transform: (SessionLog) -> SessionLog) {
        mutex.withLock {
            val next = transform(state.value)
            state.value = next
            withContext(Dispatchers.IO) {
                file.writeText(json.encodeToString(SessionLog.serializer(), next))
            }
        }
    }
}
