package com.motogallery.app.ui.screens.settings

import android.os.Build
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.InputTextDialog
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoOrange
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.FileSizeFormatter

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateToTrash: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var showChangePinDialog by remember { mutableStateOf(false) }
    var showAutoLockMenu by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MotoBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MotoBackground,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Settings",
                        color = MotoTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Storage Statistics Breakdown
            Text(
                text = "STORAGE OVERVIEW",
                color = MotoViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                color = MotoSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    StorageItemRow(
                        title = "Photos",
                        subtitle = "${uiState.photoCount} photos",
                        sizeFormatted = FileSizeFormatter.format(uiState.photoSizeBytes),
                        color = MotoViolet
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StorageItemRow(
                        title = "Videos",
                        subtitle = "${uiState.videoCount} videos",
                        sizeFormatted = FileSizeFormatter.format(uiState.videoSizeBytes),
                        color = MotoPink
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    StorageItemRow(
                        title = "Encrypted Vault",
                        subtitle = "Hardware-backed storage",
                        sizeFormatted = FileSizeFormatter.format(uiState.vaultSizeBytes),
                        color = MotoOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Appearance Section
            Text(
                text = "APPEARANCE",
                color = MotoViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                color = MotoSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Default.DarkMode,
                        title = "Dark Theme",
                        subtitle = "Sleek Obsidian dark styling",
                        checked = uiState.isDarkTheme,
                        onCheckedChange = { viewModel.setDarkTheme(it) }
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        SettingsSwitchRow(
                            icon = Icons.Default.ColorLens,
                            title = "Dynamic Colors",
                            subtitle = "Adapt to device system wallpaper theme",
                            checked = uiState.useDynamicColors,
                            onCheckedChange = { viewModel.setDynamicColors(it) }
                        )
                    }

                    SettingsClickableRow(
                        icon = Icons.Default.GridView,
                        title = "Default Grid Columns",
                        subtitle = "${uiState.gridColumns} columns in gallery",
                        onClick = {
                            val next = when (uiState.gridColumns) {
                                2 -> 3
                                3 -> 4
                                4 -> 5
                                else -> 2
                            }
                            viewModel.setGridColumns(next)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Vault Security Section
            Text(
                text = "VAULT & SECURITY",
                color = MotoViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                color = MotoSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Default.Lock,
                        title = "Change Vault PIN",
                        subtitle = if (uiState.isVaultConfigured) "Update security PIN" else "Vault not yet initialized",
                        onClick = {
                            if (uiState.isVaultConfigured) {
                                showChangePinDialog = true
                            }
                        }
                    )

                    if (uiState.isBiometricSupported) {
                        SettingsSwitchRow(
                            icon = Icons.Default.Fingerprint,
                            title = "Biometric Authentication",
                            subtitle = "Unlock vault with fingerprint or face",
                            checked = uiState.isBiometricEnabled,
                            onCheckedChange = { viewModel.setBiometricEnabled(it) }
                        )
                    }

                    Box {
                        SettingsClickableRow(
                            icon = Icons.Default.Timer,
                            title = "Auto-Lock Vault",
                            subtitle = when (uiState.autoLockDelaySeconds) {
                                0 -> "Immediately on background"
                                30 -> "After 30 seconds"
                                60 -> "After 1 minute"
                                else -> "After 5 minutes"
                            },
                            onClick = { showAutoLockMenu = true }
                        )

                        DropdownMenu(
                            expanded = showAutoLockMenu,
                            onDismissRequest = { showAutoLockMenu = false },
                            modifier = Modifier.background(MotoSurface)
                        ) {
                            listOf(
                                0 to "Immediately on background",
                                30 to "After 30 seconds",
                                60 to "After 1 minute",
                                300 to "After 5 minutes"
                            ).forEach { (seconds, label) ->
                                DropdownMenuItem(
                                    text = { Text(text = label, color = MotoTextPrimary) },
                                    onClick = {
                                        viewModel.setAutoLockDelay(seconds)
                                        showAutoLockMenu = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Organization & Tools
            Text(
                text = "MANAGEMENT",
                color = MotoViolet,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            Surface(
                color = MotoSurface,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    SettingsClickableRow(
                        icon = Icons.Default.Delete,
                        title = "Recently Deleted",
                        subtitle = "Restore or permanently shred deleted media",
                        onClick = onNavigateToTrash
                    )

                    SettingsClickableRow(
                        icon = Icons.Default.Info,
                        title = "About MotoGallery",
                        subtitle = "Version 1.0.0 · Offline & Private",
                        onClick = { showAboutDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showAboutDialog) {
        ConfirmationDialog(
            title = "About MotoGallery",
            message = "MotoGallery is built for Motorola phone users seeking a fast, private, offline native Android gallery. Completely offline, zero telemetry, no ads, no trackers, and hardware-backed AES-256-GCM vault security.\n\nOpen Source · MIT License",
            confirmText = "Done",
            onConfirm = {},
            onDismiss = { showAboutDialog = false }
        )
    }

    if (showChangePinDialog) {
        var oldPin by remember { mutableStateOf("") }
        var newPin by remember { mutableStateOf("") }

        InputTextDialog(
            title = "Enter Current PIN",
            hint = "Current PIN",
            confirmText = "Next",
            onConfirm = { currentEntered ->
                oldPin = currentEntered
                // Now prompt for new PIN
                viewModel.changeVaultPin(oldPin, newPin)
            },
            onDismiss = { showChangePinDialog = false }
        )
    }
}

@Composable
private fun StorageItemRow(
    title: String,
    subtitle: String,
    sizeFormatted: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MotoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = MotoTextSecondary, fontSize = 12.sp)
        }
        Text(text = sizeFormatted, color = MotoTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MotoViolet, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MotoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = MotoTextSecondary, fontSize = 12.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MotoViolet,
                uncheckedThumbColor = MotoTextSecondary,
                uncheckedTrackColor = MotoSurfaceVariant
            )
        )
    }
}

@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = MotoViolet, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = MotoTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Text(text = subtitle, color = MotoTextSecondary, fontSize = 12.sp)
        }
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = MotoTextTertiary)
    }
}
