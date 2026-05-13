package io.ralt.alfredson.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ralt.alfredson.R
import io.ralt.alfredson.data.HeelDropVariant
import io.ralt.alfredson.data.SessionEntry
import io.ralt.alfredson.data.UserPrefs
import io.ralt.alfredson.domain.DayPlan
import io.ralt.alfredson.domain.ProtocolState
import io.ralt.alfredson.domain.calculateDumbbellKg
import io.ralt.alfredson.domain.protocolStatus
import io.ralt.alfredson.ui.rememberApp
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayScreen(
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val prefs by app.userPrefs.flow.collectAsStateWithLifecycle(initialValue = UserPrefs())
    val log by app.sessionLog.flow.collectAsStateWithLifecycle(initialValue = io.ralt.alfredson.data.SessionLog())
    val schedule = remember { app.scheduleProvider.schedule() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onOpenCalendar) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = stringResource(R.string.calendar))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings))
                    }
                },
            )
        },
    ) { inner ->
        val startDate = prefs.startDate
        if (startDate == null) {
            EmptyMessage(inner, stringResource(R.string.no_start_date))
            return@Scaffold
        }
        val status = protocolStatus(startDate, LocalDate.now(), schedule.totalDays)
        when (status.state) {
            ProtocolState.NOT_STARTED -> EmptyMessage(inner, stringResource(R.string.not_started_yet, startDate.toString()))
            ProtocolState.COMPLETED -> EmptyMessage(inner, stringResource(R.string.protocol_complete))
            ProtocolState.IN_PROGRESS -> {
                val slot = status.slot!!
                val day = schedule.dayPlan(slot.weekIndex, slot.dayIndex)
                if (day == null) {
                    EmptyMessage(inner, stringResource(R.string.schedule_error))
                    return@Scaffold
                }
                val entry = log.entryFor(slot.absoluteDayIndex)
                Column(
                    modifier = Modifier
                        .padding(inner)
                        .padding(16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        text = stringResource(R.string.week_day_header, slot.weekIndex + 1, slot.dayIndex + 1),
                        style = MaterialTheme.typography.headlineSmall,
                    )
                    Text(
                        text = LocalDate.now().toString(),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    PlanCard(day, prefs)
                    PrescriptionCard(prefs.heelDropVariant)
                    SessionCheckboxes(
                        entry = entry,
                        onMorningChange = { v ->
                            scope.launch { app.sessionLog.setMorning(slot.absoluteDayIndex, v) }
                        },
                        onEveningChange = { v ->
                            scope.launch { app.sessionLog.setEvening(slot.absoluteDayIndex, v) }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMessage(inner: PaddingValues, text: String) {
    Column(
        modifier = Modifier
            .padding(inner)
            .padding(24.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun PlanCard(day: DayPlan, prefs: UserPrefs) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(stringResource(R.string.todays_exercise), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_support), modifier = Modifier.weight(1f))
                Text(supportLabel(day.support))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.label_speed), modifier = Modifier.weight(1f))
                Text(speedLabel(day.speed))
            }
            if (day.extraLoadPctBodyWeight > 0) {
                val kg = calculateDumbbellKg(prefs.bodyWeightKg, day.extraLoadPctBodyWeight)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.label_extra_load), modifier = Modifier.weight(1f))
                    Text("${day.extraLoadPctBodyWeight}% (${formatKg(kg)} kg)")
                }
            }
            day.plyometric?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.label_plyometric), modifier = Modifier.weight(1f))
                    Text(plyometricLabel(it))
                }
            }
        }
    }
}

@Composable
private fun PrescriptionCard(variant: HeelDropVariant) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.prescription_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.prescription_sets))
            when (variant) {
                HeelDropVariant.BOTH -> {
                    Text(stringResource(R.string.prescription_straight_knee))
                    Text(stringResource(R.string.prescription_bent_knee))
                }
                HeelDropVariant.STRAIGHT_KNEE_ONLY -> {
                    Text(stringResource(R.string.prescription_straight_knee))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.prescription_twice_daily), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun SessionCheckboxes(
    entry: SessionEntry,
    onMorningChange: (Boolean) -> Unit,
    onEveningChange: (Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(stringResource(R.string.todays_sessions), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = entry.morning, onCheckedChange = onMorningChange)
                Text(stringResource(R.string.morning), modifier = Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = entry.evening, onCheckedChange = onEveningChange)
                Text(stringResource(R.string.evening), modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun supportLabel(s: io.ralt.alfredson.domain.Support): String = when (s) {
    io.ralt.alfredson.domain.Support.BIPODAL -> stringResource(R.string.support_bipodal)
    io.ralt.alfredson.domain.Support.BIPODAL_OPPOSITE_BIAS -> stringResource(R.string.support_bipodal_opposite)
    io.ralt.alfredson.domain.Support.UNIPODAL -> stringResource(R.string.support_unipodal)
}

@Composable
private fun speedLabel(s: io.ralt.alfredson.domain.Speed): String = when (s) {
    io.ralt.alfredson.domain.Speed.SLOW -> stringResource(R.string.speed_slow)
    io.ralt.alfredson.domain.Speed.MEDIUM -> stringResource(R.string.speed_medium)
    io.ralt.alfredson.domain.Speed.FAST -> stringResource(R.string.speed_fast)
}

@Composable
private fun plyometricLabel(p: io.ralt.alfredson.domain.Plyometric): String = when (p) {
    io.ralt.alfredson.domain.Plyometric.POGO_BILATERAL -> stringResource(R.string.plyo_pogo_bilateral)
    io.ralt.alfredson.domain.Plyometric.SINGLE_LEG_HOP -> stringResource(R.string.plyo_single_leg_hop)
}

private fun formatKg(kg: Double): String {
    return if (kg == kg.toLong().toDouble()) kg.toLong().toString() else String.format("%.1f", kg)
}
