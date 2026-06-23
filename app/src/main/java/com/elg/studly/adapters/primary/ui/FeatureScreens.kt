package com.elg.studly.adapters.primary.ui

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.viewinterop.AndroidView
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Lightbulb
import com.elg.studly.BuildConfig
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Forum
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.ui.text.style.TextOverflow
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.core.content.FileProvider
import java.io.File
import com.elg.studly.adapters.secondary.pdf.PdfGenerator
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elg.studly.R
import com.elg.studly.adapters.primary.state.FeatureUiState
import com.elg.studly.adapters.primary.viewmodel.SettingsViewModel
import com.elg.studly.adapters.primary.viewmodel.StudentViewModel
import com.elg.studly.domain.model.Absence
import com.elg.studly.domain.model.AcademicDocument
import com.elg.studly.domain.model.AgendaColorMode
import com.elg.studly.domain.model.AgendaEvent
import com.elg.studly.domain.model.CalendarAccount
import com.elg.studly.domain.model.Course
import com.elg.studly.domain.model.DashboardSummary
import com.elg.studly.domain.model.DirectoryPerson
import com.elg.studly.domain.model.DirectoryRole
import com.elg.studly.domain.model.Grade
import com.elg.studly.domain.model.combineCcExam
import com.elg.studly.domain.model.mainGrades
import com.elg.studly.domain.model.NewsItem
import com.elg.studly.domain.model.StudentEvent
import com.elg.studly.domain.model.Practical
import com.elg.studly.domain.model.Project
import com.elg.studly.domain.model.ProjectGroup
import com.elg.studly.domain.model.ProjectMessage
import com.elg.studly.domain.model.ProjectStep
import com.elg.studly.domain.model.ThemeMode
import com.elg.studly.domain.model.UserSettings
import com.elg.studly.domain.model.REMINDER_LEAD_CHOICES
import com.elg.studly.domain.model.SyncFeature
import androidx.compose.material3.Slider
import kotlin.math.roundToInt
import com.elg.studly.domain.model.progress
import com.elg.studly.domain.model.isToeicExcluded
import com.elg.studly.domain.model.isExcludedFromAverage
import com.elg.studly.domain.model.isNotCounted
import com.elg.studly.domain.model.toGradeSummary
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.FormatStyle

