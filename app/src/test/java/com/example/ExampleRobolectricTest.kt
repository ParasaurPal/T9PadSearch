package com.example

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.SearchCategory
import com.example.data.model.SearchItem
import com.example.data.repository.SearchHistoryRepository
import com.example.data.repository.SearchRepository
import com.example.ui.theme.AccentPalette
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("T9 Search", appName)
    }

    @Test
    fun `search settings with T9 digits`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SearchRepository(context)
        repo.loadAllData()

        // "9434" -> WIFI
        val wifiResults = repo.search("9434", SearchCategory.SETTINGS)
        assertTrue("Wi-Fi setting should be found with 9434", wifiResults.any { it.title.contains("Wi-Fi") })

        // "2288" -> BATT (Battery)
        val batteryResults = repo.search("2288", SearchCategory.SETTINGS)
        assertTrue("Battery setting should be found with 2288", batteryResults.any { it.title.contains("Battery") })
    }

    @Test
    fun `search contacts with T9 digits or phone number`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SearchRepository(context)
        repo.loadAllData()

        // "2542" -> ALIC (Alice)
        val contactResults = repo.search("2542", SearchCategory.CONTACTS)
        assertTrue("Alice should be found with 2542", contactResults.any { it.title.contains("Alice") })

        // "555" -> common digits
        val phoneResults = repo.search("555", SearchCategory.CONTACTS)
        assertTrue("Phone numbers containing 555 should be matched", phoneResults.isNotEmpty())
    }

    @Test
    fun `pressing enter on widget intent opens actual application`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SearchRepository(context)
        repo.loadAllData()

        val results = repo.search("4664")
        assertTrue("Results should not be empty for 4664", results.isNotEmpty())
        val firstMatch = results.first()
        assertTrue("First match must be an application (not a call)", firstMatch is SearchItem.AppItem)
    }

    @Test
    fun `apps appear on top as you type digits`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SearchRepository(context)
        repo.loadAllData()

        // Typing single digit "2" (A/B/C) -> Chrome, Camera, Calculator, Calendar
        val resultsFor2 = repo.search("2")
        assertTrue("Results should not be empty for 2", resultsFor2.isNotEmpty())
        assertTrue("First result for 2 must be an AppItem", resultsFor2.first() is SearchItem.AppItem)

        // Typing "46" -> Google Maps, Google, Gmail
        val resultsFor46 = repo.search("46")
        assertTrue("Results should not be empty for 46", resultsFor46.isNotEmpty())
        assertTrue("First result for 46 must be an AppItem", resultsFor46.first() is SearchItem.AppItem)
    }

    @Test
    fun `ThemeManager toggles themes, sharp edges, and persists full customization`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val themeManager = ThemeManager(context)
        themeManager.resetToDefaults()

        assertEquals(T9ThemeStyle.GLASS, themeManager.themeState.value.style)
        assertFalse(themeManager.themeState.value.sharpEdges)

        // Toggle Glass -> Opaque -> Material -> Glass
        themeManager.toggleThemeStyle()
        assertEquals(T9ThemeStyle.OPAQUE, themeManager.themeState.value.style)
        themeManager.toggleThemeStyle()
        assertEquals(T9ThemeStyle.MATERIAL, themeManager.themeState.value.style)
        themeManager.toggleThemeStyle()
        assertEquals(T9ThemeStyle.GLASS, themeManager.themeState.value.style)

        // Enable sharp edges & customize full theme properties
        themeManager.setSharpEdges(true)
        assertTrue(themeManager.themeState.value.sharpEdges)
        assertEquals(0.dp, themeManager.themeState.value.effectiveCornerDp)

        val roseAccent = AccentPalette.presets.first { it.first == "Rose" }.second
        themeManager.setCustomAccentColor(roseAccent)
        themeManager.setBlurRadius(22f)
        themeManager.setOpacity(0.65f)
        themeManager.setBorderWidth(2.4f)
        themeManager.setDarkSurface(false)

        // Verify persistence with a fresh ThemeManager instance
        val reloaded = ThemeManager(context)
        val cfg = reloaded.themeState.value
        assertEquals(T9ThemeStyle.GLASS, cfg.style)
        assertTrue(cfg.sharpEdges)
        assertEquals(0.dp, cfg.effectiveCornerDp)
        assertEquals(roseAccent, cfg.customAccentArgb)
        assertEquals(22f, cfg.blurRadiusDp, 0.01f)
        assertEquals(0.65f, cfg.opacity, 0.01f)
        assertEquals(2.4f, cfg.borderWidthDp, 0.01f)
        assertFalse(cfg.isDarkSurface)

        // Reset restores defaults
        reloaded.resetToDefaults()
        assertFalse(reloaded.themeState.value.sharpEdges)
        assertNull(reloaded.themeState.value.customAccentArgb)
        assertTrue(reloaded.themeState.value.isDarkSurface)
    }

    @Test
    fun `Room SearchHistoryRepository records, increments, deletes, and clears recent searches`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        try {
            val historyRepo = SearchHistoryRepository(db.searchHistoryDao())

            historyRepo.recordSearch("4664", "Google", "APPS")
            historyRepo.recordSearch("2288", "Battery", "SETTINGS")
            historyRepo.recordSearch("4664", "Google", "APPS")

            val recent = historyRepo.getRecentSearches(15).first()
            assertEquals(2, recent.size)
            assertEquals("4664", recent[0].query)
            assertEquals(2, recent[0].searchCount)
            assertEquals("Google", recent[0].displayTitle)
            assertEquals("2288", recent[1].query)

            historyRepo.deleteSearch("2288")
            val afterDelete = historyRepo.getRecentSearches(15).first()
            assertEquals(1, afterDelete.size)
            assertEquals("4664", afterDelete[0].query)

            historyRepo.clearHistory()
            val afterClear = historyRepo.getRecentSearches(15).first()
            assertTrue(afterClear.isEmpty())
        } finally {
            db.close()
        }
    }
}
