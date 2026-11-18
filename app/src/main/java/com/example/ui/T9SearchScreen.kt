package com.example.ui

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RoundedCorner
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.SearchHistoryEntity
import com.example.ui.components.CategoryTabsRow
import com.example.ui.components.GlassBackdropLayer
import com.example.ui.components.LiveBarWidgetPreview
import com.example.ui.components.LiveKeypadWidgetPreview
import com.example.ui.components.PredictiveWordsRow
import com.example.ui.components.SearchResultsList
import com.example.ui.components.T9Keypad
import com.example.ui.components.T9SearchBar
import com.example.ui.components.ThemeSelectorSheet
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.viewmodel.T9UiState
import com.example.ui.viewmodel.T9ViewModel
import com.example.ui.viewmodel.WidgetPreviewFormat

@Composable
fun T9SearchScreen(
    initialQuery: String? = null,
    openedFromWidget: Boolean = false,
    onDismissOverlay: () -> Unit = {},
    viewModel: T9ViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()

    // Handle back button
    BackHandler {
        if (openedFromWidget) {
            onDismissOverlay()
        } else if (uiState.query.isNotEmpty()) {
            viewModel.clearQuery()
        } else {
            onDismissOverlay()
        }
    }

    // Set initial query from widget
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrEmpty()) {
            viewModel.setQuery(initialQuery)
        }
    }

    LaunchedEffect(openedFromWidget) {
        viewModel.setIsFromWidget(openedFromWidget)
    }

    // Permission launcher for contacts search
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onContactsPermissionResult(isGranted)
    }

    if (openedFromWidget) {
        WidgetSearchOverlay(
            uiState = uiState,
            recentSearches = recentSearches,
            viewModel = viewModel,
            onClose = onDismissOverlay,
            onRequestContactsPermission = {
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            modifier = modifier
        )
    } else {
        WidgetStudioScreen(
            uiState = uiState,
            recentSearches = recentSearches,
            viewModel = viewModel,
            onRequestContactsPermission = {
                contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
            },
            modifier = modifier
        )
    }
}

/**
 * Lightweight search overlay shown when user taps the home screen widget.
 * Tapping any result launches it and immediately dismisses back to the user's home screen!
 */
@Composable
fun WidgetSearchOverlay(
    uiState: T9UiState,
    recentSearches: List<SearchHistoryEntity>,
    viewModel: T9ViewModel,
    onClose: () -> Unit,
    onRequestContactsPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(uiState.themeConfig.getBackgroundBrush())
    ) {
        GlassBackdropLayer(themeConfig = uiState.themeConfig)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Overlay Navigation Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close to Home Screen",
                        tint = uiState.themeConfig.getTextPrimaryColor(),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "T9 Search",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = uiState.themeConfig.getTextPrimaryColor()
                    )
                    Text(
                        text = "Tapped from Home Screen Widget",
                        fontSize = 11.sp,
                        color = uiState.themeConfig.getAccentColor()
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleThemeStyle() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Cycle Theme",
                        tint = uiState.themeConfig.getAccentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleQwertyMode() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.isQwertyMode) Icons.Default.Dialpad else Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard",
                        tint = uiState.themeConfig.getAccentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Category filter tabs
            CategoryTabsRow(
                selectedCategory = uiState.selectedCategory,
                categoryCounts = uiState.categoryCounts,
                themeConfig = uiState.themeConfig,
                onCategorySelect = { viewModel.selectCategory(it) }
            )

            // Results List + Room Recent History
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                SearchResultsList(
                    results = uiState.searchResults,
                    query = uiState.query,
                    contactsPermissionGranted = uiState.contactsPermissionGranted,
                    themeConfig = uiState.themeConfig,
                    recentSearches = recentSearches,
                    onRecentSearchClick = { viewModel.applyRecentSearch(it) },
                    onDeleteRecentSearch = { viewModel.deleteRecentSearch(it) },
                    onClearHistory = { viewModel.clearRecentSearches() },
                    onRequestContactsPermission = onRequestContactsPermission,
                    onAppClick = {
                        viewModel.launchApp(it)
                        onClose()
                    },
                    onAppInfoClick = {
                        viewModel.openAppDetails(it)
                        onClose()
                    },
                    onContactClick = {
                        viewModel.dialContact(it)
                        onClose()
                    },
                    onContactSmsClick = {
                        viewModel.smsContact(it)
                        onClose()
                    },
                    onFileClick = {
                        viewModel.openFile(it)
                        onClose()
                    },
                    onSettingClick = {
                        viewModel.openSetting(it)
                        onClose()
                    },
                    onActionClick = {
                        viewModel.executeAction(it)
                        onClose()
                    }
                )
            }

            // Predictive words
            PredictiveWordsRow(
                words = uiState.predictiveWords,
                themeConfig = uiState.themeConfig,
                onWordClick = { viewModel.selectPredictiveWord(it) }
            )

            // Search Bar
            T9SearchBar(
                query = uiState.query,
                predictiveTop = uiState.predictiveWords.firstOrNull(),
                isQwertyMode = uiState.isQwertyMode,
                themeConfig = uiState.themeConfig,
                onQueryChange = { viewModel.setQuery(it) },
                onBackspace = { viewModel.backspace() },
                onClearAll = { viewModel.clearQuery() },
                onOpenThemes = { viewModel.setShowThemeSheet(true) },
                onOpenWidgetPreview = {}
            )

            // Keypad
            AnimatedVisibility(
                visible = !uiState.isQwertyMode,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                T9Keypad(
                    themeConfig = uiState.themeConfig,
                    onDigitClick = { viewModel.appendDigit(it) },
                    onBackspace = { viewModel.backspace() },
                    onClearAll = { viewModel.clearQuery() },
                    onToggleKeyboard = { viewModel.toggleQwertyMode() },
                    onOpenThemes = { viewModel.setShowThemeSheet(true) }
                )
            }
        }

        if (uiState.showThemeSheet) {
            ThemeCustomizerModal(uiState = uiState, viewModel = viewModel)
        }
    }
}

