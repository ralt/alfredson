package io.ralt.alfredson.data

import kotlinx.serialization.Serializable

@Serializable
data class SessionEntry(
    val morning: Boolean = false,
    val evening: Boolean = false,
) {
    val isFullyDone: Boolean get() = morning && evening
    val isPartial: Boolean get() = (morning || evening) && !isFullyDone
}

@Serializable
data class SessionLog(
    val entries: Map<Int, SessionEntry> = emptyMap(),
) {
    fun entryFor(absoluteDayIndex: Int): SessionEntry =
        entries[absoluteDayIndex] ?: SessionEntry()

    fun withEntry(absoluteDayIndex: Int, entry: SessionEntry): SessionLog =
        copy(entries = entries + (absoluteDayIndex to entry))
}
