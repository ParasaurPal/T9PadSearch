package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeConfig

@Composable
fun T9SearchBar(
    query: String,
    predictiveTop: String?,
    isQwertyMode: Boolean,
    themeConfig: ThemeConfig,
    onQueryChange: (String) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenWidgetPreview: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    val shape = themeConfig.getSearchBarShape()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(shape)
            .background(themeConfig.getKeySurfaceColor())
            .border(
                width = themeConfig.getEffectiveBorderWidth(),
                color = themeConfig.getKeyBorderColor(),
                shape = shape
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search icon
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = themeConfig.getAccentColor(),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(10.dp))

        // Center Query area
        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (isQwertyMode) {
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = themeConfig.getTextPrimaryColor(),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    cursorBrush = SolidColor(themeConfig.getAccentColor()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_text_input")
                )
                if (query.isEmpty()) {
                    Text(
                        text = "Type to search everything…",
                        color = themeConfig.getTextSecondaryColor(),
                        fontSize = 15.sp
                    )
                }
            } else {
                if (query.isEmpty()) {
                    Text(
                        text = "Type T9 digits (e.g. 4664 for GOOG)…",
                        color = themeConfig.getTextSecondaryColor(),
                        fontSize = 14.sp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = query,
                            color = themeConfig.getTextPrimaryColor(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 2.sp,
                            modifier = Modifier.testTag("t9_digits_display")
                        )

                        if (!predictiveTop.isNullOrEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(themeConfig.getBadgeShape())
                                    .background(themeConfig.getAccentColor().copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "→ $predictiveTop",
                                    color = themeConfig.getAccentColor(),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Backspace / Clear
        if (query.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (themeConfig.hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onBackspace()
                            },
                            onLongPress = {
                                if (themeConfig.hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onClearAll()
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Backspace,
                    contentDescription = "Backspace (Hold to Clear)",
                    tint = themeConfig.getTextSecondaryColor(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Home screen widget shortcut
        IconButton(
            onClick = onOpenWidgetPreview,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Widgets,
                contentDescription = "Widget Preview",
                tint = themeConfig.getAccentColor(),
                modifier = Modifier.size(20.dp)
            )
        }

        // Theme sheet button
        IconButton(
            onClick = onOpenThemes,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Palette,
                contentDescription = "Themes",
                tint = themeConfig.getTextSecondaryColor(),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
