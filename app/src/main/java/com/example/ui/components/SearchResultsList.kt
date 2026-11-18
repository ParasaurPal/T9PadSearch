package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.data.local.SearchHistoryEntity
import com.example.data.model.ActionType
import com.example.data.model.SearchItem
import com.example.ui.theme.CategoryAppColor
import com.example.ui.theme.CategoryContactColor
import com.example.ui.theme.CategoryFileColor
import com.example.ui.theme.CategorySettingColor
import com.example.ui.theme.CategoryWebColor
import com.example.ui.theme.ThemeConfig

@Composable
fun SearchResultsList(
    results: List<SearchItem>,
    query: String,
    contactsPermissionGranted: Boolean,
    themeConfig: ThemeConfig,
    recentSearches: List<SearchHistoryEntity> = emptyList(),
    onRecentSearchClick: (SearchHistoryEntity) -> Unit = {},
    onDeleteRecentSearch: (String) -> Unit = {},
    onClearHistory: () -> Unit = {},
    onRequestContactsPermission: () -> Unit,
    onAppClick: (SearchItem.AppItem) -> Unit,
    onAppInfoClick: (SearchItem.AppItem) -> Unit,
    onContactClick: (SearchItem.ContactItem) -> Unit,
    onContactSmsClick: (SearchItem.ContactItem) -> Unit,
    onFileClick: (SearchItem.FileItem) -> Unit,
    onSettingClick: (SearchItem.SettingItem) -> Unit,
    onActionClick: (SearchItem.ActionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val showRecentHistory = query.isEmpty() && recentSearches.isNotEmpty()

    if (results.isEmpty() && !showRecentHistory) {
        EmptySearchResults(
            query = query,
            themeConfig = themeConfig,
            modifier = modifier
        )
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Contacts permission banner if needed
            if (!contactsPermissionGranted && query.isEmpty()) {
                item(key = "contacts_permission_banner") {
                    ContactsPermissionBanner(
                        themeConfig = themeConfig,
                        onRequestPermission = onRequestContactsPermission
                    )
                }
            }

            // Recent search history section from Room DB
            if (showRecentHistory) {
                item(key = "recent_searches_section") {
                    RecentSearchesSection(
                        recentSearches = recentSearches,
                        themeConfig = themeConfig,
                        onRecentSearchClick = onRecentSearchClick,
                        onDeleteRecentSearch = onDeleteRecentSearch,
                        onClearHistory = onClearHistory
                    )
                }
            }

            val matchingAppsCount = results.count { it is SearchItem.AppItem }
            if (matchingAppsCount > 0 && query.isNotEmpty()) {
                item(key = "header_apps") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ MATCHING APPS ($matchingAppsCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = themeConfig.getAccentColor()
                        )
                    }
                }
            }

            items(results, key = { it.id }) { item ->
                SearchResultCard(
                    item = item,
                    themeConfig = themeConfig,
                    onAppClick = onAppClick,
                    onAppInfoClick = onAppInfoClick,
                    onContactClick = onContactClick,
                    onContactSmsClick = onContactSmsClick,
                    onFileClick = onFileClick,
                    onSettingClick = onSettingClick,
                    onActionClick = onActionClick
                )
            }
        }
    }
}

