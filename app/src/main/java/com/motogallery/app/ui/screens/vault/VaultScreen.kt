package com.motogallery.app.ui.screens.vault

import android.content.IntentSender
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.VaultItem
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.MediaThumbnailCard
import com.motogallery.app.ui.components.SelectionActionBar
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.AppleSprings
import com.motogallery.app.ui.theme.GlassBadge
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoGlassSurface
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.ui.theme.glassmorphic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.graphicsLayer
import com.motogallery.app.util.DateFormatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaultScreen(
    viewModel: VaultViewModel,
    onNavigateToVaultViewer: (Long) -> Unit,
    onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    var showTrashConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showAddMediaSheet by remember { mutableStateOf(false) }
    var availableMedia by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var selectedMediaToImport by remember { mutableStateOf<Set<Long>>(emptySet()) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
                    onRequestConsent = onRequestConsent
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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MotoGlassDark)
                    .border(
                        width = 0.5.dp,
                        brush = MotoGlassBorderBrush,
                        shape = androidx.compose.ui.graphics.RectangleShape
                    )
            ) {
                // Specular top edge highlight
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MotoGlassSheenBrush)
                )
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
                        letterSpacing = (-0.02).sp,
                        modifier = Modifier.weight(1f)
                    )

                    // Lock button with Apple tactile feedback
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MotoGlassSurface)
                            .border(0.5.dp, MotoGlassBorderBrush, CircleShape)
                            .applePressable(pressScale = 0.90f) { viewModel.lockVault() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Lock Vault",
                            tint = MotoViolet,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (!uiState.isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MotoViolet)
                        .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                        .applePressable(pressScale = 0.90f) {
                            scope.launch(Dispatchers.IO) {
                                val items = viewModel.queryAvailablePublicMedia()
                                withContext(Dispatchers.Main) {
                                    availableMedia = items
                                    selectedMediaToImport = emptySet()
                                    showAddMediaSheet = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Media to Vault",
                        tint = Color.White
                    )
                }
            }
        },
        bottomBar = {
            if (uiState.isSelectionMode) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .glassmorphic(shape = RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Restore button
                        Box(
                            modifier = Modifier
                                .applePressable(pressScale = 0.88f) { showRestoreConfirmDialog = true }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = "Restore",
                                    tint = MotoViolet
                                )
                                Text(
                                    text = "Restore",
                                    color = MotoTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Trash button
                        Box(
                            modifier = Modifier
                                .applePressable(pressScale = 0.88f) { showTrashConfirmDialog = true }
                                .padding(horizontal = 16.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Trash",
                                    tint = MotoError
                                )
                                Text(
                                    text = "Trash",
                                    color = MotoError,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
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
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                val items = viewModel.queryAvailablePublicMedia()
                                withContext(Dispatchers.Main) {
                                    availableMedia = items
                                    selectedMediaToImport = emptySet()
                                    showAddMediaSheet = true
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MotoViolet,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Add Photos & Videos")
                    }
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

    if (showAddMediaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddMediaSheet = false },
            sheetState = addSheetState,
            containerColor = MotoSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Add to Private Vault",
                            color = MotoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Selected items will be encrypted and deleted from public gallery",
                            color = MotoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { showAddMediaSheet = false }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = MotoTextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (availableMedia.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No public photos or videos found on device", color = MotoTextSecondary)
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .height(360.dp)
                    ) {
                        items(availableMedia, key = { it.id }) { item ->
                            val isSelected = item.id in selectedMediaToImport
                            MediaThumbnailCard(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = true,
                                onClick = {
                                    selectedMediaToImport = if (isSelected) {
                                        selectedMediaToImport - item.id
                                    } else {
                                        selectedMediaToImport + item.id
                                    }
                                },
                                onLongClick = {}
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showAddMediaSheet = false
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("External Files", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            val itemsToImport = availableMedia.filter { it.id in selectedMediaToImport }
                            if (itemsToImport.isNotEmpty()) {
                                viewModel.importMediaItems(
                                    items = itemsToImport,
                                    onRequestConsent = onRequestConsent,
                                    onSuccess = { count ->
                                        showAddMediaSheet = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Secured $count item(s) in vault; deleting public copies...")
                                        }
                                    }
                                )
                            }
                        },
                        enabled = selectedMediaToImport.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MotoViolet,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = if (selectedMediaToImport.isEmpty()) "Select Items" else "Secure in Vault (${selectedMediaToImport.size})",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
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

    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 0.92f else 1.0f,
        animationSpec = AppleSprings.BouncySpring,
        label = "vaultCardScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = cardScale
                scaleY = cardScale
            }
            .clip(RoundedCornerShape(10.dp))
            .background(MotoSurfaceVariant)
            .applePressable(
                pressScale = 0.95f,
                onClick = onClick,
                onLongClick = onLongClick
            ),
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
                    .background(MotoViolet.copy(alpha = 0.30f))
                    .border(2.dp, MotoViolet, RoundedCornerShape(10.dp))
            )
        }

        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) MotoViolet else Color.Black.copy(alpha = 0.55f))
                    .border(
                        1.dp,
                        if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        if (item.isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                GlassBadge {
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
}
