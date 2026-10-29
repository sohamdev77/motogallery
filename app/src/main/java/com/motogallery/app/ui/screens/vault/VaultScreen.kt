package com.motogallery.app.ui.screens.vault

import android.content.IntentSender
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.VaultItem
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.SelectionActionBar
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onNavigateToVaultViewer: (Long) -> Unit,
    onRequestConsent: (IntentSender) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showTrashConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // Visual media picker to select photos & videos to import into vault
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris ->
        if (uris.isNotEmpty()) {
            uris.forEach { uri ->
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                val isVideo = mime.startsWith("video/")
                viewModel.importMedia(
                    uri = uri,
                    displayName = "Secured_${System.currentTimeMillis()}.${if (isVideo) "mp4" else "jpg"}",
                    mimeType = mime,
                    isVideo = isVideo,
                    size = 0L,
                    onConsentToDeleteOriginal = onRequestConsent
                )
            }
        }
    }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (!uiState.isUnlocked) {
        VaultAuthScreen(
            isConfigured = uiState.isVaultConfigured,
            isBiometricAvailable = uiState.isBiometricAvailable,
            isBiometricEnabled = uiState.isBiometricEnabled,
            errorMessage = uiState.errorMessage,
            onSetupPin = { viewModel.setupVault(it) },
            onVerifyPin = { viewModel.verifyPin(it) },
            onBiometricSuccess = { viewModel.unlockWithBiometrics() }
        )
        return
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
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (uiState.isSelectionMode) "${uiState.selectedIds.size} selected" else "Private Vault",
                        color = MotoTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    // Lock button
                    IconButton(onClick = { viewModel.lockVault() }) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = MotoViolet
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                FloatingActionButton(
                    onClick = {
                        mediaPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                        )
                    },
                    containerColor = MotoViolet,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Import Media")
                }
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    color = MotoSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MotoBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Restore button
                        IconButton(onClick = { showRestoreConfirmDialog = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore",
                                    tint = MotoViolet
                                )
                                Text(text = "Restore", color = MotoTextPrimary, fontSize = 10.sp)
                            }
                        }

                        // Trash button
                        IconButton(onClick = { showTrashConfirmDialog = true }) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Trash",
                                    tint = MotoError
                                )
                                Text(text = "Trash", color = MotoTextPrimary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MotoSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = null,
                            tint = MotoViolet,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Vault is empty",
                        color = MotoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Tap the + button to select photos or videos to encrypt and secure inside your private vault.",
                        color = MotoTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(uiState.items, key = { it.id }) { item ->
                    val isSelected = item.id in uiState.selectedIds

                    VaultThumbnailCard(
                        item = item,
                        isSelected = isSelected,
                        isSelectionMode = uiState.isSelectionMode,
                        loadThumbnailBytes = { viewModel.getDecryptedThumbnail(item) },
                        onClick = {
                            if (uiState.isSelectionMode) {
                                viewModel.toggleSelection(item.id)
                            } else {
                                onNavigateToVaultViewer(item.id)
                            }
                        },
                        onLongClick = {
                            viewModel.toggleSelection(item.id)
                        }
                    )
                }
            }
        }
    }

    if (showRestoreConfirmDialog) {
        ConfirmationDialog(
            title = "Restore to Gallery",
            message = "Decrypt and restore ${uiState.selectedIds.size} item(s) back to your public gallery?",
            confirmText = "Restore",
            onConfirm = {
                viewModel.restoreSelected { count ->
                    scope.launch {
                        snackbarHostState.showSnackbar("Restored $count item(s) to public gallery")
                    }
                }
            },
            onDismiss = { showRestoreConfirmDialog = false }
        )
    }

    if (showTrashConfirmDialog) {
        ConfirmationDialog(
            title = "Move to Vault Trash",
            message = "Move ${uiState.selectedIds.size} item(s) to Vault Trash? Items are kept encrypted and automatically shredded after 30 days.",
            confirmText = "Move to Trash",
            isDestructive = true,
            onConfirm = { viewModel.moveSelectedToTrash() },
            onDismiss = { showTrashConfirmDialog = false }
        )
    }
}

@Composable
fun VaultThumbnailCard(
    item: VaultItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    loadThumbnailBytes: suspend () -> ByteArray?,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var thumbBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }

    LaunchedEffect(item.id) {
        withContext(Dispatchers.IO) {
            val bytes = loadThumbnailBytes()
            if (bytes != null) {
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bmp != null) {
                    thumbBitmap = bmp.asImageBitmap()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(MotoSurfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (thumbBitmap != null) {
            Image(
                bitmap = thumbBitmap!!,
                contentDescription = item.originalName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = MotoViolet.copy(alpha = 0.5f),
                modifier = Modifier.size(28.dp)
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MotoViolet.copy(alpha = 0.35f))
                    .border(2.dp, MotoViolet, RoundedCornerShape(6.dp))
            )
        }

        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MotoViolet else Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}
