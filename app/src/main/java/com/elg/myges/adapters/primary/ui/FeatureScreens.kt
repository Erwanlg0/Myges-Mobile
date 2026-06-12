package com.elg.myges.adapters.primary.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Logout
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Composable
fun DashboardScreen(viewModel: StudentViewModel) {
    val state by viewModel.dashboard.collectAsStateWithLifecycle()
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
        item { SectionTitle(R.string.dashboard_next_course) }
        item {
            dashboard.nextEvent?.let { AgendaEventCard(it) }
                ?: CompactCard { Text(stringResource(R.string.dashboard_no_next_course)) }
        }
        item { SectionTitle(R.string.dashboard_latest_grades) }
        if (dashboard.latestGrades.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.grades_empty_title)) } }
        } else {
            items(dashboard.latestGrades, key = { it.id }) { GradeCard(it) }
        }
        item { SectionTitle(R.string.dashboard_recent_absences) }
        if (dashboard.recentAbsences.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.absences_empty_title)) } }
        } else {
            items(dashboard.recentAbsences, key = { it.id }) { AbsenceCard(it) }
        }
        item { SectionTitle(R.string.dashboard_due_projects) }
        if (dashboard.dueProjects.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.projects_empty_title)) } }
        } else {
            items(dashboard.dueProjects, key = { it.id }) { ProjectCard(it) }
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
    var selectedMode by remember { mutableStateOf(AgendaMode.List) }
    val calendarLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.all { it }) viewModel.syncAgendaToCalendar(state.data)
    }
    FeatureStateContent(
        state = state,
        empty = List<AgendaEvent>::isEmpty,
        emptyTitle = R.string.agenda_empty_title,
        emptyBody = R.string.agenda_empty_body,
        onRetry = viewModel::refresh
    ) { events ->
        val filteredEvents = events.filterForMode(selectedMode)
        item {
            AgendaModeSelector(selectedMode) { selectedMode = it }
        }
        item {
            OutlinedButton(
                onClick = {
                    if (context.hasCalendarPermissions()) {
                        viewModel.syncAgendaToCalendar(events)
                    } else {
                        calendarLauncher.launch(arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR))
                    }
                }
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.agenda_sync_calendar))
            }
        }
        if (filteredEvents.isEmpty()) {
            item { CompactCard { Text(stringResource(R.string.agenda_filter_empty)) } }
        } else {
            items(filteredEvents, key = { it.id }) { event -> AgendaEventCard(event) }
        }
    }
}

@Composable
fun GradesScreen(viewModel: StudentViewModel) {
    val state by viewModel.grades.collectAsStateWithLifecycle()
    val noPeriod = stringResource(R.string.common_no_period)
    FeatureStateContent(
        state = state,
        empty = List<Grade>::isEmpty,
        emptyTitle = R.string.grades_empty_title,
        emptyBody = R.string.grades_empty_body,
        onRetry = viewModel::refresh
    ) { grades ->
        grades.groupBy { it.period ?: noPeriod }.forEach { (period, periodGrades) ->
            item { SectionTitleText(period) }
            items(periodGrades, key = { it.id }) { grade -> GradeCard(grade) }
        }
    }
}

