package io.ralt.alfredson.domain

import kotlinx.serialization.Serializable

@Serializable
enum class Support { BIPODAL, BIPODAL_OPPOSITE_BIAS, UNIPODAL }

@Serializable
enum class Speed { SLOW, MEDIUM, FAST }

@Serializable
enum class Plyometric { POGO_BILATERAL, SINGLE_LEG_HOP }

@Serializable
data class DayPlan(
    val support: Support,
    val speed: Speed,
    val extraLoadPctBodyWeight: Int = 0,
    val plyometric: Plyometric? = null,
)

@Serializable
data class WeekPlan(
    val week: Int,
    val days: List<DayPlan>,
)

@Serializable
data class Schedule(
    val weeks: List<WeekPlan>,
) {
    fun dayPlan(weekIndex: Int, dayIndex: Int): DayPlan? =
        weeks.getOrNull(weekIndex)?.days?.getOrNull(dayIndex)

    val totalDays: Int get() = weeks.sumOf { it.days.size }
}
