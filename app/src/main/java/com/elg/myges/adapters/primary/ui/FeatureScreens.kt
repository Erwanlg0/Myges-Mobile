package com.elg.myges.adapters.primary.ui

import android.Manifest
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowForward
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.aspectRatio
import androidx.core.content.FileProvider
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.ZoneOffset
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.elg.myges.domain.model.Course
import com.elg.myges.domain.model.DashboardSummary
import com.elg.myges.domain.model.Grade
import com.elg.myges.domain.model.NewsItem
import com.elg.myges.domain.model.Practical
import com.elg.myges.domain.model.Project
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
                dashboard.recentAbsences.firstOrNull()?.period?.let { period ->
                    IconButton(
                        onClick = {
                            viewModel.navigateToAbsencesPeriod(period)
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
                        lastAbsence.period?.let { period ->
                            viewModel.navigateToAbsencesPeriod(period)
                            onNavigateToTab("absences")
                        }
                    }
                )
            }
        }
        item { SectionTitle(R.string.dashboard_due_projects) }
        if (dashboard.dueProjects.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.projects_empty_title)) } }
        } else {
            items(dashboard.dueProjects, key = { it.id }) { project ->
                var showDialog by remember { mutableStateOf(false) }
                if (showDialog) {
                    ProjectDetailsDialog(project) { showDialog = false }
                }
                ProjectCard(project, onOpen = { showDialog = true })
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
fun AgendaScreen(viewModel: StudentViewModel) {
    val state by viewModel.agenda.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var selectedMode by remember { mutableStateOf(AgendaMode.Week7) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedEvent by remember { mutableStateOf<AgendaEvent?>(null) }

    LaunchedEffect(viewModel) {
        viewModel.agendaDateToNavigate.collect { targetDate ->
            selectedDate = targetDate
            selectedMode = AgendaMode.Week7
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
fun GradesScreen(viewModel: StudentViewModel) {
    val state by viewModel.grades.collectAsStateWithLifecycle()
    val coursesState by viewModel.courses.collectAsStateWithLifecycle()
    val absencesState by viewModel.absences.collectAsStateWithLifecycle()
    var selectedGrade by remember { mutableStateOf<Grade?>(null) }
    
    val gradesList = state.data.orEmpty()
    val coursesList = coursesState.data.orEmpty()
    val absencesList = absencesState.data.orEmpty()

    val periods = remember(gradesList, coursesList, absencesList) {
        val all = (gradesList.mapNotNull { it.period } + 
                   coursesList.mapNotNull { it.period } + 
                   absencesList.mapNotNull { it.period })
            .filter { it.isNotBlank() }
            .distinct()
        all.sortedWith { p1, p2 -> comparePeriods(p2, p1) }
    }
    var selectedPeriod by remember(periods) {
        val defaultPeriod = periods.firstOrNull { period ->
            gradesList.any { it.period == period }
        } ?: periods.firstOrNull()
        mutableStateOf(defaultPeriod)
    }

    LaunchedEffect(viewModel) {
        viewModel.gradesPeriodToNavigate.collect { targetPeriod ->
            selectedPeriod = targetPeriod
        }
    }

    selectedGrade?.let { grade ->
        val components = remember(grade, gradesList) {
            gradesList.filter { it.courseName == grade.courseName && it.period == grade.period }
        }
        GradeDetailsDialog(
            grade = grade,
            components = components,
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
        val filteredGrades = if (selectedPeriod != null) {
            grades.filter { it.period == selectedPeriod }
        } else {
            grades
        }

        if (periods.isNotEmpty()) {
            item {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    CompactCard(
                        modifier = Modifier.clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPeriod ?: "",
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
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriod = period
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        val mainGrades = filteredGrades.filter { !it.id.contains("-cc-") && !it.id.contains("-exam") }

        item { GradeSummaryCard(mainGrades) }
        item { GradeSimulationCard(mainGrades) }
        
        items(mainGrades, key = { it.id }) { grade ->
            GradeCard(
                grade = grade,
                onOpen = { selectedGrade = grade }
            )
        }
    }
}

@Composable
fun AbsencesScreen(viewModel: StudentViewModel) {
    val state by viewModel.absences.collectAsStateWithLifecycle()
    val coursesState by viewModel.courses.collectAsStateWithLifecycle()
    val gradesState by viewModel.grades.collectAsStateWithLifecycle()
    
    val absencesList = state.data.orEmpty()
    val coursesList = coursesState.data.orEmpty()
    val gradesList = gradesState.data.orEmpty()

    val periods = remember(absencesList, coursesList, gradesList) {
        val all = (absencesList.mapNotNull { it.period } + 
                   coursesList.mapNotNull { it.period } + 
                   gradesList.mapNotNull { it.period })
            .filter { it.isNotBlank() }
            .distinct()
        all.sortedWith { p1, p2 -> comparePeriods(p2, p1) }
    }
    var selectedPeriod by remember(periods) {
        val defaultPeriod = periods.firstOrNull { period ->
            absencesList.any { it.period == period }
        } ?: periods.firstOrNull()
        mutableStateOf(defaultPeriod)
    }

    LaunchedEffect(viewModel) {
        viewModel.absencesPeriodToNavigate.collect { targetPeriod ->
            selectedPeriod = targetPeriod
        }
    }

    FeatureStateContent(
        state = state,
        empty = List<Absence>::isEmpty,
        emptyTitle = R.string.absences_empty_title,
        emptyBody = R.string.absences_empty_body,
        onRetry = viewModel::refresh
    ) { absences ->
        val filteredAbsences = if (selectedPeriod != null) {
            absences.filter { it.period == selectedPeriod }
        } else {
            absences
        }

        if (periods.isNotEmpty()) {
            item {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    CompactCard(
                        modifier = Modifier.clickable { expanded = true }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedPeriod ?: "",
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
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriod = period
                                    expanded = false
                                }
                            )
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
    FeatureStateContent(
        state = state,
        empty = List<Course>::isEmpty,
        emptyTitle = R.string.courses_empty_title,
        emptyBody = R.string.courses_empty_body,
        onRetry = viewModel::refresh
    ) { courses ->
        items(courses, key = { it.id }) { course -> CourseCard(course) }
    }
}

@Composable
fun ProjectsScreen(viewModel: StudentViewModel) {
    val state by viewModel.projects.collectAsStateWithLifecycle()
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    selectedProject?.let { project ->
        ProjectDetailsDialog(
            project = project,
            onDismiss = { selectedProject = null }
        )
    }
    FeatureStateContent(
        state = state,
        empty = List<Project>::isEmpty,
        emptyTitle = R.string.projects_empty_title,
        emptyBody = R.string.projects_empty_body,
        onRetry = viewModel::refresh
    ) { projects ->
        items(projects, key = { it.id }) { project ->
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
    FeatureStateContent(
        state = state,
        empty = List<Practical>::isEmpty,
        emptyTitle = R.string.practicals_empty_title,
        emptyBody = R.string.practicals_empty_body,
        onRetry = viewModel::refresh
    ) { practicals ->
        items(practicals, key = { it.id }) { practical -> PracticalCard(practical) }
    }
}

@Composable
fun DocumentsScreen(viewModel: StudentViewModel) {
    val state by viewModel.documents.collectAsStateWithLifecycle()
    val downloadingDocumentIds by viewModel.downloadingDocumentIds.collectAsStateWithLifecycle()
    val documentDownloadProgress by viewModel.documentDownloadProgress.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = state,
        empty = List<AcademicDocument>::isEmpty,
        emptyTitle = R.string.documents_empty_title,
        emptyBody = R.string.documents_empty_body,
        onRetry = viewModel::refresh
    ) { documents ->
        items(documents, key = { it.id }) { document ->
            DocumentCard(
                document = document,
                downloading = document.id in downloadingDocumentIds,
                progress = documentDownloadProgress[document.id],
                onOpen = { viewModel.openDocument(document) }
            )
        }
    }
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
    val settings = state.settings
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val enableCalendarSync = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        settingsViewModel.setCalendarSync(true)
        if (agendaState.data.isNotEmpty()) studentViewModel.syncAgendaToCalendar(agendaState.data)
    }
    val calendarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) enableCalendarSync()
    }
    if (settings == null) {
        LoadingState()
        return
    }
    androidx.compose.foundation.lazy.LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val currentError = state.error
        if (currentError != null) {
            item { StateBanner(currentError.messageRes()) }
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
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Text(
                text = stringResource(R.string.grades_no_grade),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LabelValue(R.string.grades_coefficient, formatNumber(grade.coefficient))
        LabelValue(R.string.grades_average, formatNumber(grade.average))
        LabelValue(R.string.common_date, formatDate(grade.date))
        if (!grade.period.isNullOrBlank()) {
            LabelValue(R.string.common_period, grade.period)
        }
    }
}

@Composable
internal fun GradeSummaryCard(grades: List<Grade>) {
    val summary = remember(grades) { grades.toGradeSummary() }
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
            LabelValue(R.string.grades_weighted_average, formatNumber(summary.weightedAverage))
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
private fun GradeDetailsDialog(
    grade: Grade,
    components: List<Grade>,
    onDismiss: () -> Unit
) {
    val ccComponents = components.filter { it.id.contains("-cc-") }
    val examComponent = components.firstOrNull { it.id.contains("-exam") }
    
    val ccValues = ccComponents.mapNotNull { it.value }
    val ccAverage = if (ccValues.isNotEmpty()) ccValues.average() else null

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
                
                if (ccComponents.isNotEmpty()) {
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
                
                examComponent?.let { exam ->
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
private fun GradeSimulationCard(grades: List<Grade>) {
    var valueText by remember { mutableStateOf("") }
    var coefficientText by remember { mutableStateOf("1") }
    var labelText by remember { mutableStateOf("") }
    var simulatedList by remember { mutableStateOf(emptyList<Grade>()) }
    
    val overallGrades = remember(grades, simulatedList) {
        grades + simulatedList
    }
    val summary = remember(overallGrades) { overallGrades.toGradeSummary() }
    
    DataCard {
        Text(
            text = stringResource(R.string.grades_simulation_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        
        if (simulatedList.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                simulatedList.forEachIndexed { index, sim ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                          Text(
                              text = "${sim.courseName} (coef ${formatNumber(sim.coefficient)}): ${formatNumber(sim.value)}/20",
                              style = MaterialTheme.typography.bodyMedium
                          )
                          IconButton(
                              onClick = { simulatedList = simulatedList.filterIndexed { i, _ -> i != index } },
                              modifier = Modifier.size(24.dp)
                          ) {
                              Icon(
                                  imageVector = Icons.Rounded.Delete,
                                  contentDescription = stringResource(R.string.action_delete),
                                  modifier = Modifier.size(16.dp)
                              )
                          }
                      }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = labelText,
                onValueChange = { labelText = it },
                label = { Text(stringResource(R.string.grades_simulation_label)) },
                modifier = Modifier.weight(1.5f),
                singleLine = true
            )
            OutlinedTextField(
                value = valueText,
                onValueChange = { valueText = it },
                label = { Text(stringResource(R.string.grades_simulation_value)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = coefficientText,
                onValueChange = { coefficientText = it },
                label = { Text(stringResource(R.string.grades_simulation_coefficient)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }
        
        Button(
            onClick = {
                val value = valueText.toGradeNumber()
                val coef = coefficientText.toGradeNumber() ?: 1.0
                if (value != null) {
                    val label = labelText.trim().ifBlank { "Simulated" }
                    val newSim = Grade(
                        id = "sim-${System.currentTimeMillis()}",
                        courseName = label,
                        subject = label,
                        value = value,
                        scale = 20.0,
                        coefficient = coef,
                        average = null,
                        date = null,
                        period = null
                    )
                    simulatedList = simulatedList + newSim
                    valueText = ""
                    labelText = ""
                    coefficientText = "1"
                }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(stringResource(R.string.grades_simulation_add))
        }
        
        LabelValue(R.string.grades_simulation_average, formatNumber(summary.weightedAverage))
    }
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
internal fun CourseCard(course: Course) {
    var syllabusExpanded by remember(course.id) { mutableStateOf(false) }
    CompactCard {
        Text(
            text = course.name.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.courses_teacher, course.teacher.orEmpty())
        LabelValue(R.string.profile_year, course.year.orEmpty())
        LabelValue(R.string.common_period, course.period.orEmpty())
        LabelValue(R.string.courses_files, course.fileCount.toString())
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
        if (!course.syllabus.isNullOrBlank()) {
            HorizontalDivider()
            TextButton(onClick = { syllabusExpanded = !syllabusExpanded }) {
                Text(stringResource(if (syllabusExpanded) R.string.courses_hide_syllabus else R.string.courses_show_syllabus))
            }
            if (syllabusExpanded) {
                Text(course.syllabus)
            }
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
                LabelValue(R.string.projects_group, project.groupName.orEmpty())
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
            Text(step.title.orUntitled(), fontWeight = FontWeight.Medium)
            LabelValue(R.string.projects_deadline, formatInstant(step.deadline))
            LabelValue(R.string.projects_status, step.status.orEmpty())
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
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.projects_detail_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        project.steps.forEach { step ->
                            Column {
                                Text(
                                    text = step.title.orUntitled(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = formatInstant(step.deadline),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = step.status.orEmpty(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (step.status.isCompletedStatus()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                    )
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

private fun String?.isCompletedStatus(): Boolean {
    val normalized = this?.trim()?.lowercase().orEmpty()
    return normalized in setOf(
        "done", "completed", "complete", "closed", "validated", "validé", "validée", "terminé", "terminée", "fini", "finie", "rendu", "rendue"
    )
}

@Composable
internal fun PracticalCard(practical: Practical) {
    CompactCard {
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
    }
}

@Composable
internal fun DocumentCard(
    document: AcademicDocument,
    downloading: Boolean,
    progress: Float?,
    onOpen: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    CompactCard {
        Text(
            text = document.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.documents_category, document.category.orEmpty())
        LabelValue(R.string.profile_year, document.year.orEmpty())
        LabelValue(R.string.documents_updated_at, formatInstant(document.updatedAt))
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
