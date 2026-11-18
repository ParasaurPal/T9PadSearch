package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class T9ThemeStyle(val displayName: String, val description: String) {
    GLASS(
        "Glass",
        "Translucent frosted glass with backdrop blur and luminous borders"
    ),
    OPAQUE(
        "Opaque",
        "Solid high-contrast OLED surface with zero translucency"
    ),
    MATERIAL(
        "Material 3",
        "Material Design 3 tonal surfaces with expressive elevation"
    )
}

object AccentPalette {
    val presets: List<Pair<String, Long>> = listOf(
        "Cyan" to 0xFF38BDF8L,
        "Emerald" to 0xFF10B981L,
        "Indigo" to 0xFF6366F1L,
        "Rose" to 0xFFF43F5EL,
        "Amber" to 0xFFF59E0BL,
        "Violet" to 0xFFA855F7L,
        "Teal" to 0xFF14B8A6L,
        "Mono" to 0xFFE2E8F0L
    )
}

data class ThemeConfig(
    val style: T9ThemeStyle = T9ThemeStyle.GLASS,
    val opacity: Float = 0.85f,
    val blurRadiusDp: Float = 18f,
    val sharpEdges: Boolean = false,
    val cornerRadiusDp: Float = 16f,
    val borderWidthDp: Float = 1.2f,
    val customAccentArgb: Long? = null,
    val isDarkSurface: Boolean = true,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val compactKeypad: Boolean = false
) {
    val effectiveCornerDp: Dp
        get() = if (sharpEdges) 0.dp else cornerRadiusDp.coerceIn(0f, 28f).dp

    fun getContainerShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else RoundedCornerShape((cornerRadiusDp * 1.4f).coerceIn(0f, 32f).dp)

    fun getCardShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else RoundedCornerShape(cornerRadiusDp.coerceIn(0f, 28f).dp)

    fun getSearchBarShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else {
            val base = when (style) {
                T9ThemeStyle.GLASS -> cornerRadiusDp * 1.2f
                T9ThemeStyle.OPAQUE -> cornerRadiusDp * 0.9f
                T9ThemeStyle.MATERIAL -> cornerRadiusDp * 1.6f
            }
            RoundedCornerShape(base.coerceIn(0f, 32f).dp)
        }

    fun getKeyShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else {
            val base = when (style) {
                T9ThemeStyle.GLASS -> cornerRadiusDp
                T9ThemeStyle.OPAQUE -> cornerRadiusDp * 0.75f
                T9ThemeStyle.MATERIAL -> cornerRadiusDp * 1.4f
            }
            RoundedCornerShape(base.coerceIn(0f, 28f).dp)
        }

    fun getChipShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else RoundedCornerShape((cornerRadiusDp * 0.8f).coerceIn(0f, 24f).dp)

    fun getBadgeShape(): Shape =
        if (sharpEdges) RoundedCornerShape(0.dp)
        else RoundedCornerShape((cornerRadiusDp * 0.45f).coerceIn(0f, 12f).dp)

    fun getEffectiveBorderWidth(): Dp = borderWidthDp.coerceIn(0f, 3f).dp

    fun getBackgroundColor(): Color {
        return when (style) {
            T9ThemeStyle.GLASS -> {
                if (isDarkSurface) GlassBgGradientStart.copy(alpha = opacity)
                else Color(0xFFF1F5F9).copy(alpha = opacity)
            }
            T9ThemeStyle.OPAQUE -> {
                if (isDarkSurface) OpaqueBg else Color(0xFFFFFFFF)
            }
            T9ThemeStyle.MATERIAL -> {
                if (isDarkSurface) MaterialBg.copy(alpha = opacity.coerceAtLeast(0.85f))
                else Color(0xFFF8FAFC).copy(alpha = opacity.coerceAtLeast(0.90f))
            }
        }
    }

    fun getBackgroundBrush(): Brush {
        return when (style) {
            T9ThemeStyle.GLASS -> {
                if (isDarkSurface) {
                    Brush.verticalGradient(
                        colors = listOf(
                            GlassBgGradientStart.copy(alpha = (opacity * 0.92f).coerceIn(0.25f, 0.98f)),
                            GlassBgGradientEnd.copy(alpha = opacity.coerceIn(0.35f, 1f))
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE2E8F0).copy(alpha = (opacity * 0.88f).coerceIn(0.3f, 0.96f)),
                            Color(0xFFF8FAFC).copy(alpha = opacity.coerceIn(0.4f, 0.98f))
                        )
                    )
                }
            }
            T9ThemeStyle.OPAQUE -> {
                if (isDarkSurface) {
                    Brush.verticalGradient(colors = listOf(OpaqueBg, Color(0xFF05070B)))
                } else {
                    Brush.verticalGradient(colors = listOf(Color(0xFFFFFFFF), Color(0xFFF1F5F9)))
                }
            }
            T9ThemeStyle.MATERIAL -> {
                if (isDarkSurface) {
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialBg.copy(alpha = opacity.coerceAtLeast(0.85f)),
                            Color(0xFF0B1120).copy(alpha = (opacity + 0.08f).coerceIn(0.88f, 1f))
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEEF2FF).copy(alpha = opacity.coerceAtLeast(0.9f)),
                            Color(0xFFF8FAFC)
                        )
                    )
                }
            }
        }
    }

    fun getKeySurfaceColor(): Color {
        return when (style) {
            T9ThemeStyle.GLASS -> {
                if (isDarkSurface) Color(0xFF1E293B).copy(alpha = (opacity * 0.45f).coerceIn(0.18f, 0.72f))
                else Color(0xFFFFFFFF).copy(alpha = (opacity * 0.65f).coerceIn(0.3f, 0.85f))
            }
            T9ThemeStyle.OPAQUE -> {
                if (isDarkSurface) OpaqueSurface else Color(0xFFF1F5F9)
            }
            T9ThemeStyle.MATERIAL -> {
                if (isDarkSurface) MaterialSurface else Color(0xFFE2E8F0)
            }
        }
    }

    fun getKeyBorderColor(): Color {
        val accent = getAccentColor()
        return when (style) {
            T9ThemeStyle.GLASS -> accent.copy(alpha = 0.42f)
            T9ThemeStyle.OPAQUE -> if (isDarkSurface) OpaqueBorder else Color(0xFFCBD5E1)
            T9ThemeStyle.MATERIAL -> if (isDarkSurface) Color(0xFF475569) else Color(0xFF94A3B8)
        }
    }

    fun getAccentColor(): Color {
        customAccentArgb?.let { return Color(it) }
        return when (style) {
            T9ThemeStyle.GLASS -> GlassAccent
            T9ThemeStyle.OPAQUE -> OpaqueAccent
            T9ThemeStyle.MATERIAL -> MaterialPrimary
        }
    }

    fun getTextPrimaryColor(): Color {
        return if (isDarkSurface) {
            when (style) {
                T9ThemeStyle.GLASS -> GlassTextPrimary
                T9ThemeStyle.OPAQUE -> OpaqueTextPrimary
                T9ThemeStyle.MATERIAL -> Color(0xFFF1F5F9)
            }
        } else {
            Color(0xFF0F172A)
        }
    }

    fun getTextSecondaryColor(): Color {
        return if (isDarkSurface) {
            when (style) {
                T9ThemeStyle.GLASS -> GlassTextSecondary
                T9ThemeStyle.OPAQUE -> OpaqueTextSecondary
                T9ThemeStyle.MATERIAL -> Color(0xFF94A3B8)
            }
        } else {
            Color(0xFF475569)
        }
    }

    fun getCardSurfaceColor(): Color {
        return when (style) {
            T9ThemeStyle.GLASS -> {
                if (isDarkSurface) Color(0x28FFFFFF) else Color(0x99FFFFFF)
            }
            T9ThemeStyle.OPAQUE -> {
                if (isDarkSurface) OpaqueSurfaceElevated else Color(0xFFE2E8F0)
            }
            T9ThemeStyle.MATERIAL -> {
                if (isDarkSurface) MaterialSurfaceVariant else Color(0xFFFFFFFF)
            }
        }
    }

    fun getOnAccentColor(): Color {
        val accent = getAccentColor()
        val luminance = (0.299f * accent.red + 0.587f * accent.green + 0.114f * accent.blue)
        return if (luminance > 0.55f) Color.Black else Color.White
    }
}
