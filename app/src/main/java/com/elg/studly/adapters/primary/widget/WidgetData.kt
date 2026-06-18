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
    val deadlineWhen: String?
)

object WidgetData {
    private val timeFormat = DateTimeFormatter.ofPattern("EEE dd/MM HH:mm", Locale.getDefault())
    private val deadlineFormat = DateTimeFormatter.ofPattern("dd/MM HH:mm", Locale.getDefault())

    suspend fun load(context: Context): WidgetSnapshot {
        val repository = EntryPointAccessors
            .fromApplication(context.applicationContext, WidgetEntryPoint::class.java)
            .studentDataRepository()

        val dashboard = repository.observeDashboard().first()
        val grades = repository.observeGrades().first()
        val zone = ZoneId.systemDefault()
        val now = Instant.now()

        val event = dashboard.nextEvent
        val currentPeriod = grades
            .mapNotNull { it.period?.takeIf(String::isNotBlank) }
            .maxByOrNull(::periodRank)
        val periodGrades = if (currentPeriod != null) grades.filter { it.period == currentPeriod } else grades
        val summary = periodGrades.toGradeSummary()

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
            deadlineWhen = deadline?.atZone(zone)?.format(deadlineFormat)
        )
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
        SummaryWidget::class.java
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