@Composable
fun DashboardScreen(
    viewModel: StudentViewModel,
    onNavigateToTab: (String) -> Unit
) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
    var selectedEvent by remember { mutableStateOf<AgendaEvent?>(null) }

    selectedEvent?.let { event ->
        AgendaEventDetailsDialog(
            event = event,
            onDismiss = { selectedEvent = null }
        )
    }

    FeatureStateContent(
        state = state,
        empty = { it.isDashboardEmpty() },
        emptyTitle = R.string.dashboard_empty_title,
        emptyBody = R.string.dashboard_empty_body,
        onRetry = viewModel::refresh
    ) { summary ->
        val dashboard = summary ?: return@FeatureStateContent
        dashboard.profile?.let { profile ->
            item {
                DataCard {
                    StudentAvatar(
                        avatarUrl = profile.avatarUrl,
                        displayName = profile.displayName,
                        modifier = Modifier.size(72.dp)
                    )
                    Text(
                        text = profile.displayName.orUntitled(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    LabelValue(R.string.profile_email, profile.email.orEmpty())
                    LabelValue(R.string.profile_school, profile.school.orEmpty())
                    LabelValue(R.string.profile_program, profile.program.orEmpty())
                    LabelValue(R.string.profile_year, profile.academicYear.orEmpty())
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_next_course),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                dashboard.nextEvent?.let { nextEvent ->
                    IconButton(
                        onClick = {
                            val nextEventDate = nextEvent.startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
                            viewModel.navigateToAgendaDate(nextEventDate)
                            onNavigateToTab("agenda")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarMonth,
                            contentDescription = stringResource(R.string.dashboard_go_to_agenda),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            dashboard.nextEvent?.let { AgendaEventCard(it, onOpen = { selectedEvent = it }) }
                ?: CompactCard { Text(stringResource(R.string.dashboard_no_next_course)) }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_latest_grades),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                dashboard.latestGrades.firstOrNull()?.period?.let { period ->
                    IconButton(
                        onClick = {
                            viewModel.navigateToGradesPeriod(period)
                            onNavigateToTab("grades")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = stringResource(R.string.dashboard_go_to_grades),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        if (dashboard.latestGrades.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.grades_empty_title)) } }
        } else {
            items(dashboard.latestGrades, key = { it.id }) { grade ->
                GradeCard(
                    grade = grade,
                    onOpen = {
                        grade.period?.let { period ->
                            viewModel.navigateToGradesPeriod(period)
                            onNavigateToTab("grades")
                        }
                    }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_recent_absences),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                dashboard.recentAbsences.firstOrNull()?.let { absence ->
                    IconButton(
                        onClick = {
                            viewModel.navigateToAbsencesPeriod(absence.academicYearLabel())
                            onNavigateToTab("absences")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowForward,
                            contentDescription = stringResource(R.string.dashboard_go_to_absences),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        if (dashboard.recentAbsences.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.absences_empty_title)) } }
        } else {
            item {
                val lastAbsence = dashboard.recentAbsences.first()
                AbsenceCard(
                    absence = lastAbsence,
                    onOpen = {
                        viewModel.navigateToAbsencesPeriod(lastAbsence.academicYearLabel())
                        onNavigateToTab("absences")
                    }
                )
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.dashboard_due_projects),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = { onNavigateToTab("projects") }) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowForward,
                        contentDescription = stringResource(R.string.dashboard_go_to_projects),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        val dueProjects = dashboard.dueProjects.filter { it.name.isNotBlank() }
        if (dueProjects.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.projects_empty_title)) } }
        } else {
            items(dueProjects, key = { it.id }) { project ->
                val now = remember { Instant.now() }
                val nextDeadline = remember(project) {
                    project.deadline?.takeIf { it.isAfter(now) }
                        ?: project.steps.mapNotNull { it.deadline }.filter { it.isAfter(now) }.minOrNull()
                }
                CompactCard(modifier = Modifier.clickable { onNavigateToTab("projects") }) {
                    Text(
                        text = project.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LabelValue(R.string.projects_deadline, formatInstant(nextDeadline))
                }
            }
        }
        dashboard.lastSyncAt?.let {
            item {
                CompactCard {
                    LabelValue(R.string.settings_last_sync, formatInstant(it))
                }
            }
        }
    }
}

@Composable
fun AgendaScreen(
    viewModel: StudentViewModel,
    settingsViewModel: SettingsViewModel,
    highlightedEventId: String? = null
) {
    val state by viewModel.agenda.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val calendars by settingsViewModel.calendars.collectAsStateWithLifecycle()
    val selectedCalendarId by settingsViewModel.selectedCalendarId.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedMode by remember { mutableStateOf(AgendaMode.Grid) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedEvent by remember { mutableStateOf<AgendaEvent?>(null) }
    var showCalendarPicker by remember { mutableStateOf(false) }
    var pendingCalendarPick by remember { mutableStateOf(false) }

    val calendarConnected = settingsState.settings?.calendarSyncEnabled == true && selectedCalendarId != null

    val connectCalendar = {
        settingsViewModel.setCalendarSync(true)
        pendingCalendarPick = true
        settingsViewModel.loadCalendars()
    }
    val connectLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            connectCalendar()
        }
    }
    LaunchedEffect(settingsViewModel) {
        if (context.hasCalendarPermissions()) settingsViewModel.loadCalendars()
    }
    LaunchedEffect(calendars, pendingCalendarPick) {
        if (pendingCalendarPick && calendars.isNotEmpty()) {
            showCalendarPicker = true
            pendingCalendarPick = false
        }
    }
    if (showCalendarPicker) {
        CalendarAccountPickerDialog(
            calendars = calendars,
            selectedCalendarId = selectedCalendarId,
            onSelect = { id ->
                settingsViewModel.selectCalendar(id)
                showCalendarPicker = false
                viewModel.syncAgendaToCalendar(state.data)
            },
            onDismiss = { showCalendarPicker = false }
        )
    }

    LaunchedEffect(state.data, highlightedEventId) {
        if (!highlightedEventId.isNullOrBlank()) {
            val event = state.data.firstOrNull { it.id == highlightedEventId }
            if (event != null) {
                selectedDate = event.startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
                selectedEvent = event
                selectedMode = AgendaMode.Week7
            }
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.agendaDateToNavigate.collect { targetDate ->
            selectedDate = targetDate
            selectedMode = AgendaMode.Week7
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.calendarSyncCompleted.collect {
            android.widget.Toast.makeText(context, context.getString(R.string.agenda_sync_done), android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    val daysWithEvents = remember(state.data) {
        state.data.map { it.startsAt.atZone(ZoneId.systemDefault()).toLocalDate() }.toSet()
    }
    val calendarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.syncAgendaToCalendar(state.data)
        }
    }
    selectedEvent?.let { event ->
        AgendaEventDetailsDialog(
            event = event,
            onDismiss = { selectedEvent = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<AgendaEvent>::isEmpty,
        emptyTitle = R.string.agenda_empty_title,
        emptyBody = R.string.agenda_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Agenda) }
    ) { events ->
        
        item {
            AgendaModeSelector(selectedMode) { selectedMode = it }
        }

        if (!calendarConnected) {
            item {
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (context.hasCalendarPermissions()) {
                            connectCalendar()
                        } else {
                            connectLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_connect_google))
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (context.hasCalendarPermissions()) {
                            viewModel.syncAgendaToCalendar(events)
                        } else {
                            calendarLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_sync_calendar))
                }
                
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            val icsString = events.toIcsString()
                            val file = File(context.cacheDir, "agenda.ics")
                            file.writeText(icsString)
                            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/calendar"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, context.getString(R.string.agenda_export_title)))
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.agenda_sync_calendar_export))
                }
            }
        }
        
        if (selectedMode != AgendaMode.List) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            selectedDate = when (selectedMode) {
                                AgendaMode.Day -> selectedDate.minusDays(1)
                                AgendaMode.Grid, AgendaMode.Week7 -> selectedDate.minusWeeks(1)
                                AgendaMode.Month -> selectedDate.minusMonths(1)
                                else -> selectedDate
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_previous))
                    }
                    
                    val headerText = when (selectedMode) {
                        AgendaMode.Day -> formatDate(selectedDate)
                        AgendaMode.Grid, AgendaMode.Week7 -> {
                            val startOfWeek = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            val endDay = when (selectedMode) {
                                AgendaMode.Grid -> 5
                                else -> 6
                            }
                            val endOfWeek = startOfWeek.plusDays(endDay.toLong())
                            "${formatDate(startOfWeek)} - ${formatDate(endOfWeek)}"
                        }
                        AgendaMode.Month -> {
                            val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", currentJavaLocale())
                            selectedDate.format(formatter).replaceFirstChar { it.uppercase() }
                        }
                        else -> ""
                    }
                    
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = {
                            selectedDate = when (selectedMode) {
                                AgendaMode.Day -> selectedDate.plusDays(1)
                                AgendaMode.Grid, AgendaMode.Week7 -> selectedDate.plusWeeks(1)
                                AgendaMode.Month -> selectedDate.plusMonths(1)
                                else -> selectedDate
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowForward, contentDescription = stringResource(R.string.cd_next))
                    }
                }
            }
        }
        
        when (selectedMode) {
            AgendaMode.Grid -> {
                val startOfWeek = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                item {
                    AgendaWeekGrid(
                        weekStart = startOfWeek,
                        events = events,
                        colorMode = settingsState.settings?.agendaColorMode ?: AgendaColorMode.Course,
                        onOpen = { ev -> selectedEvent = ev }
                    )
                }
            }
            AgendaMode.List -> {
                if (events.isEmpty()) {
                    item { CompactCard { Text(stringResource(R.string.agenda_filter_empty)) } }
                } else {
                    items(events, key = { it.id }) { event ->
                        AgendaEventCard(
                            event = event,
                            onOpen = { selectedEvent = event }
                        )
                    }
                }
            }
            AgendaMode.Day -> {
                val dayEvents = events.filter { it.startsAt.atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate }
                if (dayEvents.isEmpty()) {
                    item { CompactCard { Text(stringResource(R.string.agenda_filter_empty)) } }
                } else {
                    items(dayEvents, key = { it.id }) { event ->
                        AgendaEventCard(
                            event = event,
                            onOpen = { selectedEvent = event }
                        )
                    }
                }
            }
            AgendaMode.Week7 -> {
                val startOfWeek = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val numDays = 7
                val daysList = (0 until numDays).map { startOfWeek.plusDays(it.toLong()) }
                
                var hasAnyEvent = false
                daysList.forEach { day ->
                    val dayEvents = events.filter { it.startsAt.atZone(ZoneId.systemDefault()).toLocalDate() == day }
                    if (dayEvents.isNotEmpty()) {
                        hasAnyEvent = true
                        item {
                            Text(
                                text = formatDate(day),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        items(dayEvents, key = { it.id }) { event ->
                            AgendaEventCard(
                                event = event,
                                onOpen = { selectedEvent = event }
                            )
                        }
                    }
                }
                if (!hasAnyEvent) {
                    item { CompactCard { Text(stringResource(R.string.agenda_filter_empty)) } }
                }
            }
            AgendaMode.Month -> {
                item {
                    MonthCalendarGrid(
                        selectedDate = selectedDate,
                        daysWithEvents = daysWithEvents,
                        onDaySelected = { selectedDate = it }
                    )
                }
                
                val dayEvents = events.filter { it.startsAt.atZone(ZoneId.systemDefault()).toLocalDate() == selectedDate }
                if (dayEvents.isNotEmpty()) {
                    item {
                        Text(
                            text = formatDate(selectedDate),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 12.dp, bottom = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(dayEvents, key = { it.id }) { event ->
                        AgendaEventCard(
                            event = event,
                            onOpen = { selectedEvent = event }
                        )
                    }
                } else {
                    item {
                        CompactCard {
                            Text(
                                text = stringResource(R.string.agenda_filter_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val PRIVACY_POLICY_URL = "https://github.com/Erwanlg0/Myges-Mobile/blob/main/PRIVACY.MD"
private const val SOURCE_CODE_URL = "https://github.com/Erwanlg0/Myges-Mobile"
private const val LICENSES_URL = "https://github.com/Erwanlg0/Myges-Mobile/blob/main/THIRD_PARTY_LICENSES.md"
private const val SUPPORT_EMAIL = "erwan.luce.guedon@gmail.com"
private const val GITHUB_ISSUES_BUG_URL = "https://github.com/Erwanlg0/Myges-Mobile/issues/new?labels=bug"
private const val GITHUB_ISSUES_FEATURE_URL = "https://github.com/Erwanlg0/Myges-Mobile/issues/new?labels=enhancement"

private val ACADEMIC_YEAR_REGEX = Regex("\\d{4}\\s*-\\s*\\d{4}")
private val SEMESTER_REGEX = Regex("Semestre\\s*\\d+", RegexOption.IGNORE_CASE)
private val FOUR_DIGITS_REGEX = Regex("\\d{4}")
private val DIGITS_REGEX = Regex("\\d+")

private fun deriveMainGrades(source: List<Grade>): List<Grade> = source.mainGrades()

@Composable
fun GradesScreen(
    viewModel: StudentViewModel,
    highlightedGradeId: String? = null
) {
    val state by viewModel.grades.collectAsStateWithLifecycle()
    var selectedGrade by remember { mutableStateOf<Grade?>(null) }
    val context = LocalContext.current
    val simulationPrefs = remember(context) {
        context.getSharedPreferences("grade_simulation", Context.MODE_PRIVATE)
    }
    
    val gradesList = state.data.orEmpty()

    val years = remember(gradesList) {
        gradesList.mapNotNull { it.academicYearLabel().takeIf(String::isNotBlank) }
            .distinct()
            .sortedDescending()
    }
    var selectedYear by remember(years) {
        mutableStateOf(years.firstOrNull())
    }
    val semesters = remember(gradesList, selectedYear) {
        gradesList.filter { selectedYear == null || it.academicYearLabel() == selectedYear }
            .mapNotNull { it.semesterLabel() }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith { p1, p2 -> comparePeriods(p2, p1) }
    }
    var selectedSemester by remember(selectedYear, semesters) {
        mutableStateOf<String?>(null)
    }
    var simulationMode by remember {
        mutableStateOf(simulationPrefs.getBoolean("enabled", false))
    }
    var simulationResetVersion by remember { mutableStateOf(0) }
    var simulatedValues by remember(gradesList) {
        mutableStateOf(loadGradeSimulations(simulationPrefs, gradesList.map { it.id }.toSet()))
    }
    var blockAssignments by remember(gradesList) {
        mutableStateOf(loadGradeBlocks(simulationPrefs, gradesList.map { it.blockKey() }.toSet()))
    }
    var groupByBlock by remember { mutableStateOf(simulationPrefs.getBoolean("group_by_block", false)) }
    val hasBlocks = remember(blockAssignments) { blockAssignments.values.any { it.isNotBlank() } }
    LaunchedEffect(hasBlocks) { if (!hasBlocks) groupByBlock = false }

    val displayedGrades = remember(gradesList, selectedYear, selectedSemester, simulationMode, simulatedValues) {
        val filtered = gradesList.filter { grade ->
            (selectedYear == null || grade.academicYearLabel() == selectedYear) &&
                (selectedSemester == null || grade.semesterLabel() == selectedSemester)
        }
        (if (simulationMode) filtered.withSimulatedValues(simulatedValues) else filtered)
            .withRecomputedMainGrades(if (simulationMode) simulatedValues.keys else emptySet())
    }
    val mainGrades = remember(displayedGrades) { deriveMainGrades(displayedGrades) }
    
    val blockMainGrades = remember(gradesList, selectedYear, simulationMode, simulatedValues) {
        val yearGrades = gradesList.filter { selectedYear == null || it.academicYearLabel() == selectedYear }
        val yearDisplayed = (if (simulationMode) yearGrades.withSimulatedValues(simulatedValues) else yearGrades)
            .withRecomputedMainGrades(if (simulationMode) simulatedValues.keys else emptySet())
        deriveMainGrades(yearDisplayed)
    }

    LaunchedEffect(viewModel) {
        viewModel.gradesPeriodToNavigate.collect { targetPeriod ->
            selectedYear = targetPeriod.academicYearLabel()
            selectedSemester = targetPeriod.semesterLabel()
        }
    }

    LaunchedEffect(gradesList, highlightedGradeId) {
        if (!highlightedGradeId.isNullOrBlank()) {
            val grade = gradesList.firstOrNull { it.id == highlightedGradeId }
            if (grade != null) {
                selectedYear = grade.academicYearLabel()
                selectedSemester = grade.semesterLabel()
                selectedGrade = grade
            }
        }
    }

    selectedGrade?.let { grade ->
        val components = remember(grade, gradesList, simulatedValues, simulationMode) {
            gradesList.filter { it.courseName == grade.courseName && it.period == grade.period }
                .let { if (simulationMode) it.withSimulatedValues(simulatedValues) else it }
        }
        GradeDetailsDialog(
            grade = if (simulationMode) grade.withSimulatedValue(simulatedValues[grade.id]) else grade,
            components = components,
            block = blockAssignments[grade.blockKey()].orEmpty(),
            simulationMode = simulationMode,
            resetVersion = simulationResetVersion,
            onValueChange = { gradeId, value ->
                val updatedValues = if (value == null) {
                    simulatedValues - gradeId
                } else {
                    simulatedValues + (gradeId to value)
                }
                simulatedValues = updatedValues
                saveGradeSimulations(simulationPrefs, updatedValues)
            },
            onBlockChange = { block ->
                val key = grade.blockKey()
                val updatedBlocks = if (block.isBlank()) {
                    blockAssignments - key
                } else {
                    blockAssignments + (key to block)
                }
                blockAssignments = updatedBlocks
                saveGradeBlocks(simulationPrefs, updatedBlocks)
            },
            onDismiss = { selectedGrade = null }
        )
    }

    FeatureStateContent(
        state = state,
        empty = List<Grade>::isEmpty,
        emptyTitle = R.string.grades_empty_title,
        emptyBody = R.string.grades_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Grades) }
    ) { _ ->
        if (years.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var yearExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        CompactCard(
                            modifier = Modifier.clickable { yearExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedYear ?: stringResource(R.string.common_all),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            (listOf(null) + years).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year ?: stringResource(R.string.common_all)) },
                                    onClick = {
                                        selectedYear = year
                                        selectedSemester = null
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    
                    if (!groupByBlock) {
                        var semesterExpanded by remember { mutableStateOf(false) }
                        Box(modifier = Modifier.weight(1f)) {
                            CompactCard(
                                modifier = Modifier.clickable { semesterExpanded = true }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedSemester ?: stringResource(R.string.common_all),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                                }
                            }
                            DropdownMenu(
                                expanded = semesterExpanded,
                                onDismissRequest = { semesterExpanded = false }
                            ) {
                                (listOf(null) + semesters).forEach { semester ->
                                    DropdownMenuItem(
                                        text = { Text(semester ?: stringResource(R.string.common_all)) },
                                        onClick = {
                                            selectedSemester = semester
                                            semesterExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item { GradeSummaryCard(mainGrades, displayedGrades) }
        if (!groupByBlock && blockAssignments.isNotEmpty()) {
            item { GradeBlockAveragesCard(mainGrades, blockAssignments) }
        }
        item {
            DataCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.grades_simulation_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Button(
                        onClick = {
                            simulationMode = !simulationMode
                            simulationPrefs.edit().putBoolean("enabled", simulationMode).apply()
                        }
                    ) {
                        Text(stringResource(if (simulationMode) R.string.grades_real_mode else R.string.grades_simulation_mode))
                    }
                }
                if (simulationMode) {
                    TextButton(
                        onClick = {
                            simulatedValues = emptyMap()
                            blockAssignments = emptyMap()
                            clearGradeSimulations(simulationPrefs)
                            simulationResetVersion++
                        },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.grades_simulation_reset))
                    }
                }
            }
        }
        
        if (hasBlocks) {
            item {
                fun setGroupByBlock(value: Boolean) {
                    groupByBlock = value
                    simulationPrefs.edit().putBoolean("group_by_block", value).apply()
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = !groupByBlock,
                        onClick = { setGroupByBlock(false) },
                        label = { Text(stringResource(R.string.grades_group_semester)) }
                    )
                    FilterChip(
                        selected = groupByBlock,
                        onClick = { setGroupByBlock(true) },
                        label = { Text(stringResource(R.string.grades_group_block)) }
                    )
                }
            }
        }

        if (groupByBlock) {
            val grouped = blockMainGrades.groupBy { blockAssignments[it.blockKey()]?.takeIf(String::isNotBlank) }
            val orderedBlocks = grouped.keys.filterNotNull()
                .sortedWith(compareBy<String> { it.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it })
            orderedBlocks.forEach { block ->
                val blockGrades = grouped[block].orEmpty()
                item(key = "block-$block") {
                    GradeGroupHeader(stringResource(R.string.grades_block_prefix, block), blockGrades)
                }
                items(blockGrades, key = { "b-${it.id}" }) { grade ->
                    GradeCard(grade = grade, onOpen = { selectedGrade = grade })
                }
            }
            grouped[null]?.takeIf { it.isNotEmpty() }?.let { unassigned ->
                item(key = "block-none") {
                    GradeGroupHeader(stringResource(R.string.grades_block_unassigned), unassigned)
                }
                items(unassigned, key = { "u-${it.id}" }) { grade ->
                    GradeCard(grade = grade, onOpen = { selectedGrade = grade })
                }
            }
        } else {
            items(mainGrades, key = { it.id }) { grade ->
                GradeCard(grade = grade, onOpen = { selectedGrade = grade })
            }
        }
    }
}

@Composable
fun AbsencesScreen(
    viewModel: StudentViewModel,
    highlightedAbsenceId: String? = null
) {
    val state by viewModel.absences.collectAsStateWithLifecycle()
    val coursesState by viewModel.courses.collectAsStateWithLifecycle()
    val gradesState by viewModel.grades.collectAsStateWithLifecycle()
    
    val absencesList = state.data.orEmpty()

    val coursesList = coursesState.data.orEmpty()
    val gradesList = gradesState.data.orEmpty()

    val years = remember(absencesList, coursesList, gradesList) {
        (absencesList.map { it.academicYearLabel() } +
            coursesList.mapNotNull { it.academicYearLabel() } +
            gradesList.mapNotNull { it.academicYearLabel().takeIf(String::isNotBlank) })
            .filter { it.isNotBlank() }
            .distinct()
            .sortedDescending()
    }
    var selectedYear by remember(years) {
        mutableStateOf(years.firstOrNull())
    }
    val semesters = remember(absencesList, coursesList, gradesList, selectedYear) {
        (absencesList.filter { selectedYear == null || it.academicYearLabel() == selectedYear }.mapNotNull { it.semesterLabel() } +
            coursesList.filter { selectedYear == null || it.academicYearLabel() == selectedYear }.mapNotNull { it.semesterLabel() } +
            gradesList.filter { selectedYear == null || it.academicYearLabel() == selectedYear }.mapNotNull { it.semesterLabel() })
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith { p1, p2 -> comparePeriods(p2, p1) }
    }
    var selectedSemester by remember(selectedYear, semesters) {
        mutableStateOf<String?>(null)
    }
    var selectedJustified by remember { mutableStateOf<Boolean?>(null) }

    val statusLabel = when (selectedJustified) {
        true -> stringResource(R.string.absences_justified)
        false -> stringResource(R.string.absences_unjustified)
        null -> stringResource(R.string.common_all)
    }

    LaunchedEffect(viewModel) {
        viewModel.absencesPeriodToNavigate.collect { targetPeriodOrYear ->
            selectedYear = targetPeriodOrYear.academicYearLabel()
            selectedSemester = targetPeriodOrYear.semesterLabel()
        }
    }

    LaunchedEffect(absencesList, highlightedAbsenceId) {
        if (!highlightedAbsenceId.isNullOrBlank()) {
            val absence = absencesList.firstOrNull { it.id == highlightedAbsenceId }
            if (absence != null) {
                selectedYear = absence.academicYearLabel()
                selectedSemester = absence.semesterLabel()
            }
        }
    }

    FeatureStateContent(
        state = state,
        empty = List<Absence>::isEmpty,
        emptyTitle = R.string.absences_empty_title,
        emptyBody = R.string.absences_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Absences) }
    ) { absences ->
        val filteredAbsences = absences.filter { absence ->
            (selectedYear == null || absence.academicYearLabel() == selectedYear) &&
                (selectedSemester == null || absence.semesterLabel() == selectedSemester) &&
                (selectedJustified == null || absence.justified == selectedJustified)
        }

        if (years.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var yearExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        CompactCard(
                            modifier = Modifier.clickable { yearExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedYear ?: stringResource(R.string.common_all),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            (listOf(null) + years).forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year ?: stringResource(R.string.common_all)) },
                                    onClick = {
                                        selectedYear = year
                                        selectedSemester = null
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    var semesterExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        CompactCard(
                            modifier = Modifier.clickable { semesterExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedSemester ?: stringResource(R.string.common_all),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = semesterExpanded,
                            onDismissRequest = { semesterExpanded = false }
                        ) {
                            (listOf(null) + semesters).forEach { semester ->
                                DropdownMenuItem(
                                    text = { Text(semester ?: stringResource(R.string.common_all)) },
                                    onClick = {
                                        selectedSemester = semester
                                        semesterExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    var statusExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CompactCard(
                            modifier = Modifier.clickable { statusExpanded = true }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = statusLabel,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Rounded.ArrowDropDown,
                                    contentDescription = null
                                )
                            }
                        }
                        DropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false }
                        ) {
                            listOf(null, true, false).forEach { justified ->
                                val label = when (justified) {
                                    true -> stringResource(R.string.absences_justified)
                                    false -> stringResource(R.string.absences_unjustified)
                                    null -> stringResource(R.string.common_all)
                                }
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        selectedJustified = justified
                                        statusExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                }
            }
        }

        item {
            val unjustified = filteredAbsences.count { !it.justified }
            DataCard {
                Text(
                    text = pluralStringResource(R.plurals.absences_count, filteredAbsences.size, filteredAbsences.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pluralStringResource(R.plurals.absences_unjustified_count, unjustified, unjustified),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(filteredAbsences, key = { it.id }) { absence -> AbsenceCard(absence) }
    }
}

@Composable
fun CoursesScreen(viewModel: StudentViewModel) {
    val state by viewModel.courses.collectAsStateWithLifecycle()
    val coursesList = state.data.orEmpty()
    val years = remember(coursesList) {
        listOf(null) + coursesList.mapNotNull { it.year }.filter { it.isNotBlank() }.distinct().sortedDescending()
    }
    val periods = remember(coursesList) {
        listOf(null) + coursesList.mapNotNull { it.period }.filter { it.isNotBlank() }.distinct().sortedWith { p1, p2 -> comparePeriods(p2, p1) }
    }
    val defaultYear = remember(coursesList) {
        coursesList.mapNotNull { it.year }.filter { it.isNotBlank() }.distinct().sortedDescending().firstOrNull()
    }
    var selectedYear by remember(defaultYear) { mutableStateOf<String?>(defaultYear) }
    val defaultPeriod = remember(coursesList, selectedYear) {
        coursesList.filter { selectedYear == null || it.year == selectedYear }
            .mapNotNull { it.period }
            .filter { it.isNotBlank() }
            .distinct()
            .sortedWith { p1, p2 -> comparePeriods(p2, p1) }
            .firstOrNull()
    }
    var selectedPeriod by remember(defaultPeriod) { mutableStateOf<String?>(defaultPeriod) }

    val filteredCourses = remember(coursesList, selectedYear, selectedPeriod) {
        coursesList.filter { course ->
            (selectedYear == null || course.year == selectedYear) &&
            (selectedPeriod == null || course.period == selectedPeriod)
        }
    }

    FeatureStateContent(
        state = state,
        empty = List<Course>::isEmpty,
        emptyTitle = R.string.courses_empty_title,
        emptyBody = R.string.courses_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Documents) }
    ) { _ ->
        if (years.size > 1 || periods.size > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    
                    var yearExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        CompactCard(modifier = Modifier.clickable { yearExpanded = true }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedYear ?: stringResource(R.string.courses_filter_year_format, stringResource(R.string.common_all)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = yearExpanded,
                            onDismissRequest = { yearExpanded = false }
                        ) {
                            years.forEach { year ->
                                DropdownMenuItem(
                                    text = { Text(year ?: stringResource(R.string.common_all)) },
                                    onClick = {
                                        selectedYear = year
                                        yearExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    
                    var periodExpanded by remember { mutableStateOf(false) }
                    Box(modifier = Modifier.weight(1f)) {
                        CompactCard(modifier = Modifier.clickable { periodExpanded = true }) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = selectedPeriod ?: stringResource(R.string.courses_filter_period_format, stringResource(R.string.common_all)),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                            }
                        }
                        DropdownMenu(
                            expanded = periodExpanded,
                            onDismissRequest = { periodExpanded = false }
                        ) {
                            periods.forEach { period ->
                                DropdownMenuItem(
                                    text = { Text(period ?: stringResource(R.string.common_all)) },
                                    onClick = {
                                        selectedPeriod = period
                                        periodExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        items(filteredCourses, key = { it.id }) { course ->
            CourseCard(course = course, viewModel = viewModel)
        }
    }
}

@Composable
fun ProjectsScreen(
    viewModel: StudentViewModel,
    highlightedProjectId: String? = null
) {
    val state by viewModel.projects.collectAsStateWithLifecycle()
    val documentsState by viewModel.documents.collectAsStateWithLifecycle()
    val downloadingDocumentIds by viewModel.downloadingDocumentIds.collectAsStateWithLifecycle()
    val documentDownloadProgress by viewModel.documentDownloadProgress.collectAsStateWithLifecycle()
    val projectMessages by viewModel.projectMessages.collectAsStateWithLifecycle()
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var selectedYear by remember(state.data) { mutableStateOf<String?>(null) }

    LaunchedEffect(state.data, highlightedProjectId) {
        if (!highlightedProjectId.isNullOrBlank()) {
            val project = state.data.firstOrNull { it.id == highlightedProjectId }
            if (project != null) {
                selectedYear = project.year
                selectedProject = project
            }
        }
    }
    val years = remember(state.data) { state.data.mapNotNull { it.year }.distinct().sortedDescending() }
    val filteredProjects = remember(state.data, selectedYear) {
        state.data
            .filter { selectedYear == null || it.year == selectedYear }
            .sortedWith(compareByDescending<Project> { it.year }.thenByDescending { it.startsAt ?: it.deadline }.thenBy { it.name })
    }
    selectedProject?.let { selected ->
        val project = state.data.firstOrNull { it.id == selected.id } ?: selected
        ProjectDetailsDialog(
            project = project,
            documents = documentsState.data.filter { it.ownerId == project.id },
            downloadingDocumentIds = downloadingDocumentIds,
            documentDownloadProgress = documentDownloadProgress,
            onOpenDocument = viewModel::openDocument,
            onDownloadDocument = viewModel::downloadDocument,
            onDeposit = viewModel::requestDeposit,
            messageStates = projectMessages,
            onLoadMessages = viewModel::loadProjectMessages,
            onSendMessage = viewModel::sendProjectMessage,
            onJoinGroup = project.courseId?.let { rcId ->
                { groupId: String -> viewModel.joinGroup(rcId, project.id, groupId) }
            },
            onLeaveGroup = project.courseId?.let { rcId ->
                { groupId: String -> viewModel.leaveGroup(rcId, project.id, groupId) }
            },
            onDismiss = { selectedProject = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<Project>::isEmpty,
        emptyTitle = R.string.projects_empty_title,
        emptyBody = R.string.projects_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Projects) }
    ) {
        if (years.size > 1) {
            item {
                YearFilterRow(
                    years = years,
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it }
                )
            }
        }
        items(filteredProjects, key = { it.id }) { project ->
            ProjectCard(
                project = project,
                onOpen = { selectedProject = project }
            )
        }
    }
}

@Composable
fun PracticalsScreen(viewModel: StudentViewModel) {
    val state by viewModel.practicals.collectAsStateWithLifecycle()
    val documentsState by viewModel.documents.collectAsStateWithLifecycle()
    val downloadingDocumentIds by viewModel.downloadingDocumentIds.collectAsStateWithLifecycle()
    val documentDownloadProgress by viewModel.documentDownloadProgress.collectAsStateWithLifecycle()
    var selectedPractical by remember { mutableStateOf<Practical?>(null) }
    var selectedYear by remember(state.data) { mutableStateOf<String?>(null) }
    val years = remember(state.data) { state.data.mapNotNull { it.year }.distinct().sortedDescending() }
    val filteredPracticals = remember(state.data, selectedYear) {
        state.data
            .filter { selectedYear == null || it.year == selectedYear }
            .sortedWith(compareByDescending<Practical> { it.year }.thenByDescending { it.startsAt }.thenBy { it.name })
    }
    selectedPractical?.let { selected ->
        val practical = state.data.firstOrNull { it.id == selected.id } ?: selected
        PracticalDetailsDialog(
            practical = practical,
            documents = documentsState.data.filter { it.ownerId == practical.id },
            downloadingDocumentIds = downloadingDocumentIds,
            documentDownloadProgress = documentDownloadProgress,
            onOpenDocument = viewModel::openDocument,
            onDownloadDocument = viewModel::downloadDocument,
            onDeposit = viewModel::requestDeposit,
            onJoinGroup = practical.courseId?.let { rcId ->
                { groupId: String -> viewModel.joinGroup(rcId, practical.id, groupId) }
            },
            onLeaveGroup = practical.courseId?.let { rcId ->
                { groupId: String -> viewModel.leaveGroup(rcId, practical.id, groupId) }
            },
            onDismiss = { selectedPractical = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<Practical>::isEmpty,
        emptyTitle = R.string.practicals_empty_title,
        emptyBody = R.string.practicals_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Projects) }
    ) {
        if (years.size > 1) {
            item {
                YearFilterRow(
                    years = years,
                    selectedYear = selectedYear,
                    onYearSelected = { selectedYear = it }
                )
            }
        }
        items(filteredPracticals, key = { it.id }) { practical ->
            PracticalCard(
                practical = practical,
                onOpen = { selectedPractical = practical }
            )
        }
    }
}

@Composable
fun DirectoryScreen(viewModel: StudentViewModel) {
    val state by viewModel.directory.collectAsStateWithLifecycle()
    val years = remember(state.data) { state.data.mapNotNull { it.year }.distinct().sortedDescending() }
    var selectedYear by remember(state.data, years) { mutableStateOf(years.firstOrNull()) }
    var selectedRole by remember(state.data) { mutableStateOf<DirectoryRole?>(null) }
    val filteredPeople = remember(state.data, selectedYear, selectedRole) {
        state.data.filter { person ->
            (selectedYear == null || person.year == selectedYear) &&
                (selectedRole == null || person.role == selectedRole)
        }
    }
    FeatureStateContent(
        state = state,
        empty = List<DirectoryPerson>::isEmpty,
        emptyTitle = R.string.directory_empty_title,
        emptyBody = R.string.directory_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Directory) }
    ) {
        item {
            DirectoryFilterRow(
                years = years,
                selectedYear = selectedYear,
                selectedRole = selectedRole,
                onYearSelected = { selectedYear = it },
                onRoleSelected = { selectedRole = it }
            )
        }
        items(filteredPeople, key = { it.id }) { person -> DirectoryPersonCard(person) }
    }
}

@Composable
private fun YearFilterRow(
    years: List<String>,
    selectedYear: String?,
    onYearSelected: (String?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedYear == null,
            onClick = { onYearSelected(null) },
            label = { Text(stringResource(R.string.filter_all_years)) }
        )
        years.forEach { year ->
            FilterChip(
                selected = selectedYear == year,
                onClick = { onYearSelected(year) },
                label = { Text(year) }
            )
        }
    }
}

@Composable
private fun DirectoryFilterRow(
    years: List<String>,
    selectedYear: String?,
    selectedRole: DirectoryRole?,
    onYearSelected: (String?) -> Unit,
    onRoleSelected: (DirectoryRole?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedRole == null,
                onClick = { onRoleSelected(null) },
                label = { Text(stringResource(R.string.directory_all_roles)) }
            )
            FilterChip(
                selected = selectedRole == DirectoryRole.Student,
                onClick = { onRoleSelected(DirectoryRole.Student) },
                label = { Text(stringResource(R.string.directory_students)) }
            )
            FilterChip(
                selected = selectedRole == DirectoryRole.Teacher,
                onClick = { onRoleSelected(DirectoryRole.Teacher) },
                label = { Text(stringResource(R.string.directory_teachers)) }
            )
        }
        if (years.size > 1) {
            YearFilterRow(
                years = years,
                selectedYear = selectedYear,
                onYearSelected = onYearSelected
            )
        }
    }
}

@Composable
private fun DirectoryPersonCard(person: DirectoryPerson) {
    CompactCard {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(
                avatarUrl = person.avatarUrl,
                displayName = person.displayName,
                modifier = Modifier.size(48.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = person.displayName.orUntitled(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(
                        if (person.role == DirectoryRole.Teacher) R.string.directory_teacher
                        else R.string.directory_student
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        LabelValue(R.string.profile_email, person.email.orEmpty())
        LabelValue(R.string.profile_year, person.year.orEmpty())
        LabelValue(R.string.projects_group, person.groupName.orEmpty())
    }
}

@Composable
fun DocumentsScreen(
    viewModel: StudentViewModel,
    highlightedDocumentId: String? = null
) {
    val state by viewModel.documents.collectAsStateWithLifecycle()
    val downloadingDocumentIds by viewModel.downloadingDocumentIds.collectAsStateWithLifecycle()
    val documentDownloadProgress by viewModel.documentDownloadProgress.collectAsStateWithLifecycle()
    val documentError by viewModel.documentError.collectAsStateWithLifecycle()
    var documentOpenTriggered by remember(highlightedDocumentId) { mutableStateOf(false) }

    val generalDocuments = remember(state.data) {
        state.data.filter { it.ownerId == null }
    }
    val years = remember(generalDocuments) {
        generalDocuments.mapNotNull { it.filterYear() }.distinct().sortedDescending()
    }
    var selectedYear by remember(years) { mutableStateOf(years.firstOrNull()) }
    val filteredGeneral = remember(generalDocuments, selectedYear) {
        if (selectedYear == null) generalDocuments else generalDocuments.filter { it.filterYear() == selectedYear }
    }
    val annualByCategory = remember(filteredGeneral) {
        filteredGeneral
            .filter { it.category != null }
            .groupBy { it.category?.trim()?.uppercase() ?: "" }
            .entries
            .sortedBy { it.key }
    }
    val folderDocuments = remember(filteredGeneral) {
        filteredGeneral.filter { it.category == null }
    }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val showScrollTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 5 } }

    LaunchedEffect(state.data, highlightedDocumentId) {
        if (!documentOpenTriggered && !highlightedDocumentId.isNullOrBlank()) {
            val document = state.data.firstOrNull { it.id == highlightedDocumentId }
            if (document != null) {
                documentOpenTriggered = true
                viewModel.openDocument(document)
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
    FeatureStateContent(
        state = state,
        empty = { allDocs -> allDocs.none { it.ownerId == null } },
        emptyTitle = R.string.documents_empty_title,
        emptyBody = R.string.documents_empty_body,
        onRetry = { viewModel.refresh(SyncFeature.Documents) },
        listState = listState
    ) { _ ->
        documentError?.let { item { StateBanner(it) } }
        if (years.isNotEmpty()) {
            item {
                var yearExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    CompactCard(modifier = Modifier.clickable { yearExpanded = true }) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedYear ?: stringResource(R.string.common_all),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(Icons.Rounded.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        (listOf(null) + years).forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year ?: stringResource(R.string.common_all)) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
        if (annualByCategory.isNotEmpty()) {
            item { SectionTitleText(stringResource(R.string.documents_annual)) }
        }
        annualByCategory.forEach { (category, docs) ->
            item(key = "cat_$category") {
                Text(
                    text = category.ifBlank { stringResource(R.string.common_untitled) },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
            }
            items(docs, key = { it.id }) { document ->
                DocumentCard(
                    document = document,
                    downloading = document.id in downloadingDocumentIds,
                    progress = documentDownloadProgress[document.id],
                    showCategory = false,
                    onDownload = { viewModel.downloadDocument(document) },
                    onOpen = { viewModel.openDocument(document) }
                )
            }
        }
        if (folderDocuments.isNotEmpty()) {
            item { SectionTitleText(stringResource(R.string.documents_folder)) }
            items(folderDocuments, key = { it.id }) { document ->
                DocumentCard(
                    document = document,
                    downloading = document.id in downloadingDocumentIds,
                    progress = documentDownloadProgress[document.id],
                    onDownload = { viewModel.downloadDocument(document) },
                    onOpen = { viewModel.openDocument(document) }
                )
            }
        }
    }
        if (showScrollTop) {
            FloatingActionButton(
                onClick = { scope.launch { listState.animateScrollToItem(0) } },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.action_scroll_top)
                )
            }
        }
    }
}

private fun AcademicDocument.filterYear(): String? {
    year?.takeIf { it.isNotBlank() }?.let { raw ->
        
        ACADEMIC_YEAR_REGEX.find(raw)?.value?.replace(" ", "")?.let { return it }
        raw.trim().toIntOrNull()?.let { return "$it-${it + 1}" }
        return raw
    }
    val instant = updatedAt ?: return null
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val start = if (date.monthValue >= 8) date.year else date.year - 1
    return "$start-${start + 1}"
}

@Composable
fun NotificationsScreen(
    studentViewModel: StudentViewModel
) {
    val newsState by studentViewModel.news.collectAsStateWithLifecycle()
    var selectedNews by remember { mutableStateOf<NewsItem?>(null) }

    selectedNews?.let { item ->
        NewsDetailScreen(item, onBack = { selectedNews = null })
        return
    }

    FeatureStateContent(
        state = newsState,
        empty = List<NewsItem>::isEmpty,
        emptyTitle = R.string.notifications_empty_title,
        emptyBody = R.string.notifications_empty_body,
        onRetry = studentViewModel::refresh
    ) { news ->
        items(news, key = { it.id }) { item ->
            NewsCard(item, onOpen = { selectedNews = item })
        }
    }
}

@Composable
fun EventsScreen(
    studentViewModel: StudentViewModel
) {
    val state by studentViewModel.events.collectAsStateWithLifecycle()
    var selectedEvent by remember { mutableStateOf<StudentEvent?>(null) }
    selectedEvent?.let { event ->
        EventDetailsDialog(
            event = event,
            onDismiss = { selectedEvent = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<StudentEvent>::isEmpty,
        emptyTitle = R.string.events_empty_title,
        emptyBody = R.string.events_empty_body,
        onRetry = studentViewModel::refresh
    ) { events ->
        val now = Instant.now()
        val (future, past) = events.partition { (it.date ?: Instant.MIN).isAfter(now) }
        if (future.isNotEmpty()) {
            item { SectionTitleText(stringResource(R.string.events_section_future)) }
            items(future, key = { "f:${it.id}" }) { EventCard(it, onOpen = { selectedEvent = it }) }
        }
        if (past.isNotEmpty()) {
            item { SectionTitleText(stringResource(R.string.events_section_past)) }
            items(past, key = { "p:${it.id}" }) { EventCard(it, onOpen = { selectedEvent = it }) }
        }
    }
}

@Composable
private fun EventCard(event: StudentEvent, onOpen: () -> Unit) {
    val hasDescription = !event.description.isNullOrBlank()
    CompactCard(
        modifier = Modifier.clickable { onOpen() }
    ) {
        Text(
            text = event.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.common_date, formatInstant(event.date))
        LabelValue(R.string.events_location, event.location.orEmpty())
        LabelValue(R.string.events_organizer, event.organizer.orEmpty())
        if (event.subscriptionStart != null && event.subscriptionEnd != null) {
            LabelValue(
                R.string.events_subscription_window,
                stringResource(
                    R.string.events_subscription_window_value,
                    formatInstant(event.subscriptionStart),
                    formatInstant(event.subscriptionEnd)
                )
            )
        }
        Text(
            text = stringResource(
                if (event.subscribed) R.string.events_subscribed else R.string.events_not_subscribed
            ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (event.subscribed) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (hasDescription) {
            Text(
                text = event.description!!,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EventDetailsDialog(
    event: StudentEvent,
    onDismiss: () -> Unit
) {
    val now = remember { Instant.now() }
    val canSubscribe = event.subscriptionStart?.let { !now.isBefore(it) } == true &&
        event.subscriptionEnd?.let { !now.isAfter(it) } == true
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(event.title.orUntitled()) },
        text = {
            Text(
                text = event.description.orEmpty(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        confirmButton = {
            if (canSubscribe) {
                Button(onClick = {}) {
                    Text(stringResource(if (event.subscribed) R.string.events_leave else R.string.events_join))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    studentViewModel: StudentViewModel
) {
    val state by settingsViewModel.state.collectAsStateWithLifecycle()
    val agendaState by studentViewModel.agenda.collectAsStateWithLifecycle()
    val calendars by settingsViewModel.calendars.collectAsStateWithLifecycle()
    val selectedCalendarId by settingsViewModel.selectedCalendarId.collectAsStateWithLifecycle()
    val settings = state.settings
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var showCalendarPicker by remember { mutableStateOf(false) }
    var pendingFirstPick by remember { mutableStateOf(false) }
    val enableCalendarSync = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        settingsViewModel.setCalendarSync(true)
        pendingFirstPick = true
        settingsViewModel.loadCalendars()
        if (agendaState.data.isNotEmpty()) studentViewModel.syncAgendaToCalendar(agendaState.data)
    }
    val calendarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) enableCalendarSync()
    }
    LaunchedEffect(calendars, pendingFirstPick) {
        if (pendingFirstPick && calendars.isNotEmpty()) {
            if (selectedCalendarId == null) showCalendarPicker = true
            pendingFirstPick = false
        }
    }
    if (settings == null) {
        LoadingState()
        return
    }
    LaunchedEffect(settings.calendarSyncEnabled) {
        if (settings.calendarSyncEnabled && context.hasCalendarPermissions()) {
            settingsViewModel.loadCalendars()
        }
    }
    if (showCalendarPicker) {
        CalendarAccountPickerDialog(
            calendars = calendars,
            selectedCalendarId = selectedCalendarId,
            onSelect = { id ->
                settingsViewModel.selectCalendar(id)
                showCalendarPicker = false
                if (agendaState.data.isNotEmpty()) {
                    studentViewModel.syncAgendaToCalendar(agendaState.data)
                }
            },
            onDismiss = { showCalendarPicker = false }
        )
    }
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val currentError = state.error
        if (currentError != null) {
            item { StateBanner(currentError) }
        }
        if (state.loading) {
            item { RefreshingRow() }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_language),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LanguageSelector(settings.languageTag, settingsViewModel::setLanguage)
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_theme),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                ThemeSelector(settings.themeMode, settingsViewModel::setThemeMode)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    SwitchRow(
                        title = R.string.settings_dynamic_color,
                        checked = settings.dynamicColorEnabled,
                        onCheckedChange = settingsViewModel::setDynamicColor
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.settings_agenda_color_mode),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AgendaColorModeSelector(settings.agendaColorMode, settingsViewModel::setAgendaColorMode)
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val notificationLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) {
                    OutlinedButton(onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                        Icon(Icons.Rounded.Notifications, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.notifications_grant_permission))
                    }
                }
                NotificationPreferences(settings, settingsViewModel)
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_sync_intervals),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                SyncIntervalPreferences(settings, settingsViewModel)
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_session),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                SwitchRow(
                    title = R.string.settings_calendar_sync,
                    checked = settings.calendarSyncEnabled,
                    onCheckedChange = { enabled ->
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (!enabled) {
                            settingsViewModel.setCalendarSync(false)
                        } else if (context.hasCalendarPermissions()) {
                            enableCalendarSync()
                        } else {
                            calendarLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                        }
                    }
                )
                if (settings.calendarSyncEnabled) {
                    val selectedName = calendars.firstOrNull { it.id == selectedCalendarId }
                        ?.let { it.accountName.ifBlank { it.displayName } }
                    LabelValue(
                        R.string.settings_calendar_account,
                        selectedName ?: stringResource(R.string.settings_calendar_account_none)
                    )
                    OutlinedButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (context.hasCalendarPermissions()) {
                                settingsViewModel.loadCalendars()
                                showCalendarPicker = true
                            } else {
                                calendarLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.settings_calendar_account_choose))
                    }
                }
                settings.lastSyncAt?.let {
                    LabelValue(R.string.settings_last_sync, formatInstant(it))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        studentViewModel.refresh()
                    }
                ) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_sync_now))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        settingsViewModel.clearCache()
                    }
                ) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_clear_cache))
                }
                Button(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        settingsViewModel.logout()
                    }
                ) {
                    Icon(Icons.Rounded.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_logout))
                }
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_about),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.about_version, BuildConfig.VERSION_NAME),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.about_unofficial_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_POLICY_URL)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Shield, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.about_privacy_policy))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(LICENSES_URL)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Description, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.about_licenses))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(SOURCE_CODE_URL)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Code, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.about_source_code))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val packageName = context.packageName
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=$packageName")
                        ).apply { setPackage("com.android.vending") }
                        try {
                            context.startActivity(intent)
                        } catch (e: android.content.ActivityNotFoundException) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                                )
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Star, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_rate_play_store))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ISSUES_BUG_URL)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.BugReport, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.about_report_bug))
                }
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        runCatching {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_ISSUES_FEATURE_URL)))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Rounded.Lightbulb, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.about_suggest_improvement))
                }
            }
        }
    }
}

