package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentPalette
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectorSheet(
    themeConfig: ThemeConfig,
    onSelectTheme: (T9ThemeStyle) -> Unit,
    onToggleThemeStyle: () -> Unit,
    onToggleSharpEdges: (Boolean) -> Unit,
    onCornerRadiusChange: (Float) -> Unit,
    onOpacityChange: (Float) -> Unit,
    onBlurRadiusChange: (Float) -> Unit,
    onBorderWidthChange: (Float) -> Unit,
    onSelectAccentColor: (Long?) -> Unit,
    onToggleDarkSurface: (Boolean) -> Unit,
    onToggleHaptic: () -> Unit,
    onToggleCompact: () -> Unit,
    onResetDefaults: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scrollState = rememberScrollState()
    val cardShape = themeConfig.getCardShape()
    val chipShape = themeConfig.getChipShape()
    val accent = themeConfig.getAccentColor()
    val onAccent = themeConfig.getOnAccentColor()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = themeConfig.getContainerShape(),
        containerColor = if (themeConfig.isDarkSurface) Color(0xFF0F172A) else Color(0xFFF8FAFC),
        contentColor = themeConfig.getTextPrimaryColor()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Full Theme Customizer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.getTextPrimaryColor()
                    )
                    Text(
                        text = "Toggle Glass, Opaque & Material 3 with sharp edges & custom tones",
                        fontSize = 12.sp,
                        color = themeConfig.getTextSecondaryColor()
                    )
                }

                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = themeConfig.getTextSecondaryColor()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme Style Header + Quick Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "THEME STYLE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = accent
                )

                TextButton(
                    onClick = onToggleThemeStyle,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("cycle_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Cycle Theme Style",
                        tint = accent,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Cycle Style",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accent
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 3 Primary Themes: Glass, Opaque, Material
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                T9ThemeStyle.values().forEach { style ->
                    val isSelected = themeConfig.style == style
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(cardShape)
                            .background(
                                if (isSelected) accent.copy(alpha = 0.2f)
                                else themeConfig.getCardSurfaceColor()
                            )
                            .border(
                                width = if (isSelected) 2.dp else themeConfig.getEffectiveBorderWidth(),
                                color = if (isSelected) accent else themeConfig.getKeyBorderColor(),
                                shape = cardShape
                            )
                            .clickable { onSelectTheme(style) }
                            .padding(vertical = 12.dp, horizontal = 8.dp)
                            .testTag("theme_btn_${style.name}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = when (style) {
                                    T9ThemeStyle.GLASS -> "✨"
                                    T9ThemeStyle.OPAQUE -> "⬛"
                                    T9ThemeStyle.MATERIAL -> "🎨"
                                },
                                fontSize = 20.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = style.displayName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) accent else themeConfig.getTextPrimaryColor()
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(if (themeConfig.sharpEdges) cardShape else CircleShape)
                                        .background(accent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = onAccent,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Geometry: Sharp Edges + Corner Radius
            Text(
                text = "CORNER GEOMETRY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RoundedCorner,
                        contentDescription = "Sharp Edges",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Sharp Edges (0dp)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeConfig.getTextPrimaryColor()
                        )
                        Text(
                            text = "Crisp architectural 90° corners on UI & widgets",
                            fontSize = 11.sp,
                            color = themeConfig.getTextSecondaryColor()
                        )
                    }
                }
                Switch(
                    checked = themeConfig.sharpEdges,
                    onCheckedChange = onToggleSharpEdges,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = onAccent,
                        checkedTrackColor = accent
                    ),
                    modifier = Modifier.testTag("sharp_edges_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (themeConfig.sharpEdges) "CORNER RADIUS: 0dp (Sharp)"
                else "CORNER RADIUS (${themeConfig.cornerRadiusDp.toInt()}dp)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = themeConfig.getTextSecondaryColor()
            )
            Slider(
                value = if (themeConfig.sharpEdges) 0f else themeConfig.cornerRadiusDp,
                onValueChange = onCornerRadiusChange,
                valueRange = 0f..28f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = themeConfig.getKeyBorderColor()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("corner_radius_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Accent Color Palette
            Text(
                text = "ACCENT COLOR",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accent
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    val isAuto = themeConfig.customAccentArgb == null
                    Box(
                        modifier = Modifier
                            .clip(chipShape)
                            .background(
                                if (isAuto) accent.copy(alpha = 0.22f)
                                else themeConfig.getCardSurfaceColor()
                            )
                            .border(
                                width = if (isAuto) 2.dp else 1.dp,
                                color = if (isAuto) accent else themeConfig.getKeyBorderColor(),
                                shape = chipShape
                            )
                            .clickable { onSelectAccentColor(null) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag("accent_auto"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Auto",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAuto) accent else themeConfig.getTextPrimaryColor()
                        )
                    }
                }

                items(AccentPalette.presets) { (name, argb) ->
                    val color = Color(argb)
                    val isSelected = themeConfig.customAccentArgb == argb
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(if (themeConfig.sharpEdges) chipShape else CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) themeConfig.getTextPrimaryColor() else Color.Transparent,
                                shape = if (themeConfig.sharpEdges) chipShape else CircleShape
                            )
                            .clickable { onSelectAccentColor(argb) }
                            .testTag("accent_swatch_$name"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = name,
                                tint = onAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Surface Tone, Opacity, Blur, Border
            Text(
                text = "SURFACE & TRANSLUCENCY",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                color = accent
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DarkMode,
                        contentDescription = "Dark Surface",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Dark Surface Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeConfig.getTextPrimaryColor()
                        )
                        Text(
                            text = if (themeConfig.isDarkSurface) "Dark translucent / OLED base"
                            else "Crisp light frosted / daylight base",
                            fontSize = 11.sp,
                            color = themeConfig.getTextSecondaryColor()
                        )
                    }
                }
                Switch(
                    checked = themeConfig.isDarkSurface,
                    onCheckedChange = onToggleDarkSurface,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = onAccent,
                        checkedTrackColor = accent
                    ),
                    modifier = Modifier.testTag("dark_surface_switch")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "SURFACE OPACITY (${(themeConfig.opacity * 100).toInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = themeConfig.getTextSecondaryColor()
            )
            Slider(
                value = themeConfig.opacity,
                onValueChange = onOpacityChange,
                valueRange = 0.25f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = themeConfig.getKeyBorderColor()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("opacity_slider")
            )

            Text(
                text = "GLASS BLUR INTENSITY (${themeConfig.blurRadiusDp.toInt()}dp)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = themeConfig.getTextSecondaryColor()
            )
            Slider(
                value = themeConfig.blurRadiusDp,
                onValueChange = onBlurRadiusChange,
                valueRange = 0f..24f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = themeConfig.getKeyBorderColor()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("blur_radius_slider")
            )

            Text(
                text = "BORDER THICKNESS (${"%.1f".format(themeConfig.borderWidthDp)}dp)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = themeConfig.getTextSecondaryColor()
            )
            Slider(
                value = themeConfig.borderWidthDp,
                onValueChange = onBorderWidthChange,
                valueRange = 0f..3f,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = themeConfig.getKeyBorderColor()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("border_width_slider")
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Haptics toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Vibration,
                        contentDescription = "Haptics",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Haptic Vibration",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = themeConfig.getTextPrimaryColor()
                        )
                        Text(
                            text = "Vibrate on T9 key taps",
                            fontSize = 11.sp,
                            color = themeConfig.getTextSecondaryColor()
                        )
                    }
                }
                Switch(
                    checked = themeConfig.hapticEnabled,
                    onCheckedChange = { onToggleHaptic() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = onAccent,
                        checkedTrackColor = accent
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Compact mode toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Compact Dial Keypad",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = themeConfig.getTextPrimaryColor()
                    )
                    Text(
                        text = "More space for results list",
                        fontSize = 11.sp,
                        color = themeConfig.getTextSecondaryColor()
                    )
                }
                Switch(
                    checked = themeConfig.compactKeypad,
                    onCheckedChange = { onToggleCompact() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = onAccent,
                        checkedTrackColor = accent
                    )
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            OutlinedButton(
                onClick = onResetDefaults,
                shape = cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_theme_button")
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Reset Defaults",
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Reset Theme to Defaults",
                    color = accent,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
