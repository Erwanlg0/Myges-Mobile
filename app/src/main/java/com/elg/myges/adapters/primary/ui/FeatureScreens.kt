package com.elg.myges.adapters.primary.ui

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Logout
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
import com.elg.myges.adapters.secondary.pdf.PdfGenerator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.elg.myges.R
import com.elg.myges.adapters.primary.state.FeatureUiState
import com.elg.myges.adapters.primary.state.messageRes
import com.elg.myges.adapters.primary.viewmodel.SettingsViewModel
import com.elg.myges.adapters.primary.viewmodel.StudentViewModel
import com.elg.myges.domain.model.Absence
import com.elg.myges.domain.model.AcademicDocument
import com.elg.myges.domain.model.AgendaEvent
import com.elg.myges.domain.model.CalendarAccount
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.DirectoryPerson
import com.elg.myges.domain.model.DirectoryRole
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
import com.elg.myges.domain.model.ProjectGroup
import com.elg.myges.domain.model.ProjectStep
import com.elg.myges.domain.model.ThemeMode
import com.elg.myges.domain.model.UserSettings
import com.elg.myges.domain.model.progress
import com.elg.myges.domain.model.toGradeSummary
import coil.compose.AsyncImage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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
    var selectedMode by remember { mutableStateOf(AgendaMode.Week7) }
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
        onRetry = viewModel::refresh
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
                            context.startActivity(Intent.createChooser(intent, "Export Agenda"))
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
                                AgendaMode.Week5, AgendaMode.Week7 -> selectedDate.minusWeeks(1)
                                AgendaMode.Month -> selectedDate.minusMonths(1)
                                else -> selectedDate
                            }
                        }
                    ) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = stringResource(R.string.cd_previous))
                    }
                    
                    val headerText = when (selectedMode) {
                        AgendaMode.Day -> formatDate(selectedDate)
                        AgendaMode.Week5, AgendaMode.Week7 -> {
                            val startOfWeek = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                            val endDay = if (selectedMode == AgendaMode.Week5) 4 else 6
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
                                AgendaMode.Week5, AgendaMode.Week7 -> selectedDate.plusWeeks(1)
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
            AgendaMode.List -> {
                if (events.isEmpty()) {
                    item { CompactCard { Text(stringResource(R.string.agenda_filter_empty)) } }
                } else {
                    items(events, key = { it.id }) { event ->
                        AgendaEventCard(
                            event = event,
                            onOpen = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedEvent = event
                            }
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
                            onOpen = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedEvent = event
                            }
                        )
                    }
                }
            }
            AgendaMode.Week5, AgendaMode.Week7 -> {
                val startOfWeek = selectedDate.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                val numDays = if (selectedMode == AgendaMode.Week5) 5 else 7
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
                                onOpen = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedEvent = event
                                }
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
                            onOpen = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                selectedEvent = event
                            }
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
        onRetry = viewModel::refresh
    ) { grades ->
        val filteredGrades = grades.filter { grade ->
            (selectedYear == null || grade.academicYearLabel() == selectedYear) &&
                (selectedSemester == null || grade.semesterLabel() == selectedSemester)
        }
        val displayedGrades = if (simulationMode) {
            filteredGrades.withSimulatedValues(simulatedValues)
        } else {
            filteredGrades
        }.withRecomputedMainGrades(if (simulationMode) simulatedValues.keys else emptySet())

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

        val structuredMainKeys = displayedGrades
            .filter { it.subject.isBlank() && !it.id.contains("-cc-") && !it.id.contains("-exam") }
            .map { it.courseName to it.period }
            .toSet()
        val ccSubjectRegex = Regex("^(cc|contrôle continu)\\s*\\d*$", RegexOption.IGNORE_CASE)
        val mainGrades = displayedGrades.filter { grade ->
            !grade.id.contains("-cc-") &&
            !grade.id.contains("-exam") &&
            !((grade.courseName to grade.period) in structuredMainKeys &&
                (grade.subject.matches(ccSubjectRegex) ||
                 grade.subject.trim().equals("examen", ignoreCase = true)))
        }

        item { GradeSummaryCard(mainGrades, displayedGrades) }
        if (blockAssignments.isNotEmpty()) {
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
        
        val gradeItems = mainGrades
        items(gradeItems, key = { it.id }) { grade ->
            GradeCard(
                grade = grade,
                onOpen = { selectedGrade = grade }
            )
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
        onRetry = viewModel::refresh
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
        onRetry = viewModel::refresh
    ) { _ ->
        if (years.size > 1 || periods.size > 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Year Selector
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

                    // Period Selector
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
            .filter { it.name.isNotBlank() }
            .filter { selectedYear == null || it.year == selectedYear }
            .sortedWith(compareByDescending<Project> { it.year }.thenByDescending { it.startsAt ?: it.deadline }.thenBy { it.name })
    }
    selectedProject?.let { project ->
        ProjectDetailsDialog(
            project = project,
            documents = documentsState.data.filter { it.ownerId == project.id },
            downloadingDocumentIds = downloadingDocumentIds,
            documentDownloadProgress = documentDownloadProgress,
            onOpenDocument = viewModel::openDocument,
            onDismiss = { selectedProject = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<Project>::isEmpty,
        emptyTitle = R.string.projects_empty_title,
        emptyBody = R.string.projects_empty_body,
        onRetry = viewModel::refresh
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
    selectedPractical?.let { practical ->
        PracticalDetailsDialog(
            practical = practical,
            documents = documentsState.data.filter { it.ownerId == practical.id },
            downloadingDocumentIds = downloadingDocumentIds,
            documentDownloadProgress = documentDownloadProgress,
            onOpenDocument = viewModel::openDocument,
            onDismiss = { selectedPractical = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<Practical>::isEmpty,
        emptyTitle = R.string.practicals_empty_title,
        emptyBody = R.string.practicals_empty_body,
        onRetry = viewModel::refresh
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
        onRetry = viewModel::refresh
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
        onRetry = viewModel::refresh,
        listState = listState
    ) { _ ->
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
    year?.takeIf { it.isNotBlank() }?.let { return it }
    val instant = updatedAt ?: return null
    val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
    val start = if (date.monthValue >= 8) date.year else date.year - 1
    return "$start-${start + 1}"
}

@Composable
fun NotificationsScreen(
    studentViewModel: StudentViewModel,
    settingsViewModel: SettingsViewModel
) {
    val newsState by studentViewModel.news.collectAsStateWithLifecycle()
    val settingsState by settingsViewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val notificationLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}
    FeatureStateContent(
        state = newsState,
        empty = List<NewsItem>::isEmpty,
        emptyTitle = R.string.notifications_empty_title,
        emptyBody = R.string.notifications_empty_body,
        onRetry = studentViewModel::refresh
    ) { news ->
        item {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                OutlinedButton(onClick = { notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }) {
                    Icon(Icons.Rounded.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.notifications_grant_permission))
                }
            }
        }
        settingsState.settings?.let { settings ->
            item { NotificationPreferences(settings, settingsViewModel) }
        }
        items(news, key = { it.id }) { item -> NewsCard(item) }
    }
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
            }
        }
        item {
            DataCard {
                Text(
                    text = stringResource(R.string.settings_notifications),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                NotificationPreferences(settings, settingsViewModel)
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
        SwitchRow(R.string.settings_notify_grades, settings.notifications.grades, settingsViewModel::setGradeNotifications)
        SwitchRow(R.string.settings_notify_absences, settings.notifications.absences, settingsViewModel::setAbsenceNotifications)
        SwitchRow(R.string.settings_notify_agenda, settings.notifications.agenda, settingsViewModel::setAgendaNotifications)
        SwitchRow(R.string.settings_notify_projects, settings.notifications.projects, settingsViewModel::setProjectNotifications)
        SwitchRow(R.string.settings_notify_documents, settings.notifications.documents, settingsViewModel::setDocumentNotifications)
    }
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
        Text(
            text = grade.courseName.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(grade.subject.orUntitled())
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
        LabelValue(R.string.grades_coefficient, formatNumber(grade.coefficient))
        if (grade.value != null) {
            LabelValue(R.string.grades_average, formatNumber(grade.average))
        }
        LabelValue(R.string.common_date, formatDate(grade.date))
        if (!grade.period.isNullOrBlank()) {
            LabelValue(R.string.common_period, grade.period)
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
        allGrades.filter { it.id.contains("-cc-") }.mapNotNull { it.value }.takeIf { it.isNotEmpty() }?.average()
    }
    val examAverage = remember(allGrades) {
        allGrades.filter { it.id.contains("-exam") }.mapNotNull { it.value }.takeIf { it.isNotEmpty() }?.average()
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
        LabelValue(R.string.grades_coefficient, formatNumber(grade.coefficient))
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
                LabelValue(R.string.grades_coefficient, formatNumber(grade.coefficient))
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
                    TextButton(
                        onClick = {
                            runCatching {
                                val file = File(context.cacheDir, "${course.name}_syllabus.pdf")
                                file.outputStream().use {
                                    PdfGenerator.generatePdfFromText(course.syllabus, "${course.name} - Syllabus", it)
                                }
                                val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "application/pdf"
                                    putExtra(Intent.EXTRA_STREAM, uri)
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                }
                                context.startActivity(Intent.createChooser(intent, "Syllabus - ${course.name}"))
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
    onDismiss: () -> Unit
) {
    var selectedStepId by remember(project.id) { mutableStateOf(project.steps.firstOrNull()?.id) }
    val selectedStep = project.steps.firstOrNull { it.id == selectedStepId }
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
                    visibleDocuments.forEach { document ->
                        DocumentCard(
                            document = document,
                            downloading = document.id in downloadingDocumentIds,
                            progress = documentDownloadProgress[document.id],
                            showDownloadButton = true,
                            onOpen = { onOpenDocument(document) }
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
    onDismiss: () -> Unit
) {
    val sortedGroups = remember(groups) {
        groups.sortedWith(
            compareBy<ProjectGroup> { !it.isMine }
                .thenBy { group -> Regex("\\d+").find(group.name)?.value?.toIntOrNull() ?: Int.MAX_VALUE }
                .thenBy { group -> group.name }
        )
    }
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
                            Text(
                                text = stringResource(R.string.projects_deliverables_label),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            groupDocs.forEach { document ->
                                DocumentCard(
                                    document = document,
                                    downloading = document.id in downloadingDocumentIds,
                                    progress = documentDownloadProgress[document.id],
                                    showDownloadButton = group.isMine,
                                    onOpen = { onOpenDocument(document) }
                                )
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
internal fun DocumentCard(
    document: AcademicDocument,
    downloading: Boolean,
    progress: Float?,
    showDownloadButton: Boolean = true,
    showCategory: Boolean = true,
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
            OutlinedButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpen()
                },
                enabled = !downloading
            ) {
                if (downloading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.Download, contentDescription = null)
                }
                Spacer(Modifier.width(8.dp))
                Text(stringResource(if (downloading) R.string.documents_downloading else R.string.documents_open))
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
internal fun NewsCard(newsItem: NewsItem) {
    CompactCard {
        Text(
            text = newsItem.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.common_date, formatInstant(newsItem.publishedAt))
        if (!newsItem.body.isNullOrBlank()) {
            Text(newsItem.body)
        }
    }
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

private enum class AgendaMode(@StringRes val title: Int) {
    Day(R.string.agenda_mode_day),
    Week5(R.string.agenda_mode_week_5),
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
    
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    
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
    val sb = java.lang.StringBuilder()
    sb.append("BEGIN:VCALENDAR\n")
    sb.append("VERSION:2.0\n")
    sb.append("PRODID:-//MyGES Mobile//Calendar Export//EN\n")
    sb.append("CALSCALE:GREGORIAN\n")
    forEach { event ->
        sb.append("BEGIN:VEVENT\n")
        sb.append("UID:").append(event.id).append("\n")
        sb.append("DTSTART:").append(formatter.format(event.startsAt)).append("\n")
        sb.append("DTEND:").append(formatter.format(event.endsAt)).append("\n")
        sb.append("SUMMARY:").append(event.title).append("\n")
        val desc = listOfNotNull(event.teacher, event.type, event.modality).joinToString(" - ")
        sb.append("DESCRIPTION:").append(desc).append("\n")
        sb.append("LOCATION:").append(event.room.orEmpty()).append("\n")
        sb.append("END:VEVENT\n")
    }
    sb.append("END:VCALENDAR\n")
    return sb.toString()
}

private fun comparePeriods(p1: String, p2: String): Int {
    val yearRegex = Regex("\\d{4}")
    val year1 = yearRegex.find(p1)?.value?.toIntOrNull() ?: 0
    val year2 = yearRegex.find(p2)?.value?.toIntOrNull() ?: 0
    if (year1 != year2) {
        return year1.compareTo(year2)
    }
    
    val numRegex = Regex("\\d+")
    val num1 = numRegex.findAll(p1).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
    val num2 = numRegex.findAll(p2).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
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
    return map { grade ->
        if (grade.id.contains("-cc-") || grade.id.contains("-exam") || grade.id in mainOverrides) {
            grade
        } else {
            val components = filter { it.courseName == grade.courseName && it.period == grade.period }
            val ccValues = components.filter { it.id.contains("-cc-") }.mapNotNull { it.value }
            val examValue = components.firstOrNull { it.id.contains("-exam") }?.value
            val ccAverage = ccValues.takeIf { it.isNotEmpty() }?.average()
            val recomputedValue = when {
                ccAverage != null && examValue != null -> 0.5 * ccAverage + 0.5 * examValue
                ccAverage != null -> ccAverage
                examValue != null -> examValue
                else -> grade.value
            }
            grade.copy(value = recomputedValue)
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
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    val gradeDate = date ?: return ""
    val startYear = if (gradeDate.monthValue >= 9) gradeDate.year else gradeDate.year - 1
    return "$startYear-${startYear + 1}"
}

private fun Course.academicYearLabel(): String? {
    year?.takeIf { it.isNotBlank() }?.let { startYear ->
        startYear.toIntOrNull()?.let { return "$it-${it + 1}" }
        return startYear
    }
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    return null
}

private fun Absence.academicYearLabel(): String {
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(period.orEmpty())?.value?.replace(" ", "")?.let { return it }
    val date = startsAt.atZone(ZoneId.systemDefault()).toLocalDate()
    val startYear = if (date.monthValue >= 9) date.year else date.year - 1
    return "$startYear-${startYear + 1}"
}

private fun String.academicYearLabel(): String {
    Regex("\\d{4}\\s*-\\s*\\d{4}").find(this)?.value?.replace(" ", "")?.let { return it }
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
    return Regex("Semestre\\s*\\d+", RegexOption.IGNORE_CASE).find(this)?.value
}

@Composable
private fun PracticalDetailsDialog(
    practical: Practical,
    documents: List<AcademicDocument>,
    downloadingDocumentIds: Set<String>,
    documentDownloadProgress: Map<String, Float?>,
    onOpenDocument: (AcademicDocument) -> Unit,
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
                    visibleDocuments.forEach { document ->
                        DocumentCard(
                            document = document,
                            downloading = document.id in downloadingDocumentIds,
                            progress = documentDownloadProgress[document.id],
                            showDownloadButton = true,
                            onOpen = { onOpenDocument(document) }
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