@Composable
fun RecentSearchesSection(
    recentSearches: List<SearchHistoryEntity>,
    themeConfig: ThemeConfig,
    onRecentSearchClick: (SearchHistoryEntity) -> Unit,
    onDeleteRecentSearch: (String) -> Unit,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val chipShape = themeConfig.getChipShape()
    val accent = themeConfig.getAccentColor()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .testTag("recent_searches_section")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.History,
                    contentDescription = "Recent Searches",
                    tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RECENT SEARCHES",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = accent
                )
            }

            TextButton(
                onClick = onClearHistory,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                modifier = Modifier.testTag("clear_history_button")
            ) {
                Text(
                    text = "Clear All",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = themeConfig.getTextSecondaryColor()
                )
            }
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(recentSearches, key = { it.query }) { entry ->
                Row(
                    modifier = Modifier
                        .clip(chipShape)
                        .background(themeConfig.getCardSurfaceColor())
                        .border(
                            width = themeConfig.getEffectiveBorderWidth(),
                            color = themeConfig.getKeyBorderColor().copy(alpha = 0.7f),
                            shape = chipShape
                        )
                        .clickable { onRecentSearchClick(entry) }
                        .padding(start = 10.dp, end = 6.dp, top = 7.dp, bottom = 7.dp)
                        .testTag("recent_search_${entry.query}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.query,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    if (entry.displayTitle.isNotBlank()) {
                        Text(
                            text = " → ${entry.displayTitle}",
                            color = themeConfig.getTextPrimaryColor(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (entry.searchCount > 1) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Box(
                            modifier = Modifier
                                .clip(themeConfig.getBadgeShape())
                                .background(accent.copy(alpha = 0.18f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${entry.searchCount}×",
                                color = accent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(if (themeConfig.sharpEdges) themeConfig.getBadgeShape() else CircleShape)
                            .clickable { onDeleteRecentSearch(entry.query) }
                            .testTag("delete_recent_${entry.query}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove ${entry.query}",
                            tint = themeConfig.getTextSecondaryColor(),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultCard(
    item: SearchItem,
    themeConfig: ThemeConfig,
    onAppClick: (SearchItem.AppItem) -> Unit,
    onAppInfoClick: (SearchItem.AppItem) -> Unit,
    onContactClick: (SearchItem.ContactItem) -> Unit,
    onContactSmsClick: (SearchItem.ContactItem) -> Unit,
    onFileClick: (SearchItem.FileItem) -> Unit,
    onSettingClick: (SearchItem.SettingItem) -> Unit,
    onActionClick: (SearchItem.ActionItem) -> Unit
) {
    val shape = themeConfig.getCardShape()
    val iconShape = if (themeConfig.sharpEdges) themeConfig.getBadgeShape() else CircleShape
    val onAccent = themeConfig.getOnAccentColor()

    val (badgeText, badgeColor) = when (item) {
        is SearchItem.AppItem -> "APP" to CategoryAppColor
        is SearchItem.ContactItem -> "CONTACT" to CategoryContactColor
        is SearchItem.FileItem -> item.extension.ifEmpty { "FILE" } to CategoryFileColor
        is SearchItem.SettingItem -> "SETTING" to CategorySettingColor
        is SearchItem.ActionItem -> when (item.actionType) {
            ActionType.DIAL -> "CALL" to CategoryContactColor
            ActionType.WEB_SEARCH -> "WEB" to CategoryWebColor
            ActionType.CALCULATOR -> "CALC" to CategoryAppColor
        }
    }

    val primaryClick = {
        when (item) {
            is SearchItem.AppItem -> onAppClick(item)
            is SearchItem.ContactItem -> onContactClick(item)
            is SearchItem.FileItem -> onFileClick(item)
            is SearchItem.SettingItem -> onSettingClick(item)
            is SearchItem.ActionItem -> onActionClick(item)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(themeConfig.getCardSurfaceColor())
            .border(
                themeConfig.getEffectiveBorderWidth(),
                themeConfig.getKeyBorderColor().copy(alpha = 0.5f),
                shape
            )
            .clickable { primaryClick() }
            .padding(12.dp)
            .testTag("result_card_${item.id}")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon Badge
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(iconShape)
                    .background(badgeColor.copy(alpha = 0.2f))
                    .border(1.dp, badgeColor.copy(alpha = 0.4f), iconShape),
                contentAlignment = Alignment.Center
            ) {
                when (item) {
                    is SearchItem.AppItem -> {
                        val bitmap = remember(item.iconDrawable) {
                            try {
                                item.iconDrawable?.toBitmap(width = 96, height = 96)
                            } catch (e: Exception) {
                                null
                            }
                        }
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(iconShape)
                            )
                        } else {
                            Text("📱", fontSize = 20.sp)
                        }
                    }
                    is SearchItem.ContactItem -> {
                        Text("👤", fontSize = 20.sp)
                    }
                    is SearchItem.FileItem -> {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "File",
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    is SearchItem.SettingItem -> {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Setting",
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    is SearchItem.ActionItem -> {
                        when (item.actionType) {
                            ActionType.DIAL -> Icon(Icons.Default.Phone, "Dial", tint = badgeColor, modifier = Modifier.size(20.dp))
                            ActionType.WEB_SEARCH -> Icon(Icons.Default.Search, "Web", tint = badgeColor, modifier = Modifier.size(20.dp))
                            ActionType.CALCULATOR -> Text("🧮", fontSize = 18.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Subtitle with Highlight
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = highlightMatches(item.title, item.matchedRanges, themeConfig.getAccentColor()),
                        color = themeConfig.getTextPrimaryColor(),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Box(
                        modifier = Modifier
                            .clip(themeConfig.getBadgeShape())
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.subtitle,
                    color = themeConfig.getTextSecondaryColor(),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Quick Action Buttons
            when (item) {
                is SearchItem.ContactItem -> {
                    Row {
                        IconButton(
                            onClick = { onContactClick(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = CategoryContactColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = { onContactSmsClick(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "SMS",
                                tint = themeConfig.getAccentColor(),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
                is SearchItem.AppItem -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { onAppClick(item) },
                            colors = ButtonDefaults.buttonColors(containerColor = themeConfig.getAccentColor()),
                            shape = themeConfig.getChipShape(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("OPEN", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = onAccent)
                        }
                        IconButton(
                            onClick = { onAppInfoClick(item) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "App Info",
                                tint = themeConfig.getTextSecondaryColor(),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                is SearchItem.FileItem -> {
                    IconButton(
                        onClick = { onFileClick(item) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open File",
                            tint = CategoryFileColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                is SearchItem.SettingItem -> {
                    IconButton(
                        onClick = { onSettingClick(item) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Open Setting",
                            tint = CategorySettingColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                is SearchItem.ActionItem -> {
                    IconButton(
                        onClick = { onActionClick(item) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Launch,
                            contentDescription = "Execute",
                            tint = themeConfig.getAccentColor(),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ContactsPermissionBanner(
    themeConfig: ThemeConfig,
    onRequestPermission: () -> Unit
) {
    val shape = themeConfig.getCardShape()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(themeConfig.getKeySurfaceColor())
            .border(themeConfig.getEffectiveBorderWidth(), CategoryContactColor.copy(alpha = 0.5f), shape)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "📇 Enable Full Contacts Search",
                    color = themeConfig.getTextPrimaryColor(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Grant permission to search all contacts on this device with T9",
                    color = themeConfig.getTextSecondaryColor(),
                    fontSize = 11.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = CategoryContactColor),
                shape = themeConfig.getChipShape(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Allow", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun EmptySearchResults(
    query: String,
    themeConfig: ThemeConfig,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(if (themeConfig.sharpEdges) themeConfig.getCardShape() else CircleShape)
                    .background(themeConfig.getKeySurfaceColor()),
                contentAlignment = Alignment.Center
            ) {
                Text(text = if (query.isEmpty()) "⌨️" else "🔍", fontSize = 28.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (query.isEmpty()) "T9 Predictive Phone Search" else "No matching items for '$query'",
                color = themeConfig.getTextPrimaryColor(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (query.isEmpty())
                    "Tap digits to search anything across your phone:\n• 4664 for Google / Chrome\n• 2288 for Battery Settings\n• 2542 for Alice\n• 733 for PDF files"
                else
                    "Try typing fewer digits or check other category tabs.",
                color = themeConfig.getTextSecondaryColor(),
                fontSize = 13.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

fun highlightMatches(
    text: String,
    matchedRanges: List<IntRange>,
    accentColor: Color
): AnnotatedString {
    if (matchedRanges.isEmpty()) return AnnotatedString(text)

    return buildAnnotatedString {
        append(text)
        for (range in matchedRanges) {
            val start = range.first.coerceIn(0, text.length)
            val end = (range.last + 1).coerceIn(0, text.length)
            if (start < end) {
                addStyle(
                    style = SpanStyle(
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        background = accentColor.copy(alpha = 0.22f)
                    ),
                    start = start,
                    end = end
                )
            }
        }
    }
}
