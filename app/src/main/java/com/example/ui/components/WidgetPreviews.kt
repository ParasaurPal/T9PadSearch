package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeConfig
import com.example.ui.viewmodel.T9UiState
import com.example.ui.viewmodel.T9ViewModel

/**
 * Ambient blurred backdrop rendered behind translucent Glass surfaces so foreground
 * typography remains razor-sharp while delivering authentic frosted depth.
 */
@Composable
fun GlassBackdropLayer(
    themeConfig: ThemeConfig,
    modifier: Modifier = Modifier
) {
    if (themeConfig.style != T9ThemeStyle.GLASS || themeConfig.blurRadiusDp <= 0f) return

    val accent = themeConfig.getAccentColor()
    val blurModifier = Modifier.blur(themeConfig.blurRadiusDp.dp)

    Box(
        modifier = modifier
            .fillMaxSize()
            .then(blurModifier)
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .align(Alignment.TopStart)
                .offset(x = (-40).dp, y = 40.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.42f),
                            Color.Transparent
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .size(260.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 50.dp, y = (-60).dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF6366F1).copy(alpha = 0.36f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Live interactive 4x3 T9 Keypad Widget Preview
 */
@Composable
fun LiveKeypadWidgetPreview(
    uiState: T9UiState,
    viewModel: T9ViewModel
) {
    val themeConfig = uiState.themeConfig
    val shape = themeConfig.getContainerShape()
    val barShape = themeConfig.getSearchBarShape()
    val keyShape = themeConfig.getKeyShape()
    val borderWidth = themeConfig.getEffectiveBorderWidth()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(themeConfig.getBackgroundBrush())
            .border(borderWidth, themeConfig.getKeyBorderColor(), shape)
            .padding(10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Widget search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(barShape)
                    .background(themeConfig.getCardSurfaceColor())
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔍", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.query.isEmpty()) "T9 Quick Search" else uiState.query,
                        color = themeConfig.getTextPrimaryColor(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (uiState.searchResults.isNotEmpty())
                            "${uiState.searchResults.first().title} (Top Match)"
                        else
                            "Tap digits below to search phone",
                        color = themeConfig.getTextSecondaryColor(),
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }

                if (uiState.query.isNotEmpty()) {
                    Text(
                        text = "⌫",
                        color = themeConfig.getTextSecondaryColor(),
                        fontSize = 14.sp,
                        modifier = Modifier
                            .clickable { viewModel.backspace() }
                            .padding(horizontal = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mini 4x3 interactive dial pad
            val rows = listOf(
                listOf('1' to ".,!", '2' to "ABC", '3' to "DEF"),
                listOf('4' to "GHI", '5' to "JKL", '6' to "MNO"),
                listOf('7' to "PQRS", '8' to "TUV", '9' to "WXYZ"),
                listOf('C' to "CLR", '0' to "+", 'S' to "GO")
            )

            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    rowKeys.forEach { (keyChar, sub) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(keyShape)
                                .background(themeConfig.getKeySurfaceColor())
                                .border(borderWidth, themeConfig.getKeyBorderColor(), keyShape)
                                .clickable {
                                    when (keyChar) {
                                        'C' -> viewModel.clearQuery()
                                        'S' -> viewModel.openFirstMatch()
                                        else -> viewModel.appendDigit(keyChar)
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (keyChar == 'C') "CLR" else if (keyChar == 'S') "↵" else keyChar.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (keyChar == 'C') Color(0xFFF87171)
                                    else if (keyChar == 'S') themeConfig.getAccentColor()
                                    else themeConfig.getTextPrimaryColor()
                                )
                                Text(
                                    text = sub,
                                    fontSize = 8.sp,
                                    color = themeConfig.getTextSecondaryColor()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Live interactive 4x1 Slim T9 Bar Widget Preview
 */
@Composable
fun LiveBarWidgetPreview(
    uiState: T9UiState,
    viewModel: T9ViewModel
) {
    val themeConfig = uiState.themeConfig
    val shape = themeConfig.getContainerShape()
    val borderWidth = themeConfig.getEffectiveBorderWidth()
    val actionShape = if (themeConfig.sharpEdges) themeConfig.getBadgeShape() else CircleShape

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(themeConfig.getBackgroundBrush())
            .border(borderWidth, themeConfig.getKeyBorderColor(), shape)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🔍", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (uiState.query.isEmpty()) "T9 Phone Search" else uiState.query,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = themeConfig.getTextPrimaryColor()
                )
                Text(
                    text = if (uiState.searchResults.isNotEmpty())
                        "${uiState.searchResults.first().title} (Top Match)"
                    else
                        "Search contacts, apps, files, settings",
                    fontSize = 10.sp,
                    color = themeConfig.getTextSecondaryColor(),
                    maxLines = 1
                )
            }

            if (uiState.query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(actionShape)
                        .background(themeConfig.getCardSurfaceColor())
                        .clickable { viewModel.backspace() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("⌫", fontSize = 12.sp, color = themeConfig.getTextSecondaryColor())
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(actionShape)
                    .background(themeConfig.getAccentColor())
                    .clickable { viewModel.openFirstMatch() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "↵",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeConfig.getOnAccentColor()
                )
            }
        }
    }
}
