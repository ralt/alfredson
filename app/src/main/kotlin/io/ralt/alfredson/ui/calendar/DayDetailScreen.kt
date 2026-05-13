package io.ralt.alfredson.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ralt.alfredson.R
import io.ralt.alfredson.data.SessionLog
import io.ralt.alfredson.data.UserPrefs
import io.ralt.alfredson.domain.dateForSlot
import io.ralt.alfredson.ui.rememberApp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dayIndex: Int,
    onBack: () -> Unit,
) {
    val app = rememberApp()
    val scope = rememberCoroutineScope()
    val prefs by app.userPrefs.flow.collectAsStateWithLifecycle(initialValue = UserPrefs())
    val log by app.sessionLog.flow.collectAsStateWithLifecycle(initialValue = SessionLog())
    val schedule = remember { app.scheduleProvider.schedule() }

    val weekIdx = dayIndex / 7
    val dayInWeek = dayIndex % 7
    val plan = schedule.dayPlan(weekIdx, dayInWeek)
    val entry = log.entryFor(dayIndex)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.week_day_header, weekIdx + 1, dayInWeek + 1))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { inner ->
        Column(
            modifier = Modifier.padding(inner).padding(16.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            prefs.startDate?.let { sd ->
                Text(dateForSlot(sd, dayIndex).toString(), style = MaterialTheme.typography.bodyMedium)
            }
            plan?.let {
                Text(
                    "${it.support.name.replace('_', ' ')} • ${it.speed.name}" +
                        (if (it.extraLoadPctBodyWeight > 0) " • +${it.extraLoadPctBodyWeight}%" else "") +
                        (it.plyometric?.let { p -> " • ${p.name.replace('_', ' ')}" } ?: ""),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = entry.morning, onCheckedChange = {
                    scope.launch { app.sessionLog.setMorning(dayIndex, it) }
                })
                Text(stringResource(R.string.morning))
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Checkbox(checked = entry.evening, onCheckedChange = {
                    scope.launch { app.sessionLog.setEvening(dayIndex, it) }
                })
                Text(stringResource(R.string.evening))
            }
        }
    }
}
