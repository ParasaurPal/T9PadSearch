package com.example.ui.viewmodel

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SearchHistoryEntity
import com.example.data.model.SearchCategory
import com.example.data.model.SearchItem
import com.example.data.repository.SearchHistoryRepository
import com.example.data.repository.SearchRepository
import com.example.engine.T9Engine
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeConfig
import com.example.ui.theme.ThemeManager
import com.example.widget.T9BarWidgetProvider
import com.example.widget.T9WidgetProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class WidgetPreviewFormat(val label: String, val sizeInfo: String) {
    PAD_4X3("Full T9 Pad", "4 × 3 Keypad"),
    BAR_4X1("Slim Search Bar", "4 × 1 Bar")
}

data class T9UiState(
    val query: String = "",
    val selectedCategory: SearchCategory = SearchCategory.ALL,
    val searchResults: List<SearchItem> = emptyList(),
    val predictiveWords: List<String> = emptyList(),
    val themeConfig: ThemeConfig = ThemeConfig(),
    val isQwertyMode: Boolean = false,
    val showThemeSheet: Boolean = false,
    val showWidgetPreviewSheet: Boolean = false,
    val isLoading: Boolean = false,
    val contactsPermissionGranted: Boolean = false,
    val categoryCounts: Map<SearchCategory, Int> = emptyMap(),
    val isFromWidgetOverlay: Boolean = false,
    val selectedWidgetFormat: WidgetPreviewFormat = WidgetPreviewFormat.PAD_4X3
)

