package com.motogallery.app.ui.screens.trash

import android.content.IntentSender
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.motogallery.app.data.model.TrashedMediaItem
import com.motogallery.app.ui.components.ConfirmationDialog
import androidx.compose.ui.graphics.Brush
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoOrange
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import kotlinx.coroutines.launch

@Composable
fun TrashScreen(
    viewModel: TrashViewModel,
    onNavigateBack: () -> Unit,
    onRequestConsent: (IntentSender, ((Boolean) -> Unit)?) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val wallpaperConfig = LocalWallpaperConfig.current

    var showPermanentDeleteDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = wallpaperConfig.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            val topBarBg = if (wallpaperConfig.isWallpaperActive) {
                wallpaperConfig.topBarColor.copy(alpha = 0.85f)
            } else {
                MotoGlassDark
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(topBarBg)
                    .background(MotoGlassSheenBrush)
                    .border(
                        androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))
                            )
                        )
                    )
            ) {
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
                            .background(Color.White.copy(alpha = 0.08f))
                            .applePressable(pressScale = 0.88f) { onNavigateBack() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MotoTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (uiState.isSelectionMode) "${uiState.selectedIds.size} selected" else "Recently Deleted",
                        color = MotoTextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp,
                        modifier = Modifier.padding(start = 12.dp)
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
                        .navigationBarsPadding()
                        .clip(RoundedCornerShape(26.dp))
                        .background(MotoGlassDark)
                        .background(MotoGlassSheenBrush)
                        .border(
                            androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.28f), Color.White.copy(alpha = 0.05f))
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MotoViolet)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                                    )
                                )
                                .border(androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
                                .applePressable(pressScale = 0.94f) { showRestoreDialog = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Restore, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(text = "Restore (${uiState.selectedIds.size})", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(MotoError)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                                    )
                                )
                                .border(androidx.compose.foundation.BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(14.dp))
                                .applePressable(pressScale = 0.94f) { showPermanentDeleteDialog = true }
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.DeleteForever, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(text = "Delete Forever", color = Color.White, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MotoViolet)
            }
        } else if (uiState.allTrashItems.isEmpty()) {
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
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = MotoTextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Trash is empty",
                        color = MotoTextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Deleted photos and videos are kept for 30 days before permanent deletion.",
                        color = MotoTextSecondary,
                        fontSize = 13.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Info Banner item
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MotoSurfaceVariant)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = "Items show remaining retention days. Once expired, they are shredded and permanently deleted.",
                            color = MotoTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                // Vault Trash section if present
                if (uiState.vaultTrashItems.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "PRIVATE VAULT TRASH",
                            color = MotoOrange,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 4.dp)
                        )
                    }

                    items(uiState.vaultTrashItems, key = { it.id }) { item ->
                        TrashedItemCard(
                            item = item,
                            isSelected = item.id in uiState.selectedIds,
                            onToggleSelect = { viewModel.toggleSelection(item.id) }
                        )
                    }
                }

                // Public Media Trash section
                if (uiState.publicTrashItems.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = "PUBLIC GALLERY TRASH",
                            color = MotoViolet,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(start = 8.dp, top = 16.dp, bottom = 4.dp)
                        )
                    }

                    items(uiState.publicTrashItems, key = { it.id }) { item ->
                        TrashedItemCard(
                            item = item,
                            isSelected = item.id in uiState.selectedIds,
                            onToggleSelect = { viewModel.toggleSelection(item.id) }
                        )
                    }
                }
            }
        }
    }

    if (showRestoreDialog) {
        ConfirmationDialog(
            title = "Restore Media",
            message = "Restore ${uiState.selectedIds.size} item(s) back to their original albums?",
            confirmText = "Restore",
            onConfirm = {
                viewModel.restoreSelected(
                    onRequestConsent = onRequestConsent,
                    onSuccess = {
                        scope.launch { snackbarHostState.showSnackbar("Restored media items") }
                    }
                )
            },
            onDismiss = { showRestoreDialog = false }
        )
    }

    if (showPermanentDeleteDialog) {
        ConfirmationDialog(
            title = "Permanent Deletion",
            message = "Permanently shred and delete ${uiState.selectedIds.size} item(s)? This action cannot be undone.",
            confirmText = "Delete Forever",
            isDestructive = true,
            onConfirm = {
                viewModel.permanentlyDeleteSelected(
                    onRequestConsent = onRequestConsent,
                    onSuccess = {
                        scope.launch { snackbarHostState.showSnackbar("Media permanently removed") }
                    }
                )
            },
            onDismiss = { showPermanentDeleteDialog = false }
        )
    }
}

@Composable
fun TrashedItemCard(
    item: TrashedMediaItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(6.dp))
            .background(MotoSurfaceVariant)
            .clickable(onClick = onToggleSelect)
    ) {
        if (!item.isVaultItem) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MotoOrange.copy(alpha = 0.5f),
                    modifier = Modifier.size(32.dp)
                )
            }
        }

        // Days remaining badge
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(4.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black.copy(alpha = 0.75f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = "${item.daysRemaining}d left",
                color = if (item.daysRemaining <= 3) MotoError else Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Selection Checkbox
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .size(20.dp)
                .clip(CircleShape)
                .background(if (isSelected) MotoViolet else Color.Black.copy(alpha = 0.5f))
                .border(1.dp, if (isSelected) MotoViolet else Color.White.copy(alpha = 0.8f), CircleShape),
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
}
