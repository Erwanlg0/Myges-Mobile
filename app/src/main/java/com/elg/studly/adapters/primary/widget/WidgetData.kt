package com.elg.studly.adapters.primary.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.elg.studly.application.ports.StudentDataRepository
import com.elg.studly.domain.model.toGradeSummary
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun studentDataRepository(): StudentDataRepository
}

data class WidgetSnapshot(
    val nextCourseTitle: String?,
    val nextCourseWhen: String?,
    val nextCourseRoom: String?,
    val averageLabel: String?,
    val averageValue: String?,
    val gradedCount: Int,
    val deadlineTitle: String?,
    val deadlineWhen: String?,
    val unjustifiedAbsences: Int = 0,
    val newsTitle: String? = null,
    val newsWhen: String? = null
)

data class AgendaItem(
    val time: String,
    val title: String,
    val room: String?
)

object WidgetData {
    val EMPTY = WidgetSnapshot(null, null, null, null, null, 0, null, null)

    private val timeFormat = DateTimeFormatter.ofPattern("EEE dd/MM HH:mm", Locale.getDefault())
    private val deadlineFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.getDefault())
    private val newsFormat = DateTimeFormatter.ofPattern("dd/MM", Locale.getDefault())
    private val agendaItemFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

    private fun repository(context: Context): StudentDataRepository =
        EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .studentDataRepository()

    suspend fun load(context: Context): WidgetSnapshot {
        val repository = repository(context)

        val dashboard = repository.observeDashboard().first()
        val grades = repository.observeGrades().first()
        val absences = repository.observeAbsences().first()
        val news = repository.observeNews().first()
        val zone = ZoneId.systemDefault()
        val now = Instant.now()

        val latestNews = news.maxByOrNull { it.publishedAt ?: Instant.MIN }

        val event = dashboard.nextEvent
        val currentPeriod = grades
            .mapNotNull { it.period?.takeIf(String::isNotBlank) }
            .maxByOrNull(::periodRank)
        val periodGrades = if (currentPeriod != null) grades.filter { it.period == currentPeriod } else grades
        val summary = periodGrades.toGradeSummary()

        val latestAbsencePeriod = absences
            .mapNotNull { it.period?.takeIf(String::isNotBlank) }
            .maxByOrNull(::periodRank)
        val periodAbsences = if (latestAbsencePeriod != null) {
            absences.filter { it.period == latestAbsencePeriod }
        } else absences

        val project = dashboard.dueProjects.firstOrNull()
        val deadline = project?.let { p ->
            listOfNotNull(
                p.deadline?.takeIf { it.isAfter(now) },
                p.steps.mapNotNull { it.deadline }.filter { it.isAfter(now) }.minOrNull()
            ).minOrNull()
        }

        return WidgetSnapshot(
            nextCourseTitle = event?.title,
            nextCourseWhen = event?.startsAt?.atZone(zone)?.format(timeFormat),
            nextCourseRoom = event?.room?.takeIf(String::isNotBlank),
            averageLabel = currentPeriod,
            averageValue = summary.weightedAverage?.let { String.format(Locale.getDefault(), "%.2f", it) },
            gradedCount = summary.gradedCount,
            deadlineTitle = project?.name?.takeIf(String::isNotBlank),
            deadlineWhen = deadline?.atZone(zone)?.format(deadlineFormat),
            unjustifiedAbsences = periodAbsences.count { !it.justified },
            newsTitle = latestNews?.title?.takeIf(String::isNotBlank),
            newsWhen = latestNews?.publishedAt?.atZone(zone)?.format(newsFormat)
        )
    }

    suspend fun loadTodayAgenda(context: Context): List<AgendaItem> {
        val zone = ZoneId.systemDefault()
        val today = java.time.LocalDate.now(zone)
        return repository(context).observeAgenda().first()
            .filter { it.startsAt.atZone(zone).toLocalDate() == today }
            .sortedBy { it.startsAt }
            .map { event ->
                AgendaItem(
                    time = event.startsAt.atZone(zone).format(agendaItemFormat),
                    title = event.title,
                    room = event.room?.takeIf(String::isNotBlank)
                )
            }
    }

    // ponytail: mirrors comparePeriods in FeatureScreens (year, then last number); copied to keep widget code decoupled from UI.
    private fun periodRank(period: String): Long {
        val year = Regex("\\d{4}").find(period)?.value?.toIntOrNull() ?: 0
        val num = Regex("\\d+").findAll(period).mapNotNull { it.value.toIntOrNull() }.lastOrNull() ?: 0
        return year.toLong() * 100 + num
    }
}

object WidgetUpdater {
    private val providers = listOf(
        NextCourseWidget::class.java,
        AverageWidget::class.java,
        DeadlineWidget::class.java,
        SummaryWidget::class.java,
        AbsencesWidget::class.java,
        NewsWidget::class.java,
        AgendaWidget::class.java
    )

    fun refreshAll(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        providers.forEach { provider ->
            val ids = manager.getAppWidgetIds(ComponentName(context, provider))
            if (ids.isNotEmpty()) {
                context.sendBroadcast(
                    android.content.Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                        .setComponent(ComponentName(context, provider))
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                )
            }
        }
    }
}
