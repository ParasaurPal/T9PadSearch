package com.example.ui.components

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
import com.example.data.model.SearchCategory
import com.example.ui.theme.ThemeConfig

@Composable
fun CategoryTabsRow(
    selectedCategory: SearchCategory,
    categoryCounts: Map<SearchCategory, Int>,
    themeConfig: ThemeConfig,
    onCategorySelect: (SearchCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(SearchCategory.values()) { category ->
            val isSelected = category == selectedCategory
            val count = categoryCounts[category] ?: 0
            val shape = themeConfig.getChipShape()

            val bgColor = if (isSelected) {
                themeConfig.getAccentColor().copy(alpha = 0.25f)
            } else {
                themeConfig.getKeySurfaceColor().copy(alpha = 0.5f)
            }

            val borderColor = if (isSelected) {
                themeConfig.getAccentColor()
            } else {
                themeConfig.getKeyBorderColor().copy(alpha = 0.4f)
            }

            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(bgColor)
                    .border(themeConfig.getEffectiveBorderWidth(), borderColor, shape)
                    .clickable { onCategorySelect(category) }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .testTag("category_tab_${category.name}"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = category.icon, fontSize = 12.sp)
                    Text(
                        text = category.label,
                        color = if (isSelected) themeConfig.getAccentColor() else themeConfig.getTextPrimaryColor(),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp
                    )
                    if (count > 0) {
                        Box(
                            modifier = Modifier
                                .clip(themeConfig.getBadgeShape())
                                .background(
                                    if (isSelected) themeConfig.getAccentColor() else themeConfig.getKeyBorderColor()
                                )
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = count.toString(),
                                color = if (isSelected) themeConfig.getOnAccentColor() else themeConfig.getTextPrimaryColor(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
