package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.T9Engine
import com.example.ui.theme.T9ThemeStyle
import com.example.ui.theme.ThemeConfig

@Composable
fun T9Keypad(
    themeConfig: ThemeConfig,
    onDigitClick: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClearAll: () -> Unit,
    onToggleKeyboard: () -> Unit,
    onOpenThemes: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val rowHeight = if (themeConfig.compactKeypad) 48.dp else 56.dp

    val keyLayout = listOf(
        listOf('1' to ". , !", '2' to "ABC", '3' to "DEF"),
        listOf('4' to "GHI", '5' to "JKL", '6' to "MNO"),
        listOf('7' to "PQRS", '8' to "TUV", '9' to "WXYZ"),
        listOf('*' to "KEY", '0' to "+ _", '#' to "THEME")
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(if (themeConfig.compactKeypad) 4.dp else 6.dp)
    ) {
        keyLayout.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (themeConfig.compactKeypad) 4.dp else 6.dp)
            ) {
                rowKeys.forEach { (keyChar, subtext) ->
                    T9KeyButton(
                        keyChar = keyChar,
                        subtext = subtext,
                        themeConfig = themeConfig,
                        onClick = {
                            if (themeConfig.hapticEnabled) {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            }
                            when (keyChar) {
                                '*' -> onToggleKeyboard()
                                '#' -> onOpenThemes()
                                else -> onDigitClick(keyChar)
                            }
                        },
                        onLongClick = if (keyChar == '0') {
                            {
                                if (themeConfig.hapticEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                onDigitClick('+')
                            }
                        } else null,
                        modifier = Modifier
                            .weight(1f)
                            .height(rowHeight)
                            .testTag("t9_key_$keyChar")
                    )
                }
            }
        }
    }
}

@Composable
fun T9KeyButton(
    keyChar: Char,
    subtext: String,
    themeConfig: ThemeConfig,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "keyScale"
    )

    val shape = themeConfig.getKeyShape()

    val surfaceColor = if (isPressed) {
        when (themeConfig.style) {
            T9ThemeStyle.GLASS -> themeConfig.getAccentColor().copy(alpha = 0.35f)
            T9ThemeStyle.OPAQUE -> themeConfig.getAccentColor().copy(alpha = 0.25f)
            T9ThemeStyle.MATERIAL -> themeConfig.getAccentColor().copy(alpha = 0.2f)
        }
    } else {
        themeConfig.getKeySurfaceColor()
    }

    val borderColor = if (isPressed) {
        themeConfig.getAccentColor()
    } else {
        themeConfig.getKeyBorderColor()
    }

    Box(
        modifier = modifier
            .scale(scale)
            .clip(shape)
            .background(surfaceColor)
            .border(
                width = themeConfig.getEffectiveBorderWidth(),
                color = borderColor,
                shape = shape
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongClick?.invoke() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            val isSpecialAction = keyChar == '*' || keyChar == '#'
            val digitLabel = when (keyChar) {
                '*' -> "⌨"
                '#' -> "🎨"
                else -> keyChar.toString()
            }

            Text(
                text = digitLabel,
                fontSize = if (isSpecialAction) 18.sp else if (themeConfig.compactKeypad) 18.sp else 21.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSpecialAction) themeConfig.getAccentColor() else themeConfig.getTextPrimaryColor()
            )

            Text(
                text = subtext,
                fontSize = if (themeConfig.compactKeypad) 9.sp else 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                color = if (isPressed) themeConfig.getAccentColor() else themeConfig.getTextSecondaryColor(),
                maxLines = 1
            )
        }
    }
}
