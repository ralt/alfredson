package io.ralt.alfredson.ui.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.ralt.alfredson.R
import io.ralt.alfredson.data.AppLanguage
import io.ralt.alfredson.data.HeelDropVariant
import io.ralt.alfredson.i18n.LocaleManager
import io.ralt.alfredson.notifications.ReminderScheduler
import io.ralt.alfredson.ui.rememberApp
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onDone: () -> Unit) {
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var weightText by remember { mutableStateOf("75") }
    var language by remember { mutableStateOf(AppLanguage.SYSTEM) }
    var heelVariant by remember { mutableStateOf(HeelDropVariant.BOTH) }
    var showDatePicker by remember { mutableStateOf(false) }

    val permLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { /* result intentionally ignored — user can still use the app */ }

    LaunchedEffect(language) { LocaleManager.apply(language) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.onboarding_title)) }) },
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.onboarding_intro))

            Text(stringResource(R.string.start_date), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            TextButton(onClick = { showDatePicker = true }) {
                Text(startDate.toString())
            }

            Text(stringResource(R.string.body_weight_kg), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = weightText,
                onValueChange = { new -> weightText = new.filter { it.isDigit() || it == '.' } },
                label = { Text("kg") },
                singleLine = true,
            )

            Text(stringResource(R.string.language), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            LanguageRow(language) { language = it }

            Text(stringResource(R.string.heel_drop_variant), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            HeelVariantRow(heelVariant) { heelVariant = it }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val weight = weightText.toDoubleOrNull() ?: 75.0
                    scope.launch {
                        app.userPrefs.completeOnboarding(startDate, weight, language, heelVariant)
                        ReminderScheduler.scheduleAll(context.applicationContext)
                        onDone()
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                },
            ) { Text(stringResource(R.string.start)) }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = startDate
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { millis ->
                        startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) { DatePicker(state = state) }
    }
}

@Composable
private fun LanguageRow(current: AppLanguage, onPick: (AppLanguage) -> Unit) {
    Column {
        for (lang in listOf(AppLanguage.SYSTEM, AppLanguage.FRENCH, AppLanguage.ENGLISH)) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                RadioButton(selected = current == lang, onClick = { onPick(lang) })
                Text(
                    when (lang) {
                        AppLanguage.SYSTEM -> stringResource(R.string.language_system)
                        AppLanguage.FRENCH -> stringResource(R.string.language_french)
                        AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                    },
                )
            }
        }
    }
}

@Composable
private fun HeelVariantRow(current: HeelDropVariant, onPick: (HeelDropVariant) -> Unit) {
    Column {
        for (variant in HeelDropVariant.entries) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                RadioButton(selected = current == variant, onClick = { onPick(variant) })
                Text(
                    when (variant) {
                        HeelDropVariant.BOTH -> stringResource(R.string.heel_drop_both)
                        HeelDropVariant.STRAIGHT_KNEE_ONLY -> stringResource(R.string.heel_drop_straight_only)
                    },
                )
            }
        }
    }
}