/**
 * Main Widget Studio & Companion screen:
 * Emphasizes that this app is a widget builder, customizer, and companion for your home screen.
 */
@Composable
fun WidgetStudioScreen(
    uiState: T9UiState,
    recentSearches: List<SearchHistoryEntity>,
    viewModel: T9ViewModel,
    onRequestContactsPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Widget Studio, 1: Live Search
    val themeConfig = uiState.themeConfig

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(themeConfig.getBackgroundBrush())
    ) {
        GlassBackdropLayer(themeConfig = themeConfig)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(if (themeConfig.sharpEdges) themeConfig.getBadgeShape() else CircleShape)
                        .background(themeConfig.getAccentColor()),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Widget Icon",
                        tint = themeConfig.getOnAccentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "T9 Widget Studio",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.getTextPrimaryColor()
                    )
                    Text(
                        text = "Add T9 phone search to any home screen",
                        fontSize = 11.sp,
                        color = themeConfig.getTextSecondaryColor()
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleThemeStyle() },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("quick_cycle_theme_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Autorenew,
                        contentDescription = "Toggle Theme Style",
                        tint = themeConfig.getAccentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.setShowThemeSheet(true) },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("open_theme_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Palette,
                        contentDescription = "Theme Settings",
                        tint = themeConfig.getAccentColor(),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Top Tab Navigation: Widget Studio vs Search Tester
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Transparent,
                contentColor = themeConfig.getAccentColor(),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = themeConfig.getAccentColor(),
                        height = 3.dp
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Home Screen Widget",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Test Search (${uiState.searchResults.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                WidgetStudioContent(
                    uiState = uiState,
                    viewModel = viewModel,
                    modifier = Modifier.weight(1f)
                )
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    CategoryTabsRow(
                        selectedCategory = uiState.selectedCategory,
                        categoryCounts = uiState.categoryCounts,
                        themeConfig = themeConfig,
                        onCategorySelect = { viewModel.selectCategory(it) }
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        SearchResultsList(
                            results = uiState.searchResults,
                            query = uiState.query,
                            contactsPermissionGranted = uiState.contactsPermissionGranted,
                            themeConfig = themeConfig,
                            recentSearches = recentSearches,
                            onRecentSearchClick = { viewModel.applyRecentSearch(it) },
                            onDeleteRecentSearch = { viewModel.deleteRecentSearch(it) },
                            onClearHistory = { viewModel.clearRecentSearches() },
                            onRequestContactsPermission = onRequestContactsPermission,
                            onAppClick = { viewModel.launchApp(it) },
                            onAppInfoClick = { viewModel.openAppDetails(it) },
                            onContactClick = { viewModel.dialContact(it) },
                            onContactSmsClick = { viewModel.smsContact(it) },
                            onFileClick = { viewModel.openFile(it) },
                            onSettingClick = { viewModel.openSetting(it) },
                            onActionClick = { viewModel.executeAction(it) }
                        )
                    }

                    PredictiveWordsRow(
                        words = uiState.predictiveWords,
                        themeConfig = themeConfig,
                        onWordClick = { viewModel.selectPredictiveWord(it) }
                    )

                    T9SearchBar(
                        query = uiState.query,
                        predictiveTop = uiState.predictiveWords.firstOrNull(),
                        isQwertyMode = uiState.isQwertyMode,
                        themeConfig = themeConfig,
                        onQueryChange = { viewModel.setQuery(it) },
                        onBackspace = { viewModel.backspace() },
                        onClearAll = { viewModel.clearQuery() },
                        onOpenThemes = { viewModel.setShowThemeSheet(true) },
                        onOpenWidgetPreview = {}
                    )

                    AnimatedVisibility(
                        visible = !uiState.isQwertyMode,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        T9Keypad(
                            themeConfig = themeConfig,
                            onDigitClick = { viewModel.appendDigit(it) },
                            onBackspace = { viewModel.backspace() },
                            onClearAll = { viewModel.clearQuery() },
                            onToggleKeyboard = { viewModel.toggleQwertyMode() },
                            onOpenThemes = { viewModel.setShowThemeSheet(true) }
                        )
                    }
                }
            }
        }

        if (uiState.showThemeSheet) {
            ThemeCustomizerModal(uiState = uiState, viewModel = viewModel)
        }
    }
}

