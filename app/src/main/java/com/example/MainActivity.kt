package com.example

import android.app.SearchManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.local.AppDatabase
import com.example.data.model.SearchItem
import com.example.data.repository.SearchHistoryRepository
import com.example.data.repository.SearchRepository
import com.example.ui.T9SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ThemeManager
import com.example.widget.T9BarWidgetProvider
import com.example.widget.T9WidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private var initialQuery by mutableStateOf<String?>(null)
    private var isFromWidget by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (handleIntent(intent)) {
            return
        }

        enableEdgeToEdge()
        val initialDark = ThemeManager(applicationContext).currentTheme.isDarkSurface

        setContent {
            MyApplicationTheme(darkTheme = initialDark) {
                T9SearchScreen(
                    initialQuery = initialQuery,
                    openedFromWidget = isFromWidget,
                    onDismissOverlay = { finish() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (handleIntent(intent)) {
            return
        }
    }

    private fun handleIntent(intent: Intent?): Boolean {
        if (intent == null) return false
        val queryFromExtra = intent.getStringExtra("EXTRA_QUERY")
        val queryFromSearch = intent.getStringExtra(SearchManager.QUERY)
        val shouldOpenFirstMatch = intent.getBooleanExtra("EXTRA_OPEN_FIRST_MATCH", false)
        initialQuery = queryFromExtra ?: queryFromSearch
        isFromWidget = intent.getBooleanExtra("EXTRA_FROM_WIDGET", false)

        if (shouldOpenFirstMatch) {
            val queryToSearch = initialQuery ?: ""
            val repo = SearchRepository(applicationContext)
            val results = repo.search(queryToSearch)

            // Specifically prioritize opening actual installed applications on the phone!
            val targetApp = results.filterIsInstance<SearchItem.AppItem>().firstOrNull()
                ?: results.firstOrNull { it !is SearchItem.ContactItem && it !is SearchItem.ActionItem }
                ?: results.firstOrNull()

            if (targetApp != null) {
                // Record search query into local Room history database
                if (queryToSearch.isNotBlank()) {
                    val historyRepo = SearchHistoryRepository(
                        AppDatabase.getInstance(applicationContext).searchHistoryDao()
                    )
                    runBlocking(Dispatchers.IO) {
                        historyRepo.recordSearch(
                            query = queryToSearch,
                            displayTitle = targetApp.title,
                            category = targetApp.category.name
                        )
                    }
                }

                // Clear widget query for subsequent searches
                val prefs = getSharedPreferences(T9WidgetProvider.PREFS_NAME, MODE_PRIVATE)
                prefs.edit().putString(T9WidgetProvider.PREF_QUERY, "").apply()
                T9WidgetProvider.updateAllWidgets(applicationContext)
                T9BarWidgetProvider.updateAllWidgets(applicationContext)

                when (targetApp) {
                    is SearchItem.AppItem -> repo.launchApp(targetApp)
                    is SearchItem.SettingItem -> repo.openSetting(targetApp)
                    is SearchItem.FileItem -> repo.openFile(targetApp)
                    else -> {}
                }
                finish()
                return true
            }
        }
        return false
    }
}
