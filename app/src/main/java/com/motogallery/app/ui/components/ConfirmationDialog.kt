package com.motogallery.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable

/**
 * Apple-Style Glassmorphic Confirmation Dialog
 * Features frosted obsidian surface, light-catching top border, and tactile spring buttons.
 */
@Composable
fun ConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val wallpaperConfig = LocalWallpaperConfig.current
    val dialogBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.surfaceColor.copy(alpha = 0.92f)
    } else {
        MotoGlassDark
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = MotoTextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        },
        text = {
            Text(
                text = message,
                color = MotoTextSecondary,
                fontSize = 14.5.sp,
                lineHeight = 21.sp
            )
        },
        confirmButton = {
            val buttonColor = if (isDestructive) MotoError else MotoViolet
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(buttonColor)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.Transparent)
                        )
                    )
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                        RoundedCornerShape(12.dp)
                    )
                    .applePressable(pressScale = 0.94f) {
                        onConfirm()
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = confirmText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                        RoundedCornerShape(12.dp)
                    )
                    .applePressable(pressScale = 0.94f) {
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dismissText,
                    color = MotoTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = dialogBg,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .background(MotoGlassSheenBrush, RoundedCornerShape(24.dp))
    )
}

@Composable
fun InputTextDialog(
    title: String,
    initialValue: String = "",
    hint: String = "Enter name",
    confirmText: String = "Save",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }
    val wallpaperConfig = LocalWallpaperConfig.current
    val dialogBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.surfaceColor.copy(alpha = 0.92f)
    } else {
        MotoGlassDark
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                color = MotoTextPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    placeholder = { Text(text = hint, color = MotoTextTertiary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MotoTextPrimary,
                        unfocusedTextColor = MotoTextPrimary,
                        focusedBorderColor = MotoViolet,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                        cursorColor = MotoViolet
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MotoViolet)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.20f), Color.Transparent)
                        )
                    )
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.30f)),
                        RoundedCornerShape(12.dp)
                    )
                    .applePressable(pressScale = 0.94f) {
                        if (textValue.isNotBlank()) {
                            onConfirm(textValue.trim())
                            onDismiss()
                        }
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = confirmText,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.06f))
                    .border(
                        BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                        RoundedCornerShape(12.dp)
                    )
                    .applePressable(pressScale = 0.94f) {
                        onDismiss()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = MotoTextSecondary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        },
        containerColor = dialogBg,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.05f))
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .background(MotoGlassSheenBrush, RoundedCornerShape(24.dp))
    )
}
