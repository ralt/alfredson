package io.ralt.alfredson.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ralt.alfredson.R
import io.ralt.alfredson.data.AppLanguage
import io.ralt.alfredson.data.HeelDropVariant
import io.ralt.alfredson.data.UserPrefs
import io.ralt.alfredson.i18n.LocaleManager
import io.ralt.alfredson.notifications.ReminderScheduler
import io.ralt.alfredson.ui.rememberApp
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs by app.userPrefs.flow.collectAsStateWithLifecycle(initialValue = UserPrefs())

    var weightText by remember(prefs.bodyWeightKg) {
        mutableStateOf(if (prefs.bodyWeightKg == prefs.bodyWeightKg.toLong().toDouble())
            prefs.bodyWeightKg.toLong().toString()
        else
            prefs.bodyWeightKg.toString())
    }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showMorningPicker by remember { mutableStateOf(false) }
    var showEveningPicker by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Section(stringResource(R.string.body_weight_kg))
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("kg") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = {
                val kg = weightText.toDoubleOrNull()
                if (kg != null && kg > 0) {
                    scope.launch { app.userPrefs.setBodyWeight(kg) }
                }
            }) { Text(stringResource(R.string.save)) }

            HorizontalDivider()

            Section(stringResource(R.string.start_date))
            TextButton(onClick = { showStartDatePicker = true }) {
                Text(prefs.startDate?.toString() ?: stringResource(R.string.not_set))
            }
            Text(stringResource(R.string.start_date_warning), style = MaterialTheme.typography.bodySmall)

            HorizontalDivider()

            Section(stringResource(R.string.language))
            for (lang in listOf(AppLanguage.SYSTEM, AppLanguage.FRENCH, AppLanguage.ENGLISH)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = prefs.language == lang,
                        onClick = {
                            scope.launch {
                                app.userPrefs.setLanguage(lang)
                                LocaleManager.apply(lang)
                            }
                        },
                    )
                    Text(
                        when (lang) {
                            AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                            AppLanguage.FRENCH -> stringResource(R.string.language_french)
                            AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                        },
                    )
                }
            }

            HorizontalDivider()

            Section(stringResource(R.string.heel_drop_variant))
            for (variant in HeelDropVariant.entries) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = prefs.heelDropVariant == variant,
                        onClick = { scope.launch { app.userPrefs.setHeelDropVariant(variant) } },
                    )
                    Text(
                        when (variant) {
                            HeelDropVariant.BOTH -> stringResource(R.string.heel_drop_both)
                            HeelDropVariant.STRAIGHT_KNEE_ONLY -> stringResource(R.string.heel_drop_straight_only)
                        },
                    )
                }
            }

            HorizontalDivider()

            Section(stringResource(R.string.reminders))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.reminders_enable), modifier = Modifier.weight(1f))
                Switch(
                    checked = prefs.remindersEnabled,
                    onCheckedChange = {
                        scope.launch {
                            app.userPrefs.setRemindersEnabled(it)
                            ReminderScheduler.scheduleAll(context.applicationContext)
                        }
                    },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.morning), modifier = Modifier.weight(1f))
                TextButton(onClick = { showMorningPicker = true }) { Text(prefs.morningReminder.toString()) }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.evening), modifier = Modifier.weight(1f))
                TextButton(onClick = { showEveningPicker = true }) { Text(prefs.eveningReminder.toString()) }
            }

            HorizontalDivider()

            OutlinedButton(
                onClick = { showResetDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            ) { Text(stringResource(R.string.reset_progress)) }
        }
    }

    if (showStartDatePicker) {
        val initialMillis = prefs.startDate
            ?.atStartOfDay(ZoneId.systemDefault())
            ?.toInstant()
            ?.toEpochMilli()
            ?: System.currentTimeMillis()
        val state = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { m ->
                        val newDate = Instant.ofEpochMilli(m).atZone(ZoneId.systemDefault()).toLocalDate()
                        scope.launch { app.userPrefs.setStartDate(newDate) }
                    }
                    showStartDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            },
        ) { DatePicker(state = state) }
    }

    if (showMorningPicker) {
        TimePickerDialog(
            initial = prefs.morningReminder,
            onConfirm = { time ->
                scope.launch {
                    app.userPrefs.setMorningReminder(time)
                    ReminderScheduler.scheduleAll(context.applicationContext)
                }
                showMorningPicker = false
            },
            onDismiss = { showMorningPicker = false },
        )
    }

    if (showEveningPicker) {
        TimePickerDialog(
            initial = prefs.eveningReminder,
            onConfirm = { time ->
                scope.launch {
                    app.userPrefs.setEveningReminder(time)
                    ReminderScheduler.scheduleAll(context.applicationContext)
                }
                showEveningPicker = false
            },
            onDismiss = { showEveningPicker = false },
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_progress)) },
            text = { Text(stringResource(R.string.reset_progress_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        app.sessionLog.reset()
                        app.userPrefs.resetOnboarding()
                    }
                    showResetDialog = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    initial: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        text = { TimePicker(state = state) },
    )
}

@Composable
private fun Section(title: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
}
