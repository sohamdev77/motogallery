package com.motogallery.app.ui.screens.vault

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.VaultItem
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoViolet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun VaultViewerScreen(
    itemId: Long,
    viewModel: VaultViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val item = remember(uiState.items, itemId) {
        uiState.items.firstOrNull { it.id == itemId }
    }

    var decryptedBitmap by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    LaunchedEffect(itemId) {
        if (item != null) {
            isLoading = true
            withContext(Dispatchers.IO) {
                val bytes = viewModel.getDecryptedBytes(item)
                if (bytes != null) {
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    if (bmp != null) {
                        decryptedBitmap = bmp.asImageBitmap()
                    }
                }
            }
            isLoading = false
        }
    }

    if (item == null) {
        Box(modifier = modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
            Text(text = "Item not found in vault", color = Color.White)
        }
        return
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MotoViolet)
            }
        } else if (decryptedBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 4f)
                            offset = if (scale > 1f) offset + pan else Offset.Zero
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = decryptedBitmap!!,
                    contentDescription = item.originalName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                )
            }
        }

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = item.originalName,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Bottom actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 32.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { showRestoreConfirmDialog = true }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Restore",
                        tint = MotoViolet
                    )
                    Text(text = "Restore", color = Color.White, fontSize = 11.sp)
                }
            }

            IconButton(onClick = { showDeleteConfirmDialog = true }) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Trash",
                        tint = MotoError
                    )
                    Text(text = "Trash", color = Color.White, fontSize = 11.sp)
                }
            }
        }
    }

    if (showRestoreConfirmDialog) {
        ConfirmationDialog(
            title = "Restore to Gallery",
            message = "Decrypt and restore \"${item.originalName}\" to your public gallery?",
            confirmText = "Restore",
            onConfirm = {
                viewModel.toggleSelection(item.id)
                viewModel.restoreSelected {
                    onNavigateBack()
                }
            },
            onDismiss = { showRestoreConfirmDialog = false }
        )
    }

    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Move to Vault Trash",
            message = "Move \"${item.originalName}\" to Vault Trash? Files in trash are automatically purged after 30 days.",
            confirmText = "Move to Trash",
            isDestructive = true,
            onConfirm = {
                viewModel.toggleSelection(item.id)
                viewModel.moveSelectedToTrash()
                onNavigateBack()
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }
}