@Composable
private fun ThemeCustomizerModal(
    uiState: T9UiState,
    viewModel: T9ViewModel
) {
    ThemeSelectorSheet(
        themeConfig = uiState.themeConfig,
        onSelectTheme = { viewModel.setThemeStyle(it) },
        onToggleThemeStyle = { viewModel.toggleThemeStyle() },
        onToggleSharpEdges = { viewModel.setSharpEdges(it) },
        onCornerRadiusChange = { viewModel.setCornerRadius(it) },
        onOpacityChange = { viewModel.setOpacity(it) },
        onBlurRadiusChange = { viewModel.setBlurRadius(it) },
        onBorderWidthChange = { viewModel.setBorderWidth(it) },
        onSelectAccentColor = { viewModel.setCustomAccentColor(it) },
        onToggleDarkSurface = { viewModel.setDarkSurface(it) },
        onToggleHaptic = { viewModel.toggleHaptic() },
        onToggleCompact = { viewModel.toggleCompactKeypad() },
        onResetDefaults = { viewModel.resetThemeDefaults() },
        onDismiss = { viewModel.setShowThemeSheet(false) }
    )
}

@Composable
fun WidgetStudioContent(
    uiState: T9UiState,
    viewModel: T9ViewModel,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val themeConfig = uiState.themeConfig
    val cardShape = themeConfig.getCardShape()
    val chipShape = themeConfig.getChipShape()
    val containerShape = themeConfig.getContainerShape()
    val borderWidth = themeConfig.getEffectiveBorderWidth()
    val accent = themeConfig.getAccentColor()
    val onAccent = themeConfig.getOnAccentColor()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Widget Format Selector: 4x3 Full Dialpad vs 4x1 Slim Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(themeConfig.getKeySurfaceColor())
                .border(borderWidth, themeConfig.getKeyBorderColor(), cardShape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WidgetPreviewFormat.values().forEach { format ->
                val isSelected = uiState.selectedWidgetFormat == format
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(chipShape)
                        .background(
                            if (isSelected) accent.copy(alpha = 0.25f)
                            else Color.Transparent
                        )
                        .border(
                            width = if (isSelected) 1.dp else 0.dp,
                            color = if (isSelected) accent else Color.Transparent,
                            shape = chipShape
                        )
                        .clickable { viewModel.setSelectedWidgetFormat(format) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = format.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) accent else themeConfig.getTextPrimaryColor()
                        )
                        Text(
                            text = format.sizeInfo,
                            fontSize = 10.sp,
                            color = themeConfig.getTextSecondaryColor()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "Add to Home Screen" Pin Action Button
        Button(
            onClick = { viewModel.pinWidgetToHomeScreen(uiState.selectedWidgetFormat) },
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("pin_widget_hero_button")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Pin to Home Screen",
                    tint = onAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add ${uiState.selectedWidgetFormat.label} to Home Screen",
                    color = onAccent,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Home Screen Wallpaper Canvas with Live Widget Preview
        Text(
            text = "LIVE HOME SCREEN PREVIEW",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = accent,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(containerShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0F172A),
                            Color(0xFF1E1B4B),
                            Color(0xFF020617)
                        )
                    )
                )
                .border(borderWidth, Color(0xFF334155), containerShape)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "08:30",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = "Wednesday, November 11",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.6f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.selectedWidgetFormat == WidgetPreviewFormat.PAD_4X3) {
                    LiveKeypadWidgetPreview(uiState = uiState, viewModel = viewModel)
                } else {
                    LiveBarWidgetPreview(uiState = uiState, viewModel = viewModel)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf("📞", "💬", "🌐", "📷").forEach { icon ->
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(if (themeConfig.sharpEdges) chipShape else CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = icon, fontSize = 20.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Widget Themes Selector: Glass, Opaque, Material
        Text(
            text = "WIDGET THEME (${themeConfig.style.displayName.uppercase()})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = accent,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            T9ThemeStyle.values().forEach { style ->
                val isSelected = themeConfig.style == style
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(cardShape)
                        .background(
                            if (isSelected) accent.copy(alpha = 0.2f)
                            else themeConfig.getKeySurfaceColor()
                        )
                        .border(
                            width = if (isSelected) 2.dp else borderWidth,
                            color = if (isSelected) accent else themeConfig.getKeyBorderColor(),
                            shape = cardShape
                        )
                        .clickable { viewModel.setThemeStyle(style) }
                        .padding(vertical = 12.dp, horizontal = 6.dp)
                        .testTag("studio_theme_${style.name}"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accent else themeConfig.getTextPrimaryColor()
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Sharp Edges Toggle Card + Full Customizer Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(cardShape)
                .background(themeConfig.getKeySurfaceColor())
                .border(borderWidth, themeConfig.getKeyBorderColor(), cardShape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
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
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.getTextPrimaryColor()
                    )
                    Text(
                        text = if (themeConfig.sharpEdges) "Crisp 90° corners active"
                        else "Corner radius: ${themeConfig.cornerRadiusDp.toInt()}dp",
                        fontSize = 11.sp,
                        color = themeConfig.getTextSecondaryColor()
                    )
                }
            }

            Switch(
                checked = themeConfig.sharpEdges,
                onCheckedChange = { viewModel.setSharpEdges(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = onAccent,
                    checkedTrackColor = accent
                ),
                modifier = Modifier.testTag("studio_sharp_edges_switch")
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = { viewModel.setShowThemeSheet(true) },
            shape = cardShape,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("open_full_customizer_button")
        ) {
            Icon(
                imageVector = Icons.Default.Tune,
                contentDescription = "Full Theme Customization",
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Customize Colors, Opacity, Blur & Borders",
                color = accent,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // How to add widget manual guide
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = cardShape,
            colors = CardDefaults.cardColors(containerColor = themeConfig.getKeySurfaceColor())
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "💡 How to add to your Home Screen",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = themeConfig.getTextPrimaryColor()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "1. Touch and hold any empty area on your phone's Home screen.\n2. Tap 'Widgets'.\n3. Scroll to 'T9 Search' and drag either the 4×3 Pad or 4×1 Bar widget onto any screen.",
                    fontSize = 12.sp,
                    color = themeConfig.getTextSecondaryColor(),
                    lineHeight = 18.sp
                )
            }
        }
    }
}