@Composable
private fun AgendaModeSelector(
    selectedMode: AgendaMode,
    onModeSelected: (AgendaMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AgendaMode.values().forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                label = { Text(stringResource(mode.title)) }
            )
        }
    }
}

@Composable
private fun NotificationPreferences(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = stringResource(R.string.settings_notify_caption),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SwitchRow(R.string.settings_notify_grades, settings.notifications.grades, settingsViewModel::setGradeNotifications)
        SwitchRow(R.string.settings_notify_absences, settings.notifications.absences, settingsViewModel::setAbsenceNotifications)
        SwitchRow(R.string.settings_notify_agenda, settings.notifications.agenda, settingsViewModel::setAgendaNotifications)
        SwitchRow(R.string.settings_notify_projects, settings.notifications.projects, settingsViewModel::setProjectNotifications)
        SwitchRow(R.string.settings_notify_documents, settings.notifications.documents, settingsViewModel::setDocumentNotifications)

        Spacer(Modifier.height(4.dp))
        ReminderLeadSelector(
            title = R.string.settings_reminder_class_title,
            description = R.string.settings_reminder_class_desc,
            selectedMinutes = settings.classReminderLeadMinutes,
            onSelect = settingsViewModel::setClassReminderLead
        )
        ReminderLeadSelector(
            title = R.string.settings_reminder_deadline_title,
            description = R.string.settings_reminder_deadline_desc,
            selectedMinutes = settings.deadlineReminderLeadMinutes,
            onSelect = settingsViewModel::setDeadlineReminderLead
        )
    }
}

