package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import com.example.widget.T9BarWidgetProvider
import com.example.widget.T9WidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Central state holder and persistence manager for T9 Search themes.
 * Supports toggling between Glass (translucent/blurred), Opaque, and Material Design 3 styles,
 * sharp edges (0dp corners), custom corner radius, blur intensity, surface opacity,
 * border width, accent color, dark/light surface mode, and keypad ergonomics.
 */
class ThemeManager(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences = appContext.getSharedPreferences(
        T9WidgetProvider.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _themeState = MutableStateFlow(loadPersistedTheme())
    val themeState: StateFlow<ThemeConfig> = _themeState.asStateFlow()

    val currentTheme: ThemeConfig
        get() = _themeState.value

    private fun loadPersistedTheme(): ThemeConfig {
        val savedStyleName = prefs.getString(KEY_STYLE, T9ThemeStyle.GLASS.name)
        val style = try {
            T9ThemeStyle.valueOf(savedStyleName ?: T9ThemeStyle.GLASS.name)
        } catch (_: Exception) {
            T9ThemeStyle.GLASS
        }

        val hasCustomAccent = prefs.contains(KEY_ACCENT_ARGB)
        val customAccent = if (hasCustomAccent) prefs.getLong(KEY_ACCENT_ARGB, 0L) else null

        return ThemeConfig(
            style = style,
            opacity = prefs.getFloat(KEY_OPACITY, 0.88f).coerceIn(0.25f, 1.0f),
            blurRadiusDp = prefs.getFloat(KEY_BLUR_DP, 18f).coerceIn(0f, 24f),
            sharpEdges = prefs.getBoolean(KEY_SHARP_EDGES, false),
            cornerRadiusDp = prefs.getFloat(KEY_CORNER_DP, 16f).coerceIn(0f, 28f),
            borderWidthDp = prefs.getFloat(KEY_BORDER_DP, 1.2f).coerceIn(0f, 3f),
            customAccentArgb = customAccent,
            isDarkSurface = prefs.getBoolean(KEY_DARK_SURFACE, true),
            hapticEnabled = prefs.getBoolean(KEY_HAPTIC, true),
            compactKeypad = prefs.getBoolean(KEY_COMPACT, false)
        )
    }

    fun setThemeStyle(style: T9ThemeStyle) {
        updateAndPersist { current ->
            val defaultOpacity = when (style) {
                T9ThemeStyle.GLASS -> if (current.opacity > 0.94f) 0.85f else current.opacity
                T9ThemeStyle.OPAQUE -> 1.0f
                T9ThemeStyle.MATERIAL -> if (current.opacity < 0.85f) 0.95f else current.opacity
            }
            current.copy(style = style, opacity = defaultOpacity)
        }
        syncWidgets()
    }

    fun toggleThemeStyle() {
        val styles = T9ThemeStyle.values()
        val nextIndex = (styles.indexOf(_themeState.value.style) + 1) % styles.size
        setThemeStyle(styles[nextIndex])
    }

    fun setSharpEdges(enabled: Boolean) {
        updateAndPersist { current ->
            current.copy(
                sharpEdges = enabled,
                cornerRadiusDp = if (!enabled && current.cornerRadiusDp == 0f) 16f else current.cornerRadiusDp
            )
        }
        syncWidgets()
    }

    fun toggleSharpEdges() {
        setSharpEdges(!_themeState.value.sharpEdges)
    }

    fun setCornerRadius(radiusDp: Float) {
        val clamped = radiusDp.coerceIn(0f, 28f)
        val wasSharp = _themeState.value.sharpEdges
        val nowSharp = clamped == 0f
        updateAndPersist {
            it.copy(
                cornerRadiusDp = clamped,
                sharpEdges = nowSharp
            )
        }
        if (wasSharp != nowSharp) {
            syncWidgets()
        }
    }

    fun setOpacity(opacity: Float) {
        val clamped = opacity.coerceIn(0.25f, 1.0f)
        updateAndPersist { it.copy(opacity = clamped) }
    }

    fun setBlurRadius(blurDp: Float) {
        val clamped = blurDp.coerceIn(0f, 24f)
        updateAndPersist { it.copy(blurRadiusDp = clamped) }
    }

    fun setBorderWidth(widthDp: Float) {
        val clamped = widthDp.coerceIn(0f, 3f)
        updateAndPersist { it.copy(borderWidthDp = clamped) }
    }

    fun setCustomAccentColor(argb: Long?) {
        updateAndPersist { it.copy(customAccentArgb = argb) }
    }

    fun setDarkSurface(isDark: Boolean) {
        updateAndPersist { it.copy(isDarkSurface = isDark) }
    }

    fun toggleDarkSurface() {
        setDarkSurface(!_themeState.value.isDarkSurface)
    }

    fun toggleHaptic() {
        updateAndPersist { it.copy(hapticEnabled = !it.hapticEnabled) }
    }

    fun toggleCompactKeypad() {
        updateAndPersist { it.copy(compactKeypad = !it.compactKeypad) }
    }

    fun resetToDefaults() {
        _themeState.value = ThemeConfig()
        prefs.edit()
            .remove(KEY_STYLE)
            .remove(KEY_OPACITY)
            .remove(KEY_BLUR_DP)
            .remove(KEY_SHARP_EDGES)
            .remove(KEY_CORNER_DP)
            .remove(KEY_BORDER_DP)
            .remove(KEY_ACCENT_ARGB)
            .remove(KEY_DARK_SURFACE)
            .remove(KEY_HAPTIC)
            .remove(KEY_COMPACT)
            .apply()
        syncWidgets()
    }

    private inline fun updateAndPersist(transform: (ThemeConfig) -> ThemeConfig) {
        val updated = transform(_themeState.value)
        _themeState.value = updated
        prefs.edit().apply {
            putString(KEY_STYLE, updated.style.name)
            putFloat(KEY_OPACITY, updated.opacity)
            putFloat(KEY_BLUR_DP, updated.blurRadiusDp)
            putBoolean(KEY_SHARP_EDGES, updated.sharpEdges)
            putFloat(KEY_CORNER_DP, updated.cornerRadiusDp)
            putFloat(KEY_BORDER_DP, updated.borderWidthDp)
            if (updated.customAccentArgb != null) {
                putLong(KEY_ACCENT_ARGB, updated.customAccentArgb)
            } else {
                remove(KEY_ACCENT_ARGB)
            }
            putBoolean(KEY_DARK_SURFACE, updated.isDarkSurface)
            putBoolean(KEY_HAPTIC, updated.hapticEnabled)
            putBoolean(KEY_COMPACT, updated.compactKeypad)
        }.apply()
    }

    private fun syncWidgets() {
        try {
            T9WidgetProvider.updateAllWidgets(appContext)
            T9BarWidgetProvider.updateAllWidgets(appContext)
        } catch (_: Exception) {
            // Safe fallback in unit test environments
        }
    }

    companion object {
        const val KEY_STYLE = T9WidgetProvider.PREF_THEME
        const val KEY_SHARP_EDGES = "widget_sharp_edges"
        const val KEY_OPACITY = "theme_opacity"
        const val KEY_BLUR_DP = "theme_blur_dp"
        const val KEY_CORNER_DP = "theme_corner_dp"
        const val KEY_BORDER_DP = "theme_border_dp"
        const val KEY_ACCENT_ARGB = "theme_accent_argb"
        const val KEY_DARK_SURFACE = "theme_dark_surface"
        const val KEY_HAPTIC = "theme_haptic"
        const val KEY_COMPACT = "theme_compact"
    }
}
