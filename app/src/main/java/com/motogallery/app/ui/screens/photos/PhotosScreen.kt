package com.motogallery.app.ui.screens.photos

import android.content.IntentSender
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.FilterSortSheet
import com.motogallery.app.ui.components.MediaThumbnailCard
import com.motogallery.app.ui.components.MotoTopBar
import com.motogallery.app.ui.components.PermissionBanner
import com.motogallery.app.ui.components.SelectionActionBar
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.DateFormatter
import com.motogallery.app.util.IntentUtils
import com.motogallery.app.util.PermissionUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhotosScreen(
    viewModel: PhotosViewModel,
    onNavigateToViewer: (Long) -> Unit,
    onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val hasFullAccess = remember(context) { PermissionUtils.hasFullMediaAccess(context) }
    val isPartialAccess = remember(context) { PermissionUtils.hasPartialMediaAccess(context) }

    var showFilterSortSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showVaultConfirmDialog by remember { mutableStateOf(false) }

    val errorMessage by viewModel.errorMessage.collectAsState()
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val wallpaperConfig = LocalWallpaperConfig.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = wallpaperConfig.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            MotoTopBar(
                title = "Photos",
                subtitle = if (uiState.mediaItems.isNotEmpty()) "${uiState.mediaItems.size} items" else null,
                isSelectionMode = uiState.isSelectionMode,
                selectedCount = uiState.selectedIds.size,
                onClearSelection = { viewModel.clearSelection() },
                onSelectAll = { viewModel.selectAll() },
                currentGridColumns = uiState.gridColumns,
                onToggleGridColumns = { viewModel.cycleGridColumns() },
                onOpenFilterSort = { showFilterSortSheet = true },
                searchQuery = uiState.searchQuery,
                onSearchQueryChange = { viewModel.setSearchQuery(it) }
            )
        },
        bottomBar = {
            SelectionActionBar(
                visible = uiState.isSelectionMode,
                onShare = {
                    IntentUtils.shareMultipleMedia(context, uiState.selectedItems)
                },
                onToggleFavorite = {
                    viewModel.toggleFavoriteSelected()
                },
                onAddToAlbum = {
                    // Quick add or selection prompt
                },
                onMoveToVault = {
                    showVaultConfirmDialog = true
                },
                onDelete = {
                    showDeleteConfirmDialog = true
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Permission Notice
            if (!hasFullAccess) {
                PermissionBanner(
                    isPartialAccess = isPartialAccess,
                    onGrantOrManage = onRequestPermissions
                )
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MotoViolet)
                }
            } else if (uiState.mediaItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = MotoViolet,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "No matching media found" else "No photos or videos yet",
                            color = MotoTextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (uiState.searchQuery.isNotBlank()) "Try a different search keyword" else "Media stored on your phone will appear here automatically",
                            color = MotoTextSecondary,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                // Group media by date header for sleek timeline display
                val groupedByDate = remember(uiState.mediaItems, uiState.sortBy) {
                    if (uiState.sortBy == com.motogallery.app.data.model.SortBy.DATE_DESC ||
                        uiState.sortBy == com.motogallery.app.data.model.SortBy.DATE_ASC) {
                        uiState.mediaItems.groupBy { DateFormatter.formatHeaderDate(it.bestTimestamp) }
                    } else {
                        mapOf("" to uiState.mediaItems)
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(uiState.gridColumns),
                    contentPadding = PaddingValues(2.dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    groupedByDate.forEach { (dateHeader, itemsInGroup) ->
                        if (dateHeader.isNotBlank()) {
                            item(span = { GridItemSpan(maxLineSpan) }) {
                                Text(
                                    text = dateHeader,
                                    color = MotoTextPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 10.dp, top = 16.dp, bottom = 6.dp)
                                )
                            }
                        }

                        items(itemsInGroup, key = { it.id }) { item ->
                            val isSelected = item.id in uiState.selectedIds

                            MediaThumbnailCard(
                                item = item,
                                isSelected = isSelected,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = {
                                    if (uiState.isSelectionMode) {
                                        viewModel.toggleSelection(item.id)
                                    } else {
                                        onNavigateToViewer(item.id)
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
        }
    }

    // Filter & Sort Modal Bottom Sheet
    if (showFilterSortSheet) {
        FilterSortSheet(
            sheetState = sheetState,
            currentSort = uiState.sortBy,
            currentFilter = uiState.mediaTypeFilter,
            onSortSelected = {
                viewModel.setSortBy(it)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSortSheet = false }
            },
            onFilterSelected = {
                viewModel.setMediaTypeFilter(it)
                scope.launch { sheetState.hide() }.invokeOnCompletion { showFilterSortSheet = false }
            },
            onDismiss = { showFilterSortSheet = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Delete Selected Media",
            message = "Move ${uiState.selectedIds.size} item(s) to trash? You will be prompted by the system for authorization.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.requestDeleteSelected(
                    onRequestConsent = onRequestConsent,
                    onComplete = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Media moved to trash")
                        }
                    }
                )
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // Move to Vault confirmation dialog
    if (showVaultConfirmDialog) {
        ConfirmationDialog(
            title = "Move to Private Vault",
            message = "Selected items will be encrypted with AES-256-GCM and stored in your private vault. The public copies will be deleted so they are only visible in your vault.",
            confirmText = "Move to Vault",
            isDestructive = false,
            onConfirm = {
                viewModel.moveToVault(
                    items = uiState.selectedItems,
                    onRequestConsent = onRequestConsent,
                    onSuccess = { count ->
                        scope.launch {
                            snackbarHostState.showSnackbar("Secured $count item(s) in vault; deleting public copies...")
                        }
                    }
                )
            },
            onDismiss = { showVaultConfirmDialog = false }
        )
    }
}
