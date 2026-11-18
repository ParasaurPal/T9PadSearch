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

class T9WidgetProvider : AppWidgetProvider() {

    companion object {
        const val ACTION_KEY_PRESSED = "com.example.T9_KEY_PRESSED"
        const val ACTION_CLEAR_PRESSED = "com.example.T9_CLEAR_PRESSED"
        const val ACTION_BACKSPACE_PRESSED = "com.example.T9_BACKSPACE_PRESSED"
        const val EXTRA_DIGIT = "extra_digit"
        const val PREFS_NAME = "t9_widget_prefs"
        const val PREF_QUERY = "widget_query"
        const val PREF_THEME = "widget_theme"

        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val thisWidget = ComponentName(context, T9WidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget)
            for (widgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }
        }

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val currentQuery = prefs.getString(PREF_QUERY, "") ?: ""
            val themeName = prefs.getString(PREF_THEME, "GLASS") ?: "GLASS"
            val sharpEdges = prefs.getBoolean("widget_sharp_edges", false)

            val views = RemoteViews(context.packageName, R.layout.widget_t9_search)

            // Dynamic theme background for the widget (rounded vs sharp edges)
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
            views.setInt(R.id.widget_root, "setBackgroundResource", bgRes)

            val keyBgRes = if (sharpEdges) R.drawable.widget_key_bg_sharp else R.drawable.widget_key_bg
            views.setInt(R.id.widget_search_bar, "setBackgroundResource", keyBgRes)
            listOf(
                R.id.widget_key_1, R.id.widget_key_2, R.id.widget_key_3,
                R.id.widget_key_4, R.id.widget_key_5, R.id.widget_key_6,
                R.id.widget_key_7, R.id.widget_key_8, R.id.widget_key_9,
                R.id.widget_key_clear, R.id.widget_key_0, R.id.widget_key_search
            ).forEach { keyId ->
                views.setInt(keyId, "setBackgroundResource", keyBgRes)
            }

            // Update Search Bar display
            if (currentQuery.isEmpty()) {
                views.setTextViewText(R.id.widget_query_text, "T9 Quick Search")
                views.setTextViewText(R.id.widget_result_preview, "Tap digits to search phone")
            } else {
                views.setTextViewText(R.id.widget_query_text, currentQuery)
                val suggestions = T9Engine.getPredictiveSuggestions(currentQuery, 1)
                val predText = if (suggestions.isNotEmpty()) " → ${suggestions.first()}" else ""

                // Get quick top match application title
                val repo = SearchRepository(context)
                val matches = repo.search(currentQuery)
                val topApp = matches.filterIsInstance<com.example.data.model.SearchItem.AppItem>().firstOrNull()
                    ?: matches.firstOrNull { it !is com.example.data.model.SearchItem.ContactItem }
                val topMatchTitle = topApp?.title ?: "No matching app"
                views.setTextViewText(R.id.widget_result_preview, "$topMatchTitle$predText")
            }

            // Bind click to open search overlay from widget (search bar area)
            val openOverlayIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_QUERY", currentQuery)
                putExtra("EXTRA_FROM_WIDGET", true)
            }
            val openOverlayPendingIntent = PendingIntent.getActivity(
                context,
                0,
                openOverlayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_search_bar, openOverlayPendingIntent)

            // Bind Enter key (and direct open button) to open the first match immediately!
            val openFirstMatchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("EXTRA_QUERY", currentQuery)
                putExtra("EXTRA_FROM_WIDGET", true)
                putExtra("EXTRA_OPEN_FIRST_MATCH", true)
            }
            val openFirstMatchPendingIntent = PendingIntent.getActivity(
                context,
                1,
                openFirstMatchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_key_search, openFirstMatchPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_btn_open_app, openFirstMatchPendingIntent)

            // Bind Backspace
            val backspaceIntent = Intent(context, T9WidgetProvider::class.java).apply {
                action = ACTION_BACKSPACE_PRESSED
            }
            val backspacePendingIntent = PendingIntent.getBroadcast(
                context,
                100,
                backspaceIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_btn_backspace, backspacePendingIntent)

            // Bind Clear
            val clearIntent = Intent(context, T9WidgetProvider::class.java).apply {
                action = ACTION_CLEAR_PRESSED
            }
            val clearPendingIntent = PendingIntent.getBroadcast(
                context,
                101,
                clearIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_key_clear, clearPendingIntent)

            // Bind T9 Digits
            val keyMap = mapOf(
                R.id.widget_key_1 to "1",
                R.id.widget_key_2 to "2",
                R.id.widget_key_3 to "3",
                R.id.widget_key_4 to "4",
                R.id.widget_key_5 to "5",
                R.id.widget_key_6 to "6",
                R.id.widget_key_7 to "7",
                R.id.widget_key_8 to "8",
                R.id.widget_key_9 to "9",
                R.id.widget_key_0 to "0"
            )

            for ((viewId, digit) in keyMap) {
                val keyIntent = Intent(context, T9WidgetProvider::class.java).apply {
                    action = ACTION_KEY_PRESSED
                    putExtra(EXTRA_DIGIT, digit)
                }
                val keyPendingIntent = PendingIntent.getBroadcast(
                    context,
                    viewId,
                    keyIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(viewId, keyPendingIntent)
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getString(PREF_QUERY, "") ?: ""

        when (intent.action) {
            ACTION_KEY_PRESSED -> {
                val digit = intent.getStringExtra(EXTRA_DIGIT) ?: ""
                if (digit.isNotEmpty() && current.length < 20) {
                    prefs.edit().putString(PREF_QUERY, current + digit).apply()
                    updateAllWidgets(context)
                    T9BarWidgetProvider.updateAllWidgets(context)
                }
            }
            ACTION_BACKSPACE_PRESSED -> {
                if (current.isNotEmpty()) {
                    prefs.edit().putString(PREF_QUERY, current.dropLast(1)).apply()
                    updateAllWidgets(context)
                    T9BarWidgetProvider.updateAllWidgets(context)
                }
            }
            ACTION_CLEAR_PRESSED -> {
                prefs.edit().putString(PREF_QUERY, "").apply()
                updateAllWidgets(context)
                T9BarWidgetProvider.updateAllWidgets(context)
            }
        }
    }
}
