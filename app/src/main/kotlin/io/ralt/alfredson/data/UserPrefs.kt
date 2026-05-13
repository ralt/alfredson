package io.ralt.alfredson.data

import java.time.LocalDate
import java.time.LocalTime

enum class HeelDropVariant { BOTH, STRAIGHT_KNEE_ONLY }

enum class AppLanguage(val tag: String) {
    SYSTEM(""),
    FRENCH("fr"),
    ENGLISH("en");

    companion object {
        fun fromTag(tag: String?): AppLanguage = when (tag) {
            "fr" -> FRENCH
            "en" -> ENGLISH
            else -> SYSTEM
        }
    }
}

data class UserPrefs(
    val onboarded: Boolean = false,
    val startDate: LocalDate? = null,
    val bodyWeightKg: Double = 75.0,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val heelDropVariant: HeelDropVariant = HeelDropVariant.BOTH,
    val morningReminder: LocalTime = LocalTime.of(8, 0),
    val eveningReminder: LocalTime = LocalTime.of(20, 0),
    val remindersEnabled: Boolean = true,
)
