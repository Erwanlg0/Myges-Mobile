package com.elg.studly.adapters.primary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.elg.studly.MainActivity
import com.elg.studly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseStudlyWidget : AppWidgetProvider() {
    protected abstract val route: String

    protected abstract fun build(context: Context, views: RemoteViews, snapshot: WidgetSnapshot)
    protected abstract fun layout(): Int

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val snapshot = runCatching { WidgetData.load(context) }.getOrNull()
            try {
                ids.forEach { id ->
                    val views = RemoteViews(context.packageName, layout())
                    views.setOnClickPendingIntent(R.id.widget_root, openAppIntent(context))
                    if (snapshot != null) build(context, views, snapshot)
                    manager.updateAppWidget(id, views)
                }
            } finally {
                pending.finish()
            }
        }
    }

    private fun openAppIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra(MainActivity.EXTRA_NOTIFICATION_ROUTE, route)
        return PendingIntent.getActivity(
            context,
            route.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

private fun RemoteViews.setTextOrFallback(id: Int, value: String?, fallback: String) {
    setTextViewText(id, value ?: fallback)
}

private fun RemoteViews.setOptionalLine(id: Int, value: String?) {
    setViewVisibility(id, if (value.isNullOrBlank()) View.GONE else View.VISIBLE)
    if (!value.isNullOrBlank()) setTextViewText(id, value)
}

class NextCourseWidget : BaseStudlyWidget() {
    override val route = "agenda"
    override fun layout() = R.layout.widget_next_course
    override fun build(context: Context, views: RemoteViews, snapshot: WidgetSnapshot) {
        val none = context.getString(R.string.widget_no_course)
        views.setTextOrFallback(R.id.widget_primary, snapshot.nextCourseTitle, none)
        views.setOptionalLine(R.id.widget_secondary, snapshot.nextCourseWhen)
        views.setOptionalLine(R.id.widget_tertiary, snapshot.nextCourseRoom)
    }
}

class AverageWidget : BaseStudlyWidget() {
    override val route = "grades"
    override fun layout() = R.layout.widget_average
    override fun build(context: Context, views: RemoteViews, snapshot: WidgetSnapshot) {
        val value = snapshot.averageValue
        if (value != null) {
            views.setTextViewText(R.id.widget_primary, context.getString(R.string.widget_average_value, value))
            views.setOptionalLine(
                R.id.widget_secondary,
                context.resources.getQuantityString(R.plurals.widget_grades_count, snapshot.gradedCount, snapshot.gradedCount)
            )
            views.setOptionalLine(R.id.widget_tertiary, snapshot.averageLabel)
        } else {
            views.setTextViewText(R.id.widget_primary, context.getString(R.string.widget_no_grades))
            views.setViewVisibility(R.id.widget_secondary, View.GONE)
            views.setViewVisibility(R.id.widget_tertiary, View.GONE)
        }
    }
}

class DeadlineWidget : BaseStudlyWidget() {
    override val route = "projects"
    override fun layout() = R.layout.widget_deadline
    override fun build(context: Context, views: RemoteViews, snapshot: WidgetSnapshot) {
        val none = context.getString(R.string.widget_no_deadline)
        views.setTextOrFallback(R.id.widget_primary, snapshot.deadlineTitle, none)
        views.setOptionalLine(R.id.widget_secondary, snapshot.deadlineWhen)
    }
}

class SummaryWidget : BaseStudlyWidget() {
    override val route = "dashboard"
    override fun layout() = R.layout.widget_summary
    override fun build(context: Context, views: RemoteViews, snapshot: WidgetSnapshot) {
        views.setTextOrFallback(R.id.widget_course_value, courseLine(snapshot), context.getString(R.string.widget_no_course))
        val average = snapshot.averageValue
            ?.let { context.getString(R.string.widget_average_value, it) }
        views.setTextOrFallback(R.id.widget_average_value, average, context.getString(R.string.widget_no_grades))
        views.setTextOrFallback(
            R.id.widget_deadline_value,
            deadlineLine(snapshot),
            context.getString(R.string.widget_no_deadline)
        )
    }

    private fun courseLine(snapshot: WidgetSnapshot): String? {
        val title = snapshot.nextCourseTitle ?: return null
        return listOfNotNull(snapshot.nextCourseWhen, title).joinToString(" · ")
    }

    private fun deadlineLine(snapshot: WidgetSnapshot): String? {
        val title = snapshot.deadlineTitle ?: return null
        return listOfNotNull(snapshot.deadlineWhen, title).joinToString(" · ")
    }
}