@Composable
private fun ReminderLeadSelector(
    @StringRes title: Int,
    @StringRes description: Int,
    selectedMinutes: Int,
    onSelect: (Int) -> Unit
) {
    Text(
        text = stringResource(title),
        style = MaterialTheme.typography.bodyLarge
    )
    Text(
        text = stringResource(description),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        REMINDER_LEAD_CHOICES.forEach { minutes ->
            FilterChip(
                selected = selectedMinutes == minutes,
                onClick = { onSelect(minutes) },
                label = { Text(reminderLeadLabel(minutes)) }
            )
        }
    }
}

@Composable
private fun SyncFeature.displayName(): String {
    return stringResource(
        when (this) {
            SyncFeature.Agenda -> R.string.settings_sync_interval_feature_agenda
            SyncFeature.Grades -> R.string.settings_sync_interval_feature_grades
            SyncFeature.Absences -> R.string.settings_sync_interval_feature_absences
            SyncFeature.Projects -> R.string.settings_sync_interval_feature_projects
            SyncFeature.Documents -> R.string.settings_sync_interval_feature_documents
            SyncFeature.Directory -> R.string.settings_sync_interval_feature_directory
            SyncFeature.News -> R.string.settings_sync_interval_feature_news
            SyncFeature.Events -> R.string.settings_sync_interval_feature_events
        }
    )
}

