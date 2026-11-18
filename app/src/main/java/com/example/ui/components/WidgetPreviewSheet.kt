package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ThemeConfig
import com.example.widget.T9WidgetProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WidgetPreviewSheet(
    themeConfig: ThemeConfig,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = themeConfig.getKeySurfaceColor(),
        contentColor = themeConfig.getTextPrimaryColor()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Widget",
                        tint = themeConfig.getAccentColor(),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Home Screen Widget",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeConfig.getTextPrimaryColor()
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

            val containerShape = themeConfig.getContainerShape()
            val barShape = themeConfig.getSearchBarShape()
            val keyShape = themeConfig.getKeyShape()
            val borderWidth = themeConfig.getEffectiveBorderWidth()

            // Realistic Live Widget Mock Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(containerShape)
                    .background(themeConfig.getKeySurfaceColor())
                    .border(borderWidth, themeConfig.getAccentColor().copy(alpha = 0.5f), containerShape)
                    .padding(14.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Widget header bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(barShape)
                            .background(themeConfig.getCardSurfaceColor())
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔍", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "4664",
                                color = themeConfig.getAccentColor(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Google Chrome (Top Match)",
                                color = themeConfig.getTextSecondaryColor(),
                                fontSize = 10.sp
                            )
                        }
                        Text("⌫", color = themeConfig.getTextSecondaryColor(), fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("↗", color = themeConfig.getAccentColor(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Keypad rows preview
                    val miniKeys = listOf(
                        listOf("1", "2 ABC", "3 DEF"),
                        listOf("4 GHI", "5 JKL", "6 MNO"),
                        listOf("7 PQRS", "8 TUV", "9 WXYZ"),
                        listOf("CLR", "0 +", "↵ ENTER")
                    )

                    miniKeys.forEach { row ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { label ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(34.dp)
                                        .clip(keyShape)
                                        .background(themeConfig.getCardSurfaceColor())
                                        .border(borderWidth, themeConfig.getKeyBorderColor(), keyShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (label.contains("ENTER") || label.contains("GO")) themeConfig.getAccentColor()
                                        else if (label == "CLR") Color(0xFFF87171)
                                        else themeConfig.getTextPrimaryColor()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Add this interactive T9 search widget directly to your phone's home screen for instant zero-lag phone search.",
                fontSize = 12.sp,
                color = themeConfig.getTextSecondaryColor(),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val appWidgetManager = AppWidgetManager.getInstance(context)
                        val myProvider = ComponentName(context, T9WidgetProvider::class.java)
                        if (appWidgetManager.isRequestPinAppWidgetSupported) {
                            appWidgetManager.requestPinAppWidget(myProvider, null, null)
                            Toast.makeText(context, "Prompting home screen widget…", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Long-press your home screen, select Widgets, and choose T9 Quick Search",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Long-press your home screen, select Widgets, and drag T9 Search",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pin_widget_button"),
                colors = ButtonDefaults.buttonColors(containerColor = themeConfig.getAccentColor()),
                shape = themeConfig.getCardShape()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Pin Widget",
                        tint = themeConfig.getOnAccentColor()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Widget to Home Screen",
                        color = themeConfig.getOnAccentColor(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
