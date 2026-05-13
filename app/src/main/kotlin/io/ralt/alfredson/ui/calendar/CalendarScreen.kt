package io.ralt.alfredson.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.ralt.alfredson.R
import io.ralt.alfredson.data.SessionLog
import io.ralt.alfredson.data.UserPrefs
import io.ralt.alfredson.domain.Slot
import io.ralt.alfredson.domain.protocolStatus
import io.ralt.alfredson.ui.rememberApp
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    onOpenDay: (Int) -> Unit,
) {
    val app = rememberApp()
    val prefs by app.userPrefs.flow.collectAsStateWithLifecycle(initialValue = UserPrefs())
    val log by app.sessionLog.flow.collectAsStateWithLifecycle(initialValue = SessionLog())
    val schedule = remember { app.scheduleProvider.schedule() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.calendar)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { inner ->
        val startDate = prefs.startDate ?: return@Scaffold EmptyText(inner, stringResource(R.string.no_start_date))
        val status = protocolStatus(startDate, LocalDate.now(), schedule.totalDays)
        val todaySlot: Slot? = status.slot

        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val streak = computeStreak(log, todaySlot)
            Text(
                stringResource(R.string.streak, streak),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                stringResource(R.string.start_date_value, startDate.toString()),
                style = MaterialTheme.typography.bodyMedium,
            )

            schedule.weeks.forEachIndexed { weekIdx, week ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = stringResource(R.string.week_short, week.week),
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    week.days.forEachIndexed { dayIdx, _ ->
                        val absolute = weekIdx * 7 + dayIdx
                        val entry = log.entryFor(absolute)
                        val isToday = todaySlot?.absoluteDayIndex == absolute
                        val isFuture = todaySlot != null && absolute > todaySlot.absoluteDayIndex
                        Tile(
                            done = entry.isFullyDone,
                            partial = entry.isPartial,
                            today = isToday,
                            future = isFuture,
                            onClick = { onOpenDay(absolute) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Tile(done: Boolean, partial: Boolean, today: Boolean, future: Boolean, onClick: () -> Unit) {
    val color = when {
        done -> Color(0xFF66BB6A)
        partial -> Color(0xFFFFB74D)
        future -> Color.Transparent
        else -> Color(0xFFE0E0E0)
    }
    val borderColor = if (today) MaterialTheme.colorScheme.primary else Color(0xFFBDBDBD)
    Box(
        modifier = Modifier
            .padding(2.dp)
            .size(28.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color)
            .border(if (today) 2.dp else 1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick),
    )
}

@Composable
private fun EmptyText(inner: PaddingValues, text: String) {
    Column(
        modifier = Modifier.padding(inner).padding(24.dp).fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text)
    }
}

private fun computeStreak(log: SessionLog, todaySlot: Slot?): Int {
    if (todaySlot == null) return 0
    var streak = 0
    var i = todaySlot.absoluteDayIndex
    if (!log.entryFor(i).isFullyDone) i -= 1
    while (i >= 0 && log.entryFor(i).isFullyDone) {
        streak += 1
        i -= 1
    }
    return streak
}