@Composable
fun AbsencesScreen(viewModel: StudentViewModel) {
    val state by viewModel.absences.collectAsStateWithLifecycle()
    FeatureStateContent(
        state = state,
        empty = List<Absence>::isEmpty,
        emptyTitle = R.string.absences_empty_title,
        emptyBody = R.string.absences_empty_body,
        onRetry = viewModel::refresh
    ) { absences ->
        item {
            val unjustified = absences.count { !it.justified }
            DataCard {
                Text(
                    text = pluralStringResource(R.plurals.absences_count, absences.size, absences.size),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = pluralStringResource(R.plurals.absences_unjustified_count, unjustified, unjustified),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        items(absences, key = { it.id }) { absence -> AbsenceCard(absence) }
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
    FeatureStateContent(
        state = state,
        empty = List<Project>::isEmpty,
        emptyTitle = R.string.projects_empty_title,
        emptyBody = R.string.projects_empty_body,
        onRetry = viewModel::refresh
    ) { projects ->
        items(projects, key = { it.id }) { project -> ProjectCard(project) }
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
    FeatureStateContent(
        state = state,
        empty = List<AcademicDocument>::isEmpty,
        emptyTitle = R.string.documents_empty_title,
        emptyBody = R.string.documents_empty_body,
        onRetry = viewModel::refresh
    ) { documents ->
        items(documents, key = { it.id }) { document ->
            DocumentCard(document, onOpen = { viewModel.openDocument(document) })
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
    val enableCalendarSync = {
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
                OutlinedButton(onClick = studentViewModel::refresh) {
                    Icon(Icons.Rounded.Refresh, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.action_sync_now))
                }
                OutlinedButton(onClick = settingsViewModel::clearCache) {
                    Icon(Icons.Rounded.Delete, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_clear_cache))
                }
                Button(onClick = settingsViewModel::logout) {
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
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AgendaMode.entries.forEach { mode ->
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
private fun LanguageSelector(
    selectedLanguageTag: String?,
    onLanguageSelected: (String?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LanguageOption.entries.forEach { option ->
            FilterChip(
                selected = selectedLanguageTag == option.languageTag,
                onClick = { onLanguageSelected(option.languageTag) },
                label = { Text(stringResource(option.title)) }
            )
        }
    }
}

@Composable
private fun SwitchRow(
    @StringRes title: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(title),
            style = MaterialTheme.typography.bodyLarge
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AgendaEventCard(event: AgendaEvent) {
    CompactCard {
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
private fun GradeCard(grade: Grade) {
    CompactCard {
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
        }
        LabelValue(R.string.grades_coefficient, formatNumber(grade.coefficient))
        LabelValue(R.string.grades_average, formatNumber(grade.average))
        LabelValue(R.string.common_date, formatDate(grade.date))
    }
}

@Composable
private fun AbsenceCard(absence: Absence) {
    CompactCard {
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
        LabelValue(R.string.absences_reason, absence.reason.orEmpty())
    }
}

@Composable
private fun CourseCard(course: Course) {
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
        if (!course.syllabus.isNullOrBlank()) {
            HorizontalDivider()
            Text(course.syllabus)
        }
    }
}

@Composable
private fun ProjectCard(project: Project) {
    CompactCard {
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
        project.steps.forEach { step ->
            HorizontalDivider()
            Text(step.title.orUntitled(), fontWeight = FontWeight.Medium)
            LabelValue(R.string.projects_deadline, formatInstant(step.deadline))
            LabelValue(R.string.projects_status, step.status.orEmpty())
        }
    }
}

@Composable
private fun PracticalCard(practical: Practical) {
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
private fun DocumentCard(
    document: AcademicDocument,
    onOpen: () -> Unit
) {
    CompactCard {
        Text(
            text = document.title.orUntitled(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        LabelValue(R.string.documents_category, document.category.orEmpty())
        LabelValue(R.string.profile_year, document.year.orEmpty())
        LabelValue(R.string.documents_updated_at, formatInstant(document.updatedAt))
        OutlinedButton(onClick = onOpen) {
            Icon(Icons.Rounded.Download, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.documents_open))
        }
    }
}

@Composable
private fun NewsCard(newsItem: NewsItem) {
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

private fun List<AgendaEvent>.filterForMode(mode: AgendaMode): List<AgendaEvent> {
    val now = Instant.now()
    val today = LocalDate.now()
    return when (mode) {
        AgendaMode.Day -> filter { it.startsAt.atZone(ZoneId.systemDefault()).toLocalDate() == today }
        AgendaMode.Week -> filter { it.startsAt.isAfter(now.minus(1, ChronoUnit.HOURS)) && it.startsAt.isBefore(now.plus(7, ChronoUnit.DAYS)) }
        AgendaMode.List -> this
    }
}

private enum class AgendaMode(@StringRes val title: Int) {
    Day(R.string.agenda_mode_day),
    Week(R.string.agenda_mode_week),
    List(R.string.agenda_mode_list)
}

private enum class LanguageOption(@StringRes val title: Int, val languageTag: String?) {
    System(R.string.settings_language_system, null),
    French(R.string.settings_language_french, "fr"),
    English(R.string.settings_language_english, "en")
}
