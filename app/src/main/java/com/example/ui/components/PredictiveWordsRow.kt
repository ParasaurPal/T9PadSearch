package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.T9Engine
import com.example.ui.theme.ThemeConfig

@Composable
fun PredictiveWordsRow(
    words: List<String>,
    themeConfig: ThemeConfig,
    onWordClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = words.isNotEmpty(),
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        LazyRow(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(words) { word ->
                PredictiveChip(
                    word = word,
                    themeConfig = themeConfig,
                    onClick = { onWordClick(word) }
                )
            }
        }
    }
}

@Composable
fun PredictiveChip(
    word: String,
    themeConfig: ThemeConfig,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = themeConfig.getChipShape()
    val t9Representation = T9Engine.textToT9(word)

    Box(
        modifier = modifier
            .clip(shape)
            .background(themeConfig.getKeySurfaceColor())
            .border(
                width = themeConfig.getEffectiveBorderWidth(),
                color = themeConfig.getAccentColor().copy(alpha = 0.5f),
                shape = shape
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("predictive_chip_$word"),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "✨",
                fontSize = 11.sp
            )
            Text(
                text = word,
                color = themeConfig.getTextPrimaryColor(),
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "($t9Representation)",
                color = themeConfig.getTextSecondaryColor(),
                fontSize = 10.sp
            )
        }
    }
}