class T9ViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SearchRepository(application)
    val themeManager = ThemeManager(application)
    private val historyRepository = SearchHistoryRepository(
        AppDatabase.getInstance(application).searchHistoryDao()
    )

    private val _uiState = MutableStateFlow(
        T9UiState(themeConfig = themeManager.currentTheme)
    )
    val uiState: StateFlow<T9UiState> = _uiState.asStateFlow()

    val recentSearches: StateFlow<List<SearchHistoryEntity>> =
        historyRepository.getRecentSearches(15)
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            themeManager.themeState.collect { config ->
                _uiState.update { it.copy(themeConfig = config) }
            }
        }
        loadData()
    }

    fun setIsFromWidget(fromWidget: Boolean) {
        _uiState.update { it.copy(isFromWidgetOverlay = fromWidget) }
    }

    fun setSelectedWidgetFormat(format: WidgetPreviewFormat) {
        _uiState.update { it.copy(selectedWidgetFormat = format) }
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.loadAllData()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    contactsPermissionGranted = repository.isContactsPermissionGranted
                )
            }
            executeSearch()
        }
    }

    fun appendDigit(digit: Char) {
        val current = _uiState.value.query
        if (current.length < 25) {
            val newQuery = current + digit
            _uiState.update { it.copy(query = newQuery) }
            syncQueryToWidgets(newQuery)
            executeSearch()
        }
    }

    fun backspace() {
        val current = _uiState.value.query
        if (current.isNotEmpty()) {
            val newQuery = current.dropLast(1)
            _uiState.update { it.copy(query = newQuery) }
            syncQueryToWidgets(newQuery)
            executeSearch()
        }
    }

    fun clearQuery() {
        _uiState.update { it.copy(query = "") }
        syncQueryToWidgets("")
        executeSearch()
    }

    fun setQuery(newQuery: String) {
        _uiState.update { it.copy(query = newQuery) }
        syncQueryToWidgets(newQuery)
        executeSearch()
    }

    private fun syncQueryToWidgets(query: String) {
        val context = getApplication<Application>()
        val prefs = context.getSharedPreferences(T9WidgetProvider.PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(T9WidgetProvider.PREF_QUERY, query).apply()
        T9WidgetProvider.updateAllWidgets(context)
        T9BarWidgetProvider.updateAllWidgets(context)
    }

    fun selectPredictiveWord(word: String) {
        val t9Code = T9Engine.textToT9(word)
        _uiState.update { it.copy(query = t9Code) }
        syncQueryToWidgets(t9Code)
        executeSearch()
    }

    fun selectCategory(category: SearchCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        executeSearch()
    }

    fun toggleQwertyMode() {
        _uiState.update { it.copy(isQwertyMode = !it.isQwertyMode) }
    }

    // ThemeManager delegations
    fun setThemeStyle(style: T9ThemeStyle) = themeManager.setThemeStyle(style)
    fun toggleThemeStyle() = themeManager.toggleThemeStyle()
    fun setSharpEdges(enabled: Boolean) = themeManager.setSharpEdges(enabled)
    fun toggleSharpEdges() = themeManager.toggleSharpEdges()
    fun setCornerRadius(radiusDp: Float) = themeManager.setCornerRadius(radiusDp)
    fun setOpacity(opacity: Float) = themeManager.setOpacity(opacity)
    fun setBlurRadius(blurDp: Float) = themeManager.setBlurRadius(blurDp)
    fun setBorderWidth(widthDp: Float) = themeManager.setBorderWidth(widthDp)
    fun setCustomAccentColor(argb: Long?) = themeManager.setCustomAccentColor(argb)
    fun setDarkSurface(isDark: Boolean) = themeManager.setDarkSurface(isDark)
    fun toggleDarkSurface() = themeManager.toggleDarkSurface()
    fun toggleHaptic() = themeManager.toggleHaptic()
    fun toggleCompactKeypad() = themeManager.toggleCompactKeypad()
    fun resetThemeDefaults() = themeManager.resetToDefaults()

    fun setShowThemeSheet(show: Boolean) {
        _uiState.update { it.copy(showThemeSheet = show) }
    }

    fun setShowWidgetPreviewSheet(show: Boolean) {
        _uiState.update { it.copy(showWidgetPreviewSheet = show) }
    }

    fun onContactsPermissionResult(granted: Boolean) {
        repository.loadContacts()
        _uiState.update {
            it.copy(contactsPermissionGranted = granted || repository.isContactsPermissionGranted)
        }
        executeSearch()
    }

    fun pinWidgetToHomeScreen(format: WidgetPreviewFormat) {
        val context = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val providerClass = when (format) {
                WidgetPreviewFormat.PAD_4X3 -> T9WidgetProvider::class.java
                WidgetPreviewFormat.BAR_4X1 -> T9BarWidgetProvider::class.java
            }
            val myProvider = ComponentName(context, providerClass)
            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
                Toast.makeText(context, "Adding ${format.label} to home screen…", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    context,
                    "Touch and hold empty space on your Home screen, tap Widgets, and add ${format.label}",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Touch and hold empty space on your Home screen, tap Widgets, and drag ${format.label}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun executeSearch() {
        val q = _uiState.value.query
        val cat = _uiState.value.selectedCategory
        val results = repository.search(q, cat)
        val predictive = T9Engine.getPredictiveSuggestions(q)

        val allResults = if (cat == SearchCategory.ALL) results else repository.search(q, SearchCategory.ALL)
        val counts = mutableMapOf<SearchCategory, Int>()
        counts[SearchCategory.ALL] = allResults.size
        counts[SearchCategory.APPS] = allResults.count { it.category == SearchCategory.APPS }
        counts[SearchCategory.CONTACTS] = allResults.count { it.category == SearchCategory.CONTACTS }
        counts[SearchCategory.FILES] = allResults.count { it.category == SearchCategory.FILES }
        counts[SearchCategory.SETTINGS] = allResults.count { it.category == SearchCategory.SETTINGS }

        _uiState.update {
            it.copy(
                searchResults = results,
                predictiveWords = predictive,
                categoryCounts = counts
            )
        }
    }

    // Room search history operations
    private fun recordCurrentSearch(displayTitle: String, category: SearchCategory) {
        val currentQuery = _uiState.value.query.trim()
        if (currentQuery.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.recordSearch(
                query = currentQuery,
                displayTitle = displayTitle,
                category = category.name
            )
        }
    }

    fun applyRecentSearch(entry: SearchHistoryEntity) {
        _uiState.update { it.copy(query = entry.query) }
        syncQueryToWidgets(entry.query)
        executeSearch()
    }

    fun deleteRecentSearch(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.deleteSearch(query)
        }
    }

    fun clearRecentSearches() {
        viewModelScope.launch(Dispatchers.IO) {
            historyRepository.clearHistory()
        }
    }

    fun openFirstMatch(item: SearchItem? = null) {
        val target = item
            ?: _uiState.value.searchResults.filterIsInstance<SearchItem.AppItem>().firstOrNull()
            ?: _uiState.value.searchResults.firstOrNull { it !is SearchItem.ContactItem && it !is SearchItem.ActionItem }
            ?: return
        when (target) {
            is SearchItem.AppItem -> launchApp(target)
            is SearchItem.SettingItem -> openSetting(target)
            is SearchItem.FileItem -> openFile(target)
            else -> {}
        }
        clearQuery()
    }

    fun launchApp(item: SearchItem.AppItem) {
        recordCurrentSearch(item.title, item.category)
        repository.launchApp(item)
    }

    fun openAppDetails(item: SearchItem.AppItem) {
        recordCurrentSearch(item.title, item.category)
        repository.openAppDetails(item)
    }

    fun dialContact(item: SearchItem.ContactItem) {
        recordCurrentSearch(item.title, item.category)
        repository.dialContact(item)
    }

    fun smsContact(item: SearchItem.ContactItem) {
        recordCurrentSearch(item.title, item.category)
        repository.smsContact(item)
    }

    fun openFile(item: SearchItem.FileItem) {
        recordCurrentSearch(item.title, item.category)
        repository.openFile(item)
    }

    fun openSetting(item: SearchItem.SettingItem) {
        recordCurrentSearch(item.title, item.category)
        repository.openSetting(item)
    }

    fun executeAction(item: SearchItem.ActionItem) {
        recordCurrentSearch(item.title, item.category)
        repository.executeAction(item)
    }
}
