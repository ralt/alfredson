package io.ralt.alfredson.domain

import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class Slot(val weekIndex: Int, val dayIndex: Int) {
    val absoluteDayIndex: Int get() = weekIndex * 7 + dayIndex
}

enum class ProtocolState {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
}

data class ProtocolStatus(
    val state: ProtocolState,
    val slot: Slot?,
)

fun protocolStatus(
    startDate: LocalDate,
    today: LocalDate,
    totalDays: Int,
): ProtocolStatus {
    val daysSince = ChronoUnit.DAYS.between(startDate, today).toInt()
    return when {
        daysSince < 0 -> ProtocolStatus(ProtocolState.NOT_STARTED, null)
        daysSince >= totalDays -> ProtocolStatus(ProtocolState.COMPLETED, null)
        else -> ProtocolStatus(
            ProtocolState.IN_PROGRESS,
            Slot(daysSince / 7, daysSince % 7),
        )
    }
}

fun dateForSlot(startDate: LocalDate, absoluteDayIndex: Int): LocalDate =
    startDate.plusDays(absoluteDayIndex.toLong())
