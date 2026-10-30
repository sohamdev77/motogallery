package com.motogallery.app.ui.screens.albums

import android.content.IntentSender
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.MediaThumbnailCard
import com.motogallery.app.ui.components.SelectionActionBar
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoGlassSurface
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.util.IntentUtils

import androidx.compose.runtime.rememberCoroutineScope
import com.motogallery.app.MotoGalleryApp
import com.motogallery.app.data.mediastore.MediaOperationResult
import com.motogallery.app.data.mediastore.MediaOperationsHelper
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun AlbumDetailScreen(
    albumId: String,
    albumName: String,
    viewModel: AlbumsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewer: (Long) -> Unit,
    onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as MotoGalleryApp
    val operationsHelper = remember { MediaOperationsHelper(app) }
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showVaultConfirmDialog by remember { mutableStateOf(false) }

    val isSelectionMode = selectedIds.isNotEmpty()

    LaunchedEffect(albumId) {
        isLoading = true
        items = viewModel.getMediaForAlbum(albumId)
        isLoading = false
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MotoBackground,
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
                // Specular top highlight
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
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MotoGlassSurface)
                            .border(0.5.dp, MotoGlassBorderBrush, CircleShape)
                            .applePressable(pressScale = 0.88f, onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MotoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(
                            text = if (isSelectionMode) "${selectedIds.size} selected" else albumName,
                            color = MotoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.015).sp
                        )
                        if (!isSelectionMode) {
                            Text(
                                text = "${items.size} items",
                                color = MotoTextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            SelectionActionBar(
                visible = isSelectionMode,
                onShare = {
                    val selected = items.filter { it.id in selectedIds }
                    IntentUtils.shareMultipleMedia(context, selected)
                },
                onToggleFavorite = {
                    selectedIds = emptySet()
                },
                onAddToAlbum = {},
                onMoveToVault = { showVaultConfirmDialog = true },
                onDelete = { showDeleteConfirmDialog = true }
            )
        }
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotoViolet)
            }
        } else if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No media in this album", color = MotoTextSecondary)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(items, key = { it.id }) { item ->
                    val isSelected = item.id in selectedIds

                    MediaThumbnailCard(
                        item = item,
                        isSelected = isSelected,
                        isSelectionMode = isSelectionMode,
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                            } else {
                                onNavigateToViewer(item.id)
                            }
                        },
                        onLongClick = {
                            selectedIds = if (isSelected) selectedIds - item.id else selectedIds + item.id
                        }
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        val selectedItems = items.filter { it.id in selectedIds }
        ConfirmationDialog(
            title = "Delete Selected Media",
            message = "Move ${selectedIds.size} item(s) to trash? Media can be restored within 30 days.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                scope.launch {
                    val uris = selectedItems.map { it.uri }
                    when (val res = operationsHelper.requestTrashMedia(uris, trash = true)) {
                        is MediaOperationResult.RequiresConsent -> {
                            onRequestConsent(res.intentSender) { granted ->
                                if (granted) {
                                    items = items.filter { it.id !in selectedIds }
                                    selectedIds = emptySet()
                                }
                            }
                        }
                        is MediaOperationResult.Success -> {
                            items = items.filter { it.id !in selectedIds }
                            selectedIds = emptySet()
                        }
                        else -> {}
                    }
                }
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    if (showVaultConfirmDialog) {
        val selectedItems = items.filter { it.id in selectedIds }
        ConfirmationDialog(
            title = "Move to Private Vault",
            message = "Selected items will be encrypted with AES-256-GCM and stored in your private vault. The public copies will be deleted so they are only visible in your vault.",
            confirmText = "Move to Vault",
            isDestructive = false,
            onConfirm = {
                scope.launch {
                    var imported = 0
                    val urisToDelete = mutableListOf<android.net.Uri>()
                    val pathsToDelete = mutableListOf<String>()

                    for (item in selectedItems) {
                        val res = app.vaultManager.importMedia(
                            sourceUri = item.uri,
                            originalName = item.displayName,
                            mimeType = item.mimeType,
                            isVideo = item.isVideo,
                            size = item.size,
                            durationMs = item.durationMs,
                            width = item.width,
                            height = item.height
                        )
                        if (res.isSuccess) {
                            imported++
                            urisToDelete.add(item.uri)
                            if (item.path.isNotBlank()) pathsToDelete.add(item.path)
                        }
                    }

                    if (imported > 0) {
                        when (val delRes = operationsHelper.requestDeleteMedia(urisToDelete, pathsToDelete)) {
                            is MediaOperationResult.RequiresConsent -> {
                                onRequestConsent(delRes.intentSender) { granted ->
                                    if (granted) {
                                        pathsToDelete.forEach { File(it).delete() }
                                        items = items.filter { it.id !in selectedIds }
                                        selectedIds = emptySet()
                                    }
                                }
                            }
                            is MediaOperationResult.Success -> {
                                pathsToDelete.forEach { File(it).delete() }
                                items = items.filter { it.id !in selectedIds }
                                selectedIds = emptySet()
                            }
                            else -> {
                                pathsToDelete.forEach { File(it).delete() }
                                items = items.filter { it.id !in selectedIds }
                                selectedIds = emptySet()
                            }
                        }
                    }
                }
            },
            onDismiss = { showVaultConfirmDialog = false }
        )
    }
}