@Composable
private fun formatDuration(minutes: Int): String {
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h == 0 -> stringResource(R.string.duration_minutes, m)
        m == 0 -> stringResource(R.string.duration_hours, h)
        else -> stringResource(R.string.duration_hours_minutes, h, m)
    }
}

@Composable
private fun SyncIntervalPreferences(
    settings: UserSettings,
    settingsViewModel: SettingsViewModel
) {
    val haptic = LocalHapticFeedback.current
    var activeFeatureForDialog by remember { mutableStateOf<SyncFeature?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        SyncFeature.entries.forEach { feature ->
            val currentMinutes = settings.refreshIntervals.minutesFor(feature)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        activeFeatureForDialog = feature
                    }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = feature.displayName(),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = formatDuration(currentMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    activeFeatureForDialog?.let { feature ->
        SyncIntervalPickerDialog(
            feature = feature,
            currentMinutes = settings.refreshIntervals.minutesFor(feature),
            onConfirm = { minutes ->
                settingsViewModel.setRefreshInterval(feature, minutes)
                activeFeatureForDialog = null
            },
            onDismiss = { activeFeatureForDialog = null }
        )
    }
}

@Composable
internal fun SyncIntervalPickerDialog(
    feature: SyncFeature,
    currentMinutes: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var sliderValue by remember { mutableStateOf(currentMinutes.toFloat()) }
    val snappedMinutes = (sliderValue.roundToInt() / 15) * 15

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.settings_sync_interval_title, feature.displayName()))
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = formatDuration(snappedMinutes),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Slider(
                    value = sliderValue,
                    onValueChange = { sliderValue = it },
                    valueRange = 15f..1440f,
                    steps = 95,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(snappedMinutes) }
            ) {
                Text(stringResource(R.string.action_confirm))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
internal fun LanguageSelector(
    selectedLanguageTag: String?,
    onLanguageSelected: (String?) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LanguageOption.entries.forEach { option ->
            FilterChip(
                selected = selectedLanguageTag == option.languageTag,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLanguageSelected(option.languageTag)
                },
                label = { Text(stringResource(option.title)) }
            )
        }
    }
}

