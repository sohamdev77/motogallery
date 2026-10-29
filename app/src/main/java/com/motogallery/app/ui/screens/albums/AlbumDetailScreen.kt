package com.motogallery.app.ui.screens.albums

import android.content.IntentSender
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import com.motogallery.app.util.IntentUtils

@Composable
fun AlbumDetailScreen(
    albumId: String,
    albumName: String,
    viewModel: AlbumsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToViewer: (Long) -> Unit,
    onRequestConsent: (IntentSender) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var items by remember { mutableStateOf<List<MediaItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedIds by remember { mutableStateOf<Set<Long>>(emptySet()) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

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
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MotoBackground,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MotoTextPrimary
                        )
                    }

                    Column(modifier = Modifier.padding(start = 4.dp)) {
                        Text(
                            text = if (isSelectionMode) "${selectedIds.size} selected" else albumName,
                            color = MotoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
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
                onMoveToVault = {},
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
                contentPadding = PaddingValues(2.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
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
}
