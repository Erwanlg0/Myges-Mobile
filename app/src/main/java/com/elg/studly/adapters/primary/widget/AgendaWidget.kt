package com.elg.studly.adapters.primary.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.elg.studly.MainActivity
import com.elg.studly.R
import kotlinx.coroutines.runBlocking

class AgendaWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { id ->
            val views = RemoteViews(context.packageName, R.layout.widget_agenda)
            val serviceIntent = Intent(context, AgendaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.widget_agenda_list, serviceIntent)
            views.setEmptyView(R.id.widget_agenda_list, R.id.widget_agenda_empty)

            val open = PendingIntent.getActivity(
                context,
                "agenda".hashCode(),
                Intent(context, MainActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(MainActivity.EXTRA_NOTIFICATION_ROUTE, "agenda"),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_agenda_list, open)
            views.setOnClickPendingIntent(R.id.widget_agenda_header, open)
            manager.updateAppWidget(id, views)
        }
        manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_agenda_list)
    }
}

class AgendaWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory =
        AgendaRemoteViewsFactory(applicationContext)
}

private class AgendaRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {
    private var items = emptyList<AgendaItem>()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        items = runCatching { runBlocking { WidgetData.loadTodayAgenda(context) } }.getOrDefault(emptyList())
    }

    override fun onDestroy() {
        items = emptyList()
    }

    override fun getCount() = items.size

    override fun getViewAt(position: Int): RemoteViews {
        val item = items[position]
        return RemoteViews(context.packageName, R.layout.widget_agenda_item).apply {
            setTextViewText(R.id.widget_agenda_item_time, item.time)
            setTextViewText(R.id.widget_agenda_item_title, item.title)
            if (item.room.isNullOrBlank()) {
                setViewVisibility(R.id.widget_agenda_item_room, View.GONE)
            } else {
                setViewVisibility(R.id.widget_agenda_item_room, View.VISIBLE)
                setTextViewText(R.id.widget_agenda_item_room, item.room)
            }
            setOnClickFillInIntent(R.id.widget_agenda_item_root, Intent())
        }
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true
}
