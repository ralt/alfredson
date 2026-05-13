package io.ralt.alfredson.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalTime

private val Context.dataStore by preferencesDataStore(name = "user_prefs")

private object Keys {
    val ONBOARDED = booleanPreferencesKey("onboarded")
    val START_DATE = stringPreferencesKey("start_date")
    val BODY_WEIGHT_KG = doublePreferencesKey("body_weight_kg")
    val LANGUAGE = stringPreferencesKey("language")
    val HEEL_DROP_VARIANT = stringPreferencesKey("heel_drop_variant")
    val MORNING_REMINDER = stringPreferencesKey("morning_reminder")
    val EVENING_REMINDER = stringPreferencesKey("evening_reminder")
    val REMINDERS_ENABLED = booleanPreferencesKey("reminders_enabled")
}

class UserPrefsRepository(private val context: Context) {

    val flow: Flow<UserPrefs> = context.dataStore.data.map { it.toUserPrefs() }

    suspend fun completeOnboarding(
        startDate: LocalDate,
        bodyWeightKg: Double,
        language: AppLanguage,
        heelDropVariant: HeelDropVariant,
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDED] = true
            prefs[Keys.START_DATE] = startDate.toString()
            prefs[Keys.BODY_WEIGHT_KG] = bodyWeightKg
            prefs[Keys.LANGUAGE] = language.tag
            prefs[Keys.HEEL_DROP_VARIANT] = heelDropVariant.name
        }
    }

    suspend fun setBodyWeight(kg: Double) {
        context.dataStore.edit { it[Keys.BODY_WEIGHT_KG] = kg }
    }

    suspend fun setStartDate(date: LocalDate) {
        context.dataStore.edit { it[Keys.START_DATE] = date.toString() }
    }

    suspend fun setLanguage(language: AppLanguage) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language.tag }
    }

    suspend fun setHeelDropVariant(variant: HeelDropVariant) {
        context.dataStore.edit { it[Keys.HEEL_DROP_VARIANT] = variant.name }
    }

    suspend fun setMorningReminder(time: LocalTime) {
        context.dataStore.edit { it[Keys.MORNING_REMINDER] = time.toString() }
    }

    suspend fun setEveningReminder(time: LocalTime) {
        context.dataStore.edit { it[Keys.EVENING_REMINDER] = time.toString() }
    }

    suspend fun setRemindersEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.REMINDERS_ENABLED] = enabled }
    }

    suspend fun resetOnboarding() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ONBOARDED)
            prefs.remove(Keys.START_DATE)
        }
    }
}

private fun Preferences.toUserPrefs(): UserPrefs = UserPrefs(
    onboarded = this[Keys.ONBOARDED] ?: false,
    startDate = this[Keys.START_DATE]?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    bodyWeightKg = this[Keys.BODY_WEIGHT_KG] ?: 75.0,
    language = AppLanguage.fromTag(this[Keys.LANGUAGE]),
    heelDropVariant = this[Keys.HEEL_DROP_VARIANT]
        ?.let { runCatching { HeelDropVariant.valueOf(it) }.getOrNull() }
        ?: HeelDropVariant.BOTH,
    morningReminder = this[Keys.MORNING_REMINDER]
        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        ?: LocalTime.of(8, 0),
    eveningReminder = this[Keys.EVENING_REMINDER]
        ?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
        ?: LocalTime.of(20, 0),
    remindersEnabled = this[Keys.REMINDERS_ENABLED] ?: true,
)
