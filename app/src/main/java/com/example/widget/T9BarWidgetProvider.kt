package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.repository.SearchRepository
import com.example.engine.T9Engine

class T9BarWidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_BAR_BACKSPACE = "com.example.T9_BAR_BACKSPACE"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, T9BarWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in appWidgetIds) {
                updateBarWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateBarWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(T9WidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
            val currentQuery = prefs.getString(T9WidgetProvider.PREF_QUERY, "") ?: ""
            val themeName = prefs.getString("widget_theme", "GLASS") ?: "GLASS"
            val sharpEdges = prefs.getBoolean("widget_sharp_edges", false)

            val views = RemoteViews(context.packageName, R.layout.widget_t9_bar)

            // Select background style based on active theme and sharp edge setting
            val bgRes = if (sharpEdges) {
                when (themeName) {
                    "OPAQUE" -> R.drawable.widget_bg_opaque_sharp
                    "MATERIAL" -> R.drawable.widget_bg_material_sharp
                    else -> R.drawable.widget_bg_glass_sharp
                }
            } else {
                when (themeName) {
                    "OPAQUE" -> R.drawable.widget_bg_opaque
                    "MATERIAL" -> R.drawable.widget_bg_material
                    else -> R.drawable.widget_bg_glass
                }
            }
            views.setInt(R.id.widget_bar_root, "setBackgroundResource", bgRes)

            val btnBgRes = if (sharpEdges) R.drawable.widget_key_bg_sharp else R.drawable.widget_key_bg
            views.setInt(R.id.widget_bar_backspace, "setBackgroundResource", btnBgRes)
            views.setInt(R.id.widget_bar_launch, "setBackgroundResource", btnBgRes)

            // Update text & preview
            if (currentQuery.isEmpty()) {
                views.setTextViewText(R.id.widget_bar_query, "T9 Phone Search")
                views.setTextViewText(R.id.widget_bar_preview, "Search contacts, apps, files & settings")
            } else {
                views.setTextViewText(R.id.widget_bar_query, currentQuery)
                val repo = SearchRepository(context)
                val matches = repo.search(currentQuery)
                val topApp = matches.filterIsInstance<com.example.data.model.SearchItem.AppItem>().firstOrNull()
                    ?: matches.firstOrNull { it !is com.example.data.model.SearchItem.ContactItem }
                val topMatchTitle = topApp?.title ?: "No matching app"
                val suggestions = T9Engine.getPredictiveSuggestions(currentQuery, 1)
                val predText = if (suggestions.isNotEmpty()) " (→ ${suggestions.first()})" else ""
                views.setTextViewText(R.id.widget_bar_preview, "$topMatchTitle$predText")
            }

            // Click search bar text -> open popup search overlay
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_QUERY", currentQuery)
                putExtra("EXTRA_FROM_WIDGET", true)
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                201,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_root, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_bar_text_container, openAppPendingIntent)

            // Click Enter button (↵) -> open first match directly!
            val openFirstMatchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_QUERY", currentQuery)
                putExtra("EXTRA_FROM_WIDGET", true)
                putExtra("EXTRA_OPEN_FIRST_MATCH", true)
            }
            val openFirstMatchPendingIntent = PendingIntent.getActivity(
                context,
                203,
                openFirstMatchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_launch, openFirstMatchPendingIntent)

            // Backspace click
            val backspaceIntent = Intent(context, T9BarWidgetProvider::class.java).apply {
                action = ACTION_BAR_BACKSPACE
            }
            val backspacePendingIntent = PendingIntent.getBroadcast(
                context,
                202,
                backspaceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_backspace, backspacePendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateBarWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = context.getSharedPreferences(T9WidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getString(T9WidgetProvider.PREF_QUERY, "") ?: ""

        if (intent.action == ACTION_BAR_BACKSPACE) {
            if (current.isNotEmpty()) {
                prefs.edit().putString(T9WidgetProvider.PREF_QUERY, current.dropLast(1)).apply()
                updateAllWidgets(context)
                T9WidgetProvider.updateAllWidgets(context)
            }
        }
    }
}