@Composable
internal fun CalendarAccountPickerDialog(
    calendars: List<CalendarAccount>,
    selectedCalendarId: Long?,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        },
        title = { Text(stringResource(R.string.settings_calendar_account)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (calendars.isEmpty()) {
                    Text(stringResource(R.string.settings_calendar_account_empty))
                }
                calendars.forEach { calendar ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(calendar.id) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (calendar.id == selectedCalendarId) {
                            Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        } else {
                            Spacer(Modifier.width(24.dp))
                        }
                        Column {
                            Text(calendar.displayName, style = MaterialTheme.typography.bodyLarge)
                            if (calendar.accountName.isNotBlank() && calendar.accountName != calendar.displayName) {
                                Text(
                                    calendar.accountName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
internal fun ThemeSelector(
    selectedMode: ThemeMode,
    onModeSelected: (ThemeMode) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onModeSelected(mode)
                },
                label = {
                    Text(
                        stringResource(
                            when (mode) {
                                ThemeMode.System -> R.string.settings_theme_system
                                ThemeMode.Light -> R.string.settings_theme_light
                                ThemeMode.Dark -> R.string.settings_theme_dark
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
private fun AgendaColorModeSelector(
    selectedMode: AgendaColorMode,
    onModeSelected: (AgendaColorMode) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AgendaColorMode.entries.forEach { mode ->
            FilterChip(
                selected = selectedMode == mode,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onModeSelected(mode)
                },
                label = {
                    Text(
                        stringResource(
                            when (mode) {
                                AgendaColorMode.Course -> R.string.settings_agenda_color_course
                                AgendaColorMode.Location -> R.string.settings_agenda_color_location
                            }
                        )
                    )
                }
            )
        }
    }
}

@Composable
internal fun SwitchRow(
    @StringRes title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onCheckedChange(it)
                }
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = null)
    }
}

@Composable
private fun reminderLeadLabel(minutes: Int): String =
    if (minutes <= 0) stringResource(R.string.settings_reminder_lead_off) else formatDuration(minutes)

@Composable
fun StudentAvatar(
    avatarUrl: String?,
    displayName: String,
    modifier: Modifier = Modifier
) {
    if (avatarUrl.isNullOrBlank()) {
        Box(modifier = modifier)
        return
    }
    AsyncImage(
        model = avatarUrl,
        contentDescription = stringResource(R.string.profile_avatar_cd, displayName.orUntitled()),
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(CircleShape)
    )
}

@Composable
internal fun AgendaEventCard(
    event: AgendaEvent,
    onOpen: (() -> Unit)? = null
) {
    CompactCard(
        modifier = if (onOpen == null) Modifier else Modifier.clickable(onClick = onOpen)
    ) {
        Text(
            text = event.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(formatInstant(event.startsAt))
        LabelValue(R.string.agenda_end, formatInstant(event.endsAt))
        LabelValue(R.string.agenda_room, event.room.orEmpty())
        LabelValue(R.string.agenda_teacher, event.teacher.orEmpty())
        LabelValue(R.string.agenda_modality, event.modality.orEmpty())
    }
}

@Composable
internal fun AgendaEventDetailsDialog(
    event: AgendaEvent,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.agenda_details_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = event.title.orUntitled(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LabelValue(R.string.practicals_start, formatInstant(event.startsAt))
                LabelValue(R.string.agenda_end, formatInstant(event.endsAt))
                val context = LocalContext.current
                LabelValue(
                    label = R.string.agenda_room,
                    value = event.room.orEmpty()
                )
                LabelValue(
                    label = R.string.agenda_address,
                    value = event.address.orEmpty(),
                    onClick = if (event.address.isNullOrBlank()) null else {
                        {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(event.address)}"))
                            runCatching { context.startActivity(intent) }
                        }
                    }
                )
                LabelValue(R.string.agenda_teacher, event.teacher.orEmpty())
                LabelValue(R.string.agenda_modality, event.modality.orEmpty())
                LabelValue(R.string.agenda_type, event.type.orEmpty())
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun GradeCard(
    grade: Grade,
    onOpen: () -> Unit
) {
    CompactCard(modifier = Modifier.clickable(onClick = onOpen)) {
        val title = grade.courseName.ifBlank { grade.subject }
        Text(
            text = title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (grade.subject.isNotBlank() && grade.courseName.isNotBlank()) {
            Text(grade.subject)
        }
        if (grade.value != null) {
            Text(
                text = stringResource(
                    R.string.grades_value_format,
                    formatNumber(grade.value),
                    formatNumber(grade.scale)
                ),
                style = MaterialTheme.typography.titleLarge,
                color = gradeColor(grade.value, grade.scale)
            )
        } else {
            Text(
                text = stringResource(R.string.grades_no_grade),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LabelValue(R.string.grades_coefficient, if (grade.isNotCounted()) stringResource(R.string.grades_not_counted) else formatNumber(grade.coefficient))
        LabelValue(R.string.common_date, formatDate(grade.date))
        if (!grade.period.isNullOrBlank()) {
            LabelValue(R.string.common_period, grade.period)
        }
    }
}

@Composable
private fun GradeGroupHeader(title: String, grades: List<Grade>) {
    val avg = remember(grades) { grades.toGradeSummary().weightedAverage }
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        if (avg != null) {
            Text(
                text = stringResource(R.string.grades_value_format, formatNumber(avg), formatNumber(20.0)),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = gradeColor(avg, 20.0)
            )
        }
    }
}

@Composable
internal fun GradeSummaryCard(
    grades: List<Grade>,
    allGrades: List<Grade> = grades
) {
    val summary = remember(grades) { grades.toGradeSummary() }
    val ccAverage = remember(allGrades) {
        allGrades.filter { it.id.contains("-cc-") && !it.isExcludedFromAverage() }.mapNotNull { it.value }.takeIf { it.isNotEmpty() }?.average()
    }
    val examAverage = remember(allGrades) {
        allGrades.filter { it.id.contains("-exam") && !it.isExcludedFromAverage() }.mapNotNull { it.value }.takeIf { it.isNotEmpty() }?.average()
    }
    DataCard {
        Text(
            text = stringResource(R.string.grades_summary_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (summary.gradedCount == 0) {
            Text(
                text = stringResource(R.string.grades_no_grade),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            ColoredLabelValue(R.string.grades_weighted_average, formatNumber(summary.weightedAverage), gradeColor(summary.weightedAverage, 20.0))
            ColoredLabelValue(R.string.grades_cc_average, ccAverage?.let { formatNumber(it) } ?: stringResource(R.string.grades_no_grade), gradeColor(ccAverage, 20.0))
            ColoredLabelValue(R.string.grades_exam_title, examAverage?.let { formatNumber(it) } ?: stringResource(R.string.grades_no_grade), gradeColor(examAverage, 20.0))
            LabelValue(R.string.grades_gpa, formatNumber(summary.gpa))
        }
        LabelValue(R.string.grades_count, summary.gradedCount.toString())
        if (summary.incomplete) {
            Text(
                text = stringResource(R.string.grades_summary_incomplete),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GradeBlockAveragesCard(
    grades: List<Grade>,
    blockAssignments: Map<String, String>
) {
    val blockSummaries = remember(grades, blockAssignments) {
        grades.groupBy { blockAssignments[it.blockKey()]?.takeIf(String::isNotBlank) }
            .filterKeys { it != null }
            .mapKeys { it.key.orEmpty() }
            .toSortedMap(compareBy<String> { it.toIntOrNull() ?: Int.MAX_VALUE }.thenBy { it })
            .mapValues { (_, blockGrades) -> blockGrades.toGradeSummary() }
    }
    if (blockSummaries.isEmpty()) return
    DataCard {
        Text(
            text = stringResource(R.string.grades_block_average),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        blockSummaries.forEach { (block, summary) ->
            ColoredLabelValue(
                label = R.string.grades_block,
                value = "$block : ${summary.weightedAverage?.let { formatNumber(it) } ?: stringResource(R.string.grades_no_grade)}",
                color = gradeColor(summary.weightedAverage, 20.0)
            )
        }
    }
}

@Composable
private fun GradeSimulationEditCard(
    grade: Grade,
    resetVersion: Int,
    block: String,
    onValueChange: (Double?) -> Unit,
    onBlockChange: (String) -> Unit,
    onOpen: () -> Unit
) {
    val initialValueText = formatNumber(grade.value)
    var valueText by remember(grade.id, resetVersion) { mutableStateOf(initialValueText) }
    var blockText by remember(grade.blockKey(), resetVersion) { mutableStateOf(block) }
    CompactCard {
        Text(
            text = grade.courseName.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.clickable(onClick = onOpen)
        )
        if (grade.subject.isNotBlank()) {
            Text(grade.subject)
        }
        OutlinedTextField(
            value = valueText,
            onValueChange = { value ->
                valueText = value
                onValueChange(value.toGradeNumber())
            },
            label = { Text(stringResource(R.string.grades_simulation_value)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        if (!grade.id.contains("-cc-") && !grade.id.contains("-exam")) {
            OutlinedTextField(
                value = blockText,
                onValueChange = { value ->
                    blockText = value
                    onBlockChange(value.trim())
                },
                label = { Text(stringResource(R.string.grades_block)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        LabelValue(R.string.grades_coefficient, if (grade.isNotCounted()) stringResource(R.string.grades_not_counted) else formatNumber(grade.coefficient))
        if (!grade.period.isNullOrBlank()) {
            LabelValue(R.string.common_period, grade.period)
        }
    }
}

@Composable
private fun GradeValueEditor(
    grade: Grade,
    resetVersion: Int,
    onValueChange: (Double?) -> Unit
) {
    val initialValueText = formatNumber(grade.value)
    var valueText by remember(grade.id, resetVersion) { mutableStateOf(initialValueText) }
    OutlinedTextField(
        value = valueText,
        onValueChange = { value ->
            valueText = value
            onValueChange(value.toGradeNumber())
        },
        label = { Text(grade.subject.ifBlank { stringResource(R.string.grades_general_average) }) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun GradeDetailsDialog(
    grade: Grade,
    components: List<Grade>,
    block: String,
    simulationMode: Boolean,
    resetVersion: Int,
    onValueChange: (String, Double?) -> Unit,
    onBlockChange: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val isComponent = grade.id.contains("-cc-") || grade.id.contains("-exam")
    val ccComponents = if (isComponent) emptyList() else components.filter { it.id.contains("-cc-") }
    val examComponent = if (isComponent) null else components.firstOrNull { it.id.contains("-exam") }
    val editableGrades = if (ccComponents.isEmpty() && examComponent == null) listOf(grade) else ccComponents + listOfNotNull(examComponent)
    
    val ccValues = ccComponents.mapNotNull { it.value }
    val ccAverage = if (ccValues.isNotEmpty()) ccValues.average() else null
    var blockText by remember(grade.blockKey(), resetVersion) { mutableStateOf(block) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.grades_detail_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = grade.courseName.orUntitled(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LabelValue(R.string.common_period, grade.period.orEmpty())
                if (!isComponent) {
                    OutlinedTextField(
                        value = blockText,
                        onValueChange = { value ->
                            blockText = value
                            onBlockChange(value.trim())
                        },
                        label = { Text(stringResource(R.string.grades_block)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                
                if (simulationMode) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    editableGrades.forEach { editableGrade ->
                        GradeValueEditor(
                            grade = editableGrade,
                            resetVersion = resetVersion,
                            onValueChange = { value -> onValueChange(editableGrade.id, value) }
                        )
                    }
                } else if (ccComponents.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.grades_cc_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    ccComponents.forEach { component ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = component.subject.orUntitled(),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val valText = if (component.value != null) {
                                stringResource(R.string.grades_value_format, formatNumber(component.value), formatNumber(component.scale))
                            } else {
                                stringResource(R.string.grades_no_grade)
                            }
                            Text(
                                text = valText,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    ccAverage?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        LabelValue(R.string.grades_cc_average, stringResource(R.string.grades_value_format, formatNumber(it), "20"))
                    }
                }
                
                if (!simulationMode) examComponent?.let { exam ->
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.grades_exam_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = exam.subject.orUntitled(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        val valText = if (exam.value != null) {
                            stringResource(R.string.grades_value_format, formatNumber(exam.value), formatNumber(exam.scale))
                        } else {
                            stringResource(R.string.grades_no_grade)
                        }
                        Text(
                            text = valText,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                LabelValue(
                    R.string.grades_general_average,
                    if (grade.value != null) stringResource(R.string.grades_value_format, formatNumber(grade.value), formatNumber(grade.scale)) else stringResource(R.string.grades_no_grade)
                )
                LabelValue(R.string.grades_coefficient, if (grade.isNotCounted()) stringResource(R.string.grades_not_counted) else formatNumber(grade.coefficient))
                LabelValue(R.string.common_date, formatDate(grade.date))
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
internal fun AbsenceCard(
    absence: Absence,
    onOpen: (() -> Unit)? = null
) {
    CompactCard(
        modifier = if (onOpen == null) Modifier else Modifier.clickable(onClick = onOpen)
    ) {
        Text(
            text = absence.courseName.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(formatInstant(absence.startsAt))
        LabelValue(R.string.absences_end, formatInstant(absence.endsAt))
        LabelValue(
            R.string.absences_status,
            if (absence.justified) stringResource(R.string.absences_justified) else stringResource(R.string.absences_unjustified)
        )
        if (!absence.period.isNullOrBlank()) {
            LabelValue(R.string.common_period, absence.period)
        }
    }
}

@Composable
internal fun CourseCard(
    course: Course,
    viewModel: StudentViewModel
) {
    var showFilesDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val documentsState by viewModel.documents.collectAsStateWithLifecycle()
    val courseFiles = remember(documentsState.data, course.id) {
        documentsState.data.orEmpty().filter {
            it.downloadUrl?.contains("me/${course.id}/files/") == true ||
            it.downloadUrl?.contains("courseId=${course.id}") == true
        }
    }
    val effectiveFileCount = maxOf(course.fileCount, courseFiles.size)

    if (showFilesDialog) {
        CourseFilesDialog(
            files = courseFiles,
            viewModel = viewModel,
            onDismiss = { showFilesDialog = false }
        )
    }

    CompactCard {
        Text(
            text = course.name.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.courses_teacher, course.teacher.orEmpty())
        LabelValue(R.string.profile_year, course.year.orEmpty())
        LabelValue(R.string.common_period, course.period.orEmpty())
        LabelValue(R.string.courses_files, effectiveFileCount.toString())
        course.location?.let { location ->
            if (location.isNotBlank()) {
                val context = LocalContext.current
                Row(
                    modifier = Modifier
                        .clickable {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(location)}"))
                            runCatching { context.startActivity(intent) }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = location,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }
        }
        if (!course.syllabus.isNullOrBlank() || effectiveFileCount > 0) {
            HorizontalDivider()
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (!course.syllabus.isNullOrBlank()) {
                    val savedMsg = stringResource(R.string.courses_syllabus_saved)
                    val failedMsg = stringResource(R.string.courses_syllabus_save_failed)
                    val saveSyllabus: () -> Unit = {
                        val uri = PdfGenerator.savePdfToDownloads(
                            context = context,
                            text = course.syllabus,
                            title = "${course.name} - Syllabus",
                            fileName = "${course.name}_syllabus"
                        )
                        if (uri != null) {
                            Toast.makeText(context, savedMsg, Toast.LENGTH_SHORT).show()
                            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                                setDataAndType(uri, "application/pdf")
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            runCatching { context.startActivity(viewIntent) }
                        } else {
                            Toast.makeText(context, failedMsg, Toast.LENGTH_SHORT).show()
                        }
                    }
                    val storagePermissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { granted ->
                        if (granted) saveSyllabus() else Toast.makeText(context, failedMsg, Toast.LENGTH_SHORT).show()
                    }
                    TextButton(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                saveSyllabus()
                            } else {
                                storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.courses_download_syllabus))
                    }
                }
                
                if (effectiveFileCount > 0) {
                    TextButton(
                        onClick = { showFilesDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(pluralStringResource(R.plurals.courses_files_button, effectiveFileCount, effectiveFileCount))
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseFilesDialog(
    files: List<AcademicDocument>,
    viewModel: StudentViewModel,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.courses_dialog_title)) },
        text = {
            if (files.isEmpty()) {
                Text(stringResource(R.string.courses_no_files))
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(files, key = { it.id }) { file ->
                        Card(
                            onClick = {
                                viewModel.openDocument(file)
                                onDismiss()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = file.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = file.fileName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Rounded.Download,
                                    contentDescription = stringResource(R.string.courses_download_file)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun StepItem(step: ProjectStep) {
    var expanded by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = step.title.orUntitled(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = if (expanded) Int.MAX_VALUE else 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        if (expanded) {
            Spacer(modifier = Modifier.height(4.dp))
            LabelValue(R.string.projects_deadline, formatInstant(step.deadline))
            LabelValue(R.string.projects_status, step.status.orEmpty())
        }
    }
}

@Composable
internal fun ProjectCard(
    project: Project,
    onOpen: () -> Unit = {}
) {
    CompactCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Text(
            text = project.name.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                LabelValue(R.string.projects_course, project.courseName.orEmpty())
            }
            Column(modifier = Modifier.weight(1f)) {
                LabelValue(R.string.projects_status, project.status.orEmpty())
                LabelValue(R.string.projects_deadline, formatInstant(project.deadline))
            }
        }
        LabelValue(R.string.projects_files, project.fileCount.toString())
        Spacer(Modifier.height(4.dp))
        ProjectProgress(project)
        project.steps.forEach { step ->
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            StepItem(step)
        }
    }
}

@Composable
private fun ProjectProgress(project: Project) {
    val progress = remember(project) { project.progress() }
    Text(
        text = stringResource(R.string.projects_progress),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LinearProgressIndicator(
        progress = { progress.fraction.toFloat() },
        modifier = Modifier.fillMaxWidth().clip(CircleShape)
    )
    Text(
        text = if (progress.totalSteps == 0) {
            stringResource(R.string.projects_no_steps)
        } else {
            stringResource(R.string.projects_steps_completed, progress.completedSteps, progress.totalSteps)
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ProjectDetailsDialog(
    project: Project,
    documents: List<AcademicDocument>,
    downloadingDocumentIds: Set<String>,
    documentDownloadProgress: Map<String, Float?>,
    onOpenDocument: (AcademicDocument) -> Unit,
    onDownloadDocument: (AcademicDocument) -> Unit,
    onDeposit: (String) -> Unit,
    messageStates: Map<String, FeatureUiState<List<ProjectMessage>>>,
    onLoadMessages: (String) -> Unit,
    onSendMessage: (String, String) -> Unit,
    onJoinGroup: ((String) -> Unit)? = null,
    onLeaveGroup: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    var selectedStepId by remember(project.id) { mutableStateOf(project.steps.firstOrNull()?.id) }
    var chatGroup by remember(project.id) { mutableStateOf<ProjectGroup?>(null) }
    val selectedStep = project.steps.firstOrNull { it.id == selectedStepId }
    chatGroup?.let { group ->
        ProjectChatDialog(
            group = group,
            state = messageStates[group.id] ?: FeatureUiState(emptyList()),
            onLoad = { onLoadMessages(group.id) },
            onSend = { message -> onSendMessage(group.id, message) },
            onDismiss = { chatGroup = null }
        )
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_detail_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = project.name.orUntitled(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LabelValue(R.string.projects_course, project.courseName.orEmpty())
                LabelValue(R.string.projects_group, project.groupName.orEmpty())
                project.groupMode?.takeIf { it.isNotBlank() }?.let {
                    LabelValue(R.string.projects_group_mode, it)
                }
                LabelValue(R.string.projects_status, project.status.orEmpty())
                LabelValue(R.string.projects_deadline, formatInstant(project.deadline))
                LabelValue(R.string.projects_files, project.fileCount.toString())
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                ProjectProgress(project)
                
                if (project.steps.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_steps_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        project.steps.forEach { step ->
                            FilterChip(
                                selected = selectedStepId == step.id,
                                onClick = { selectedStepId = step.id },
                                label = { Text(step.title.orUntitled()) }
                            )
                        }
                    }
                    selectedStep?.let { step ->
                        CompactCard {
                            Text(step.title.orUntitled(), fontWeight = FontWeight.Medium)
                            LabelValue(R.string.projects_deadline, formatInstant(step.deadline))
                            LabelValue(R.string.projects_status, step.status.orEmpty())
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        project.steps.forEach { step ->
                            StepItem(step)
                        }
                    }
                }
                val myGroupId = project.groups.firstOrNull { it.isMine }?.id
                if (project.groups.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_groups_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val myGroup = project.groups.firstOrNull { it.isMine }
                    if (myGroup != null) {
                        CompactCard {
                            Text(
                                text = stringResource(R.string.projects_my_group, myGroup.name),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = myGroup.students.joinToString(", ").ifBlank { stringResource(R.string.projects_group_empty) },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onDeposit(myGroup.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.deposit_document))
                            }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { chatGroup = myGroup },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Forum, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.projects_chat_open))
                            }
                        }
                    }
                    var showAllGroupsDialog by remember { mutableStateOf(false) }
                    if (showAllGroupsDialog) {
                        AllGroupsDialog(
                            groups = project.groups,
                            documents = documents,
                            downloadingDocumentIds = downloadingDocumentIds,
                            documentDownloadProgress = documentDownloadProgress,
                            onOpenDocument = onOpenDocument,
                            onDownloadDocument = onDownloadDocument,
                            onOpenChat = { chatGroup = it },
                            onJoinGroup = onJoinGroup,
                            onLeaveGroup = onLeaveGroup,
                            maxStudents = project.maxStudents,
                            onDismiss = { showAllGroupsDialog = false }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showAllGroupsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.projects_view_all_groups))
                    }
                }
                val visibleDocuments = remember(documents, myGroupId) {
                    documents.filter { it.groupId == null || it.groupId == myGroupId }
                }
                if (visibleDocuments.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_files_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val groupedDocuments = remember(visibleDocuments) {
                        visibleDocuments.groupBy { it.category?.trim()?.takeIf(String::isNotBlank) }
                    }
                    val uncategorizedLabel = stringResource(R.string.documents_uncategorized)
                    groupedDocuments.forEach { (category, docs) ->
                        DocumentGroupSection(
                            title = category ?: uncategorizedLabel,
                            documents = docs,
                            downloadingDocumentIds = downloadingDocumentIds,
                            documentDownloadProgress = documentDownloadProgress,
                            showDownloadButton = true,
                            onOpenDocument = onOpenDocument,
                            onDownloadDocument = onDownloadDocument
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun AllGroupsDialog(
    groups: List<ProjectGroup>,
    documents: List<AcademicDocument>,
    downloadingDocumentIds: Set<String>,
    documentDownloadProgress: Map<String, Float?>,
    onOpenDocument: (AcademicDocument) -> Unit,
    onDownloadDocument: (AcademicDocument) -> Unit,
    onOpenChat: ((ProjectGroup) -> Unit)? = null,
    onJoinGroup: ((String) -> Unit)? = null,
    onLeaveGroup: ((String) -> Unit)? = null,
    maxStudents: Int? = null,
    onDismiss: () -> Unit
) {
    val sortedGroups = remember(groups) {
        groups.sortedWith(
            compareBy<ProjectGroup> { !it.isMine }
                .thenBy { group -> DIGITS_REGEX.find(group.name)?.value?.toIntOrNull() ?: Int.MAX_VALUE }
                .thenBy { group -> group.name }
        )
    }
    val alreadyInAGroup = groups.any { it.isMine }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_groups_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                sortedGroups.forEach { group ->
                    CompactCard {
                        Text(
                            text = if (group.isMine) {
                                stringResource(R.string.projects_my_group, group.name)
                            } else {
                                group.name
                            },
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = group.students.joinToString(", ").ifBlank { stringResource(R.string.projects_group_empty) },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        val groupDocs = documents.filter { it.groupId == group.id }
                        if (groupDocs.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            DocumentGroupSection(
                                title = stringResource(R.string.projects_deliverables_label),
                                documents = groupDocs,
                                downloadingDocumentIds = downloadingDocumentIds,
                                documentDownloadProgress = documentDownloadProgress,
                                showDownloadButton = group.isMine,
                                onOpenDocument = onOpenDocument,
                                onDownloadDocument = onDownloadDocument
                            )
                        }

                        if (group.isMine && onLeaveGroup != null) {
                            Spacer(Modifier.height(8.dp))
                            onOpenChat?.let { openChat ->
                                OutlinedButton(
                                    onClick = { openChat(group) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.Forum, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.projects_chat_open))
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                            OutlinedButton(
                                onClick = { onLeaveGroup(group.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.Logout, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.group_leave))
                            }
                        } else if (!group.isMine && !alreadyInAGroup && onJoinGroup != null) {
                            val isFull = maxStudents != null && group.students.size >= maxStudents
                            Spacer(Modifier.height(8.dp))
                            if (isFull) {
                                Text(
                                    text = stringResource(R.string.group_full),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Button(
                                    onClick = { onJoinGroup(group.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(Icons.Rounded.GroupAdd, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(stringResource(R.string.group_join))
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun ProjectChatDialog(
    group: ProjectGroup,
    state: FeatureUiState<List<ProjectMessage>>,
    onLoad: () -> Unit,
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember(group.id) { mutableStateOf("") }
    LaunchedEffect(group.id) {
        onLoad()
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_chat_title, group.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                state.error?.let { StateBanner(it) }
                if (state.refreshing) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                if (state.data.isEmpty() && !state.refreshing) {
                    Text(
                        text = stringResource(R.string.projects_chat_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 360.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.data, key = { it.id }) { message ->
                            ProjectMessageRow(message)
                        }
                    }
                }
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.projects_chat_message)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                onSend(draft)
                                draft = ""
                            },
                            enabled = draft.isNotBlank() && !state.refreshing
                        ) {
                            Icon(Icons.Rounded.Send, contentDescription = null)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun ProjectMessageRow(message: ProjectMessage) {
    val alignment = if (message.mine) Alignment.End else Alignment.Start
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        CompactCard(
            modifier = Modifier.fillMaxWidth(if (message.mine) 0.86f else 1f)
        ) {
            Text(
                text = message.author.ifBlank { stringResource(R.string.projects_chat_unknown_author) },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message.body,
                style = MaterialTheme.typography.bodyMedium
            )
            message.sentAt?.let {
                Text(
                    text = formatInstant(it),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun String?.isCompletedStatus(): Boolean {
    val normalized = this?.trim()?.lowercase().orEmpty()
    return normalized in setOf(
        "done", "completed", "complete", "closed", "validated", "validé", "validée", "terminé", "terminée", "fini", "finie", "rendu", "rendue"
    )
}

@Composable
private fun PracticalProgress(practical: Practical) {
    val progress = remember(practical) { practical.progress() }
    Text(
        text = stringResource(R.string.projects_progress),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    LinearProgressIndicator(
        progress = { progress.fraction.toFloat() },
        modifier = Modifier.fillMaxWidth().clip(CircleShape)
    )
    Text(
        text = if (progress.totalSteps == 0) {
            stringResource(R.string.projects_no_steps)
        } else {
            stringResource(R.string.projects_steps_completed, progress.completedSteps, progress.totalSteps)
        },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
internal fun PracticalCard(
    practical: Practical,
    onOpen: () -> Unit = {}
) {
    CompactCard(modifier = Modifier.clickable(onClick = onOpen)) {
        Text(
            text = practical.name.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.practicals_course, practical.courseName.orEmpty())
        LabelValue(R.string.practicals_start, formatInstant(practical.startsAt))
        LabelValue(R.string.practicals_end, formatInstant(practical.endsAt))
        LabelValue(R.string.agenda_room, practical.room.orEmpty())
        LabelValue(R.string.projects_status, practical.status.orEmpty())
        if (practical.steps.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            PracticalProgress(practical)
            practical.steps.forEach { step ->
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                StepItem(step)
            }
        }
    }
}

@Composable
private fun DocumentGroupSection(
    title: String,
    documents: List<AcademicDocument>,
    downloadingDocumentIds: Set<String>,
    documentDownloadProgress: Map<String, Float?>,
    showDownloadButton: Boolean,
    onOpenDocument: (AcademicDocument) -> Unit,
    onDownloadDocument: (AcademicDocument) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.small)
                .clickable { expanded = !expanded }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = null
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = documents.size.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expanded) {
            documents.forEach { document ->
                DocumentCard(
                    document = document,
                    downloading = document.id in downloadingDocumentIds,
                    progress = documentDownloadProgress[document.id],
                    showDownloadButton = showDownloadButton,
                    showCategory = false,
                    onDownload = { onDownloadDocument(document) },
                    onOpen = { onOpenDocument(document) }
                )
            }
        }
    }
}

@Composable
internal fun DocumentCard(
    document: AcademicDocument,
    downloading: Boolean,
    progress: Float?,
    showDownloadButton: Boolean = true,
    showCategory: Boolean = true,
    onDownload: (() -> Unit)? = null,
    onOpen: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    CompactCard {
        Text(
            text = document.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (showCategory) LabelValue(R.string.documents_category, document.category.orEmpty())
        LabelValue(R.string.profile_year, document.year.orEmpty())
        LabelValue(R.string.documents_updated_at, formatInstant(document.updatedAt))
        if (showDownloadButton) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpen()
                    },
                    enabled = !downloading,
                    modifier = Modifier.weight(1f)
                ) {
                    if (downloading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Rounded.OpenInNew, contentDescription = null)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(if (downloading) R.string.documents_downloading else R.string.documents_open))
                }
                if (onDownload != null) {
                    OutlinedIconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onDownload()
                        },
                        enabled = !downloading
                    ) {
                        Icon(
                            Icons.Rounded.Download,
                            contentDescription = stringResource(R.string.documents_download_only)
                        )
                    }
                }
            }
            if (downloading) {
                if (progress == null) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                } else {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
internal fun NewsCard(newsItem: NewsItem, onOpen: () -> Unit = {}) {
    val hasDetail = !newsItem.html.isNullOrBlank() || !newsItem.body.isNullOrBlank()
    CompactCard(
        modifier = if (hasDetail) Modifier.clickable(onClick = onOpen) else Modifier
    ) {
        if (!newsItem.imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = newsItem.imageUrl,
                contentDescription = newsItem.title,
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .clip(MaterialTheme.shapes.medium)
            )
        }
        Text(
            text = newsItem.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.common_date, formatInstant(newsItem.publishedAt))
        if (!newsItem.body.isNullOrBlank()) {
            Text(
                text = newsItem.body,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NewsDetailScreen(newsItem: NewsItem, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val document = remember(newsItem.html, newsItem.body, isDark) {
        wrapNewsHtml(newsItem.html ?: ("<p>" + (newsItem.body.orEmpty()) + "</p>"), isDark)
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_close))
            }
            Text(
                text = newsItem.title.orUntitled(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    settings.javaScriptEnabled = false
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.loadsImagesAutomatically = true
                    settings.blockNetworkImage = false
                    settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                    setBackgroundColor(android.graphics.Color.TRANSPARENT)
                    isNestedScrollingEnabled = true
                    
                    setOnTouchListener { v, event ->
                        v.parent?.requestDisallowInterceptTouchEvent(true)
                        false
                    }
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL("https://myges.fr/", document, "text/html", "utf-8", null)
            }
        )
    }
}


private fun wrapNewsHtml(bodyHtml: String, dark: Boolean): String {
    val bg = if (dark) "#1c1b1f" else "#ffffff"
    val fg = if (dark) "#e6e1e5" else "#1c1b1f"
    return """
        <!DOCTYPE html><html><head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
          html,body{margin:0;padding:16px;background:$bg;color:$fg;
            font-family:-apple-system,Roboto,sans-serif;font-size:16px;line-height:1.5;
            word-wrap:break-word;overflow-wrap:break-word;}
          img{max-width:100%;height:auto;}
          a{color:#3478f6;}
          table{max-width:100%;}
        </style></head><body>$bodyHtml</body></html>
    """.trimIndent()
}

@Composable
private fun SectionTitleText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp)
    )
}

private fun DashboardSummary?.isDashboardEmpty(): Boolean {
    return this == null ||
        (profile == null && nextEvent == null && latestGrades.isEmpty() && recentAbsences.isEmpty() && dueProjects.isEmpty())
}

private fun Context.hasCalendarPermissions(): Boolean {
    return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
}

private fun String.toGradeNumber(): Double? {
    return trim().replace(',', '.').toDoubleOrNull()?.takeIf { it >= 0.0 }
}

private enum class LanguageOption(@StringRes val title: Int, val languageTag: String?) {
    System(R.string.settings_language_system, null),
    French(R.string.settings_language_french, "fr"),
    English(R.string.settings_language_english, "en")
}

private val GRID_HOUR_HEIGHT = 56.dp
private val GRID_TIME_GUTTER = 44.dp
private val GRID_DAY_MIN_WIDTH = 96.dp

private val EVENT_PALETTE = listOf(
    Color(0xFF039BE5), Color(0xFF0B8043), Color(0xFFD50000),
    Color(0xFF8E24AA), Color(0xFFF4511E), Color(0xFF33B679),
    Color(0xFF3F51B5), Color(0xFF7986CB), Color(0xFFF6BF26),
    Color(0xFFE67C73), Color(0xFF616161)
)

private fun eventColor(event: AgendaEvent, mode: AgendaColorMode): Color {
    val key = when (mode) {
        AgendaColorMode.Course -> event.courseId?.takeIf { it.isNotBlank() } ?: event.title.ifBlank { event.id }
        AgendaColorMode.Location -> event.address?.takeIf { it.isNotBlank() }
            ?: event.room?.takeIf { it.isNotBlank() }
            ?: event.title.ifBlank { event.id }
    }
    val idx = (key.hashCode() and 0x7fffffff) % EVENT_PALETTE.size
    return EVENT_PALETTE[idx]
}

private fun timeLabel(instant: Instant, zone: ZoneId): String {
    val t = instant.atZone(zone)
    return "%02d:%02d".format(t.hour, t.minute)
}

private data class DayLayoutEvent(
    val event: AgendaEvent,
    val startMin: Int,
    val endMin: Int,
    val lane: Int,
    val laneCount: Int
)

private fun layoutDayEvents(events: List<AgendaEvent>, zone: ZoneId): List<DayLayoutEvent> {
    if (events.isEmpty()) return emptyList()
    val items = events.map { ev ->
        val s = ev.startsAt.atZone(zone)
        val e = ev.endsAt.atZone(zone)
        val startMin = s.hour * 60 + s.minute
        var endMin = e.hour * 60 + e.minute
        if (e.toLocalDate() != s.toLocalDate()) endMin = 24 * 60
        if (endMin <= startMin) endMin = startMin + 30
        Triple(ev, startMin, endMin)
    }.sortedBy { it.second }

    val result = mutableListOf<DayLayoutEvent>()
    var i = 0
    while (i < items.size) {
        var clusterEnd = items[i].third
        var j = i + 1
        while (j < items.size && items[j].second < clusterEnd) {
            clusterEnd = maxOf(clusterEnd, items[j].third)
            j++
        }
        val cluster = items.subList(i, j)
        val laneEnds = mutableListOf<Int>()
        val laneOf = IntArray(cluster.size)
        cluster.forEachIndexed { idx, (_, s, e) ->
            val lane = laneEnds.indexOfFirst { it <= s }
            if (lane == -1) {
                laneEnds.add(e)
                laneOf[idx] = laneEnds.size - 1
            } else {
                laneEnds[lane] = e
                laneOf[idx] = lane
            }
        }
        val laneCount = laneEnds.size
        cluster.forEachIndexed { idx, (ev, s, e) ->
            result.add(DayLayoutEvent(ev, s, e, laneOf[idx], laneCount))
        }
        i = j
    }
    return result
}

@Composable
private fun AgendaWeekGrid(
    weekStart: LocalDate,
    events: List<AgendaEvent>,
    colorMode: AgendaColorMode,
    onOpen: (AgendaEvent) -> Unit
) {
    val zone = remember { ZoneId.systemDefault() }
    val days = remember(weekStart) { (0 until 6).map { weekStart.plusDays(it.toLong()) } }
    val eventsByDay = remember(events, days) {
        days.associateWith { day -> events.filter { it.startsAt.atZone(zone).toLocalDate() == day } }
    }
    val allDayEvents = remember(eventsByDay) { eventsByDay.values.flatten() }
    val startHour = remember(allDayEvents) {
        (allDayEvents.minOfOrNull { it.startsAt.atZone(zone).hour } ?: 8).coerceAtMost(8)
    }
    val endHour = remember(allDayEvents) {
        val maxEnd = allDayEvents.maxOfOrNull {
            val e = it.endsAt.atZone(zone)
            if (e.minute > 0) e.hour + 1 else e.hour
        } ?: 19
        maxEnd.coerceAtLeast(19).coerceAtMost(24)
    }
    val rangeStartMin = startHour * 60
    val hours = remember(startHour, endHour) { (startHour until endHour).toList() }
    val totalHeight = GRID_HOUR_HEIGHT * hours.size
    val maxBodyHeight = (LocalConfiguration.current.screenHeightDp * 0.7f).dp
    val bodyHeight = if (totalHeight < maxBodyHeight) totalHeight else maxBodyHeight
    val scrollH = rememberScrollState()
    val locale = currentJavaLocale()
    val dayFmt = remember(locale) { DateTimeFormatter.ofPattern("EEE d", locale) }
    val hourFmt = remember(locale) { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
    val today = LocalDate.now()

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        
        val dayWidth = maxOf(GRID_DAY_MIN_WIDTH, (maxWidth - GRID_TIME_GUTTER) / days.size)
        Column {
            Row {
                Spacer(Modifier.width(GRID_TIME_GUTTER))
                Row(Modifier.horizontalScroll(scrollH)) {
                    days.forEach { day ->
                        val isToday = day == today
                        Box(
                            modifier = Modifier.width(dayWidth).padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.format(dayFmt).replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.height(bodyHeight).verticalScroll(rememberScrollState())
            ) {
                Row {
                    Column(Modifier.width(GRID_TIME_GUTTER)) {
                        hours.forEach { h ->
                            Box(Modifier.height(GRID_HOUR_HEIGHT)) {
                                Text(
                                    text = LocalTime.of(h, 0).format(hourFmt),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                    Row(Modifier.horizontalScroll(scrollH)) {
                        days.forEach { day ->
                            Box(
                                modifier = Modifier
                                    .width(dayWidth)
                                    .height(totalHeight)
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column {
                                    hours.forEach { _ ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(GRID_HOUR_HEIGHT)
                                                .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                                layoutDayEvents(eventsByDay[day].orEmpty(), zone).forEach { le ->
                                    val topDp = GRID_HOUR_HEIGHT * ((le.startMin - rangeStartMin) / 60f)
                                    val hDp = (GRID_HOUR_HEIGHT * ((le.endMin - le.startMin) / 60f)).coerceAtLeast(22.dp)
                                    val laneW = dayWidth / le.laneCount
                                    EventBlock(
                                        event = le.event,
                                        zone = zone,
                                        colorMode = colorMode,
                                        modifier = Modifier
                                            .offset(x = laneW * le.lane, y = topDp)
                                            .width(laneW)
                                            .height(hDp)
                                            .padding(1.dp),
                                        onOpen = { onOpen(le.event) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventBlock(
    event: AgendaEvent,
    zone: ZoneId,
    colorMode: AgendaColorMode,
    modifier: Modifier,
    onOpen: () -> Unit
) {
    val timeText = "${timeLabel(event.startsAt, zone)}–${timeLabel(event.endsAt, zone)}"
    val description = listOfNotNull(
        event.title.takeIf { it.isNotBlank() },
        timeText,
        event.room?.takeIf { it.isNotBlank() }
    ).joinToString(", ")
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(eventColor(event, colorMode))
            .clickable(onClick = onOpen, role = Role.Button)
            .semantics(mergeDescendants = true) { contentDescription = description }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Column {
            Text(
                text = event.title.orUntitled(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${timeLabel(event.startsAt, zone)}–${timeLabel(event.endsAt, zone)}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1
            )
            event.room?.takeIf { it.isNotBlank() }?.let { room ->
                Text(
                    text = room,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private enum class AgendaMode(@StringRes val title: Int) {
    Grid(R.string.agenda_mode_grid),
    Day(R.string.agenda_mode_day),
    Week7(R.string.agenda_mode_week_7),
    Month(R.string.agenda_mode_month),
    List(R.string.agenda_mode_list)
}

@Composable
private fun MonthCalendarGrid(
    selectedDate: LocalDate,
    daysWithEvents: Set<LocalDate>,
    onDaySelected: (LocalDate) -> Unit
) {
    val firstOfMonth = selectedDate.withDayOfMonth(1)
    val lengthOfMonth = selectedDate.lengthOfMonth()
    val firstDayOfWeek = firstOfMonth.dayOfWeek.value
    
    val locale = currentJavaLocale()
    val daysOfWeek = remember(locale) {
        (1..7).map {
            java.time.DayOfWeek.of(it)
                .getDisplayName(java.time.format.TextStyle.NARROW, locale)
                .uppercase(locale)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { dayName ->
                Text(
                    text = dayName,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
        
        val totalCells = (firstDayOfWeek - 1) + lengthOfMonth
        val numRows = (totalCells + 6) / 7
        
        for (r in 0 until numRows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val cellIndex = r * 7 + c
                    val dayNum = cellIndex - (firstDayOfWeek - 2)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (dayNum in 1..lengthOfMonth) {
                            val cellDate = selectedDate.withDayOfMonth(dayNum)
                            val isSelected = cellDate == selectedDate
                            val hasEvent = cellDate in daysWithEvents
                            
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .clickable { onDaySelected(cellDate) }
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = dayNum.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasEvent) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun List<AgendaEvent>.toIcsString(): String {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(ZoneOffset.UTC)
    val lines = mutableListOf(
        "BEGIN:VCALENDAR",
        "VERSION:2.0",
        "PRODID:-//MyGES Mobile//Calendar Export//EN",
        "CALSCALE:GREGORIAN"
    )
    forEach { event ->
        val desc = listOfNotNull(event.teacher, event.type, event.modality).joinToString(" - ")
        lines += "BEGIN:VEVENT"
        lines += "UID:${event.id.escapeIcsText()}"
        lines += "DTSTART:${formatter.format(event.startsAt)}"
        lines += "DTEND:${formatter.format(event.endsAt)}"
        lines += "SUMMARY:${event.title.escapeIcsText()}"
        lines += "DESCRIPTION:${desc.escapeIcsText()}"
        lines += "LOCATION:${event.room.orEmpty().escapeIcsText()}"
        lines += "END:VEVENT"
    }
    lines += "END:VCALENDAR"
    return lines.joinToString("\r\n", postfix = "\r\n") { it.foldIcsLine() }
}

private fun String.escapeIcsText(): String {
    return replace("\\", "\\\\")
        .replace(";", "\\;")
        .replace(",", "\\,")
        .replace("\r\n", "\\n")
        .replace("\n", "\\n")
        .replace("\r", "\\n")
}

private fun String.foldIcsLine(): String {
    if (length <= 75) return this
    val builder = StringBuilder()
    var index = 0
    while (index < length) {
        val end = minOf(index + 75, length)
        if (index > 0) builder.append("\r\n ")
        builder.append(this, index, end)
        index = end
    }
    return builder.toString()
}

private fun comparePeriods(p1: String, p2: String): Int {
    val year1 = FOUR_DIGITS_REGEX.find(p1)?.value?.toIntOrNull() ?: 0
    val year2 = FOUR_DIGITS_REGEX.find(p2)?.value?.toIntOrNull() ?: 0
    if (year1 != year2) {
        return year1.compareTo(year2)
    }
    val num1 = DIGITS_REGEX.findAll(p1).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
    val num2 = DIGITS_REGEX.findAll(p2).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
    return num1.compareTo(num2)
}

@Composable
private fun ColoredLabelValue(
    @StringRes label: Int,
    value: String,
    color: Color
) {
    if (value.isBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
private fun gradeColor(value: Double?, scale: Double?): Color {
    val normalized = value?.let { gradeValue ->
        val gradeScale = scale?.takeIf { it > 0.0 } ?: 20.0
        gradeValue / gradeScale * 20.0
    }
    return when {
        normalized == null -> MaterialTheme.colorScheme.onSurfaceVariant
        normalized >= 10.0 -> Color(0xFF2E7D32)
        else -> Color(0xFFC62828)
    }
}

private fun List<Grade>.withSimulatedValues(values: Map<String, Double>): List<Grade> {
    return map { it.withSimulatedValue(values[it.id]) }
}

private fun List<Grade>.withRecomputedMainGrades(mainOverrides: Set<String>): List<Grade> {
    val componentsByCourse = groupBy { it.courseName to it.period }
    return map { grade ->
        if (grade.id.contains("-cc-") || grade.id.contains("-exam") || grade.id in mainOverrides) {
            grade
        } else {
            val components = componentsByCourse[grade.courseName to grade.period].orEmpty()
            val ccValues = components.filter { it.id.contains("-cc-") }.mapNotNull { it.value }
            val examValue = components.firstOrNull { it.id.contains("-exam") }?.value
            val ccAverage = ccValues.takeIf { it.isNotEmpty() }?.average()
            grade.copy(value = combineCcExam(ccAverage, examValue) ?: grade.value)
        }
    }
}

private fun Grade.withSimulatedValue(value: Double?): Grade {
    return if (value == null) this else copy(value = value)
}

private fun loadGradeSimulations(
    preferences: SharedPreferences,
    allowedIds: Set<String>
): Map<String, Double> {
    return preferences.getString("values", null)
        .orEmpty()
        .lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val id = line.substring(0, separator)
            val value = line.substring(separator + 1).toDoubleOrNull()
            if (id in allowedIds && value != null) id to value else null
        }
        .toMap()
}

private fun saveGradeSimulations(
    preferences: SharedPreferences,
    values: Map<String, Double>
) {
    preferences.edit()
        .putString("values", values.entries.joinToString("\n") { "${it.key}=${it.value}" })
        .apply()
}

private fun clearGradeSimulations(preferences: SharedPreferences) {
    preferences.edit().remove("values").remove("blocks").apply()
}

private fun loadGradeBlocks(
    preferences: SharedPreferences,
    allowedKeys: Set<String>
): Map<String, String> {
    return preferences.getString("blocks", null)
        .orEmpty()
        .lineSequence()
        .mapNotNull { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) return@mapNotNull null
            val key = line.substring(0, separator)
            val block = line.substring(separator + 1).takeIf { it.isNotBlank() }
            if (key in allowedKeys && block != null) key to block else null
        }
        .toMap()
}

private fun saveGradeBlocks(
    preferences: SharedPreferences,
    blocks: Map<String, String>
) {
    preferences.edit()
        .putString("blocks", blocks.entries.joinToString("\n") { "${it.key}=${it.value}" })
        .apply()
}

private fun Grade.blockKey(): String {
    return "${academicYearLabel()}|$courseName"
}

private fun Grade.academicYearLabel(): String {
    ACADEMIC_YEAR_REGEX.find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    val gradeDate = date ?: return ""
    val startYear = if (gradeDate.monthValue >= 9) gradeDate.year else gradeDate.year - 1
    return "$startYear-${startYear + 1}"
}

private fun Course.academicYearLabel(): String? {
    year?.takeIf { it.isNotBlank() }?.let { startYear ->
        startYear.toIntOrNull()?.let { return "$it-${it + 1}" }
        return startYear
    }
    ACADEMIC_YEAR_REGEX.find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    return null
}

private fun Absence.academicYearLabel(): String {
    ACADEMIC_YEAR_REGEX.find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    val date = startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val startYear = if (date.monthValue >= 9) date.year else date.year - 1
    return "$startYear-${startYear + 1}"
}

private fun String.academicYearLabel(): String {
    ACADEMIC_YEAR_REGEX.find(this)?.value?.replace(" ", "")?.let { return it }
    return this
}

private fun Grade.semesterLabel(): String? {
    return period?.semesterLabel()
}

private fun Absence.semesterLabel(): String? {
    return period?.semesterLabel()
}

private fun Course.semesterLabel(): String? {
    return period?.semesterLabel()
}

private fun String.semesterLabel(): String? {
    return SEMESTER_REGEX.find(this)?.value
}

@Composable
private fun PracticalDetailsDialog(
    practical: Practical,
    documents: List<AcademicDocument>,
    downloadingDocumentIds: Set<String>,
    documentDownloadProgress: Map<String, Float?>,
    onOpenDocument: (AcademicDocument) -> Unit,
    onDownloadDocument: (AcademicDocument) -> Unit,
    onDeposit: (String) -> Unit,
    onJoinGroup: ((String) -> Unit)? = null,
    onLeaveGroup: ((String) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val myGroupId = practical.groups.firstOrNull { it.isMine }?.id
    val visibleDocuments = remember(documents, myGroupId) {
        documents.filter { it.groupId == null || it.groupId == myGroupId }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.practicals_detail_title)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = practical.name.orUntitled(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                LabelValue(R.string.practicals_course, practical.courseName.orEmpty())
                LabelValue(R.string.practicals_start, formatInstant(practical.startsAt))
                LabelValue(R.string.practicals_end, formatInstant(practical.endsAt))
                LabelValue(R.string.agenda_room, practical.room.orEmpty())
                LabelValue(R.string.projects_status, practical.status.orEmpty())

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                PracticalProgress(practical)

                if (practical.steps.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_steps_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        practical.steps.forEach { step ->
                            StepItem(step)
                        }
                    }
                }

                if (practical.groups.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_groups_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val myGroup = practical.groups.firstOrNull { it.isMine }
                    if (myGroup != null) {
                        CompactCard {
                            Text(
                                text = stringResource(R.string.projects_my_group, myGroup.name),
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = myGroup.students.joinToString(", ").ifBlank { stringResource(R.string.projects_group_empty) },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { onDeposit(myGroup.id) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.UploadFile, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.deposit_document))
                            }
                        }
                    }
                    var showAllGroupsDialog by remember { mutableStateOf(false) }
                    if (showAllGroupsDialog) {
                        AllGroupsDialog(
                            groups = practical.groups,
                            documents = documents,
                            downloadingDocumentIds = downloadingDocumentIds,
                            documentDownloadProgress = documentDownloadProgress,
                            onOpenDocument = onOpenDocument,
                            onDownloadDocument = onDownloadDocument,
                            onJoinGroup = onJoinGroup,
                            onLeaveGroup = onLeaveGroup,
                            onDismiss = { showAllGroupsDialog = false }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = { showAllGroupsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.projects_view_all_groups))
                    }
                }

                if (visibleDocuments.isNotEmpty()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text(
                        text = stringResource(R.string.projects_files_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    val groupedDocuments = remember(visibleDocuments) {
                        visibleDocuments.groupBy { it.category?.trim()?.takeIf(String::isNotBlank) }
                    }
                    val uncategorizedLabel = stringResource(R.string.documents_uncategorized)
                    groupedDocuments.forEach { (category, docs) ->
                        DocumentGroupSection(
                            title = category ?: uncategorizedLabel,
                            documents = docs,
                            downloadingDocumentIds = downloadingDocumentIds,
                            documentDownloadProgress = documentDownloadProgress,
                            showDownloadButton = true,
                            onOpenDocument = onOpenDocument,
                            onDownloadDocument = onDownloadDocument
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}
