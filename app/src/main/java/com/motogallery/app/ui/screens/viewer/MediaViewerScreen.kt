package com.motogallery.app.ui.screens.viewer

import android.content.IntentSender
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.components.ExifInfoSheet
import com.motogallery.app.ui.components.InputTextDialog
import com.motogallery.app.ui.components.ZoomableImageView
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.DateFormatter
import com.motogallery.app.util.IntentUtils

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MediaViewerScreen(
    mediaId: Long,
    viewModel: MediaViewerViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPhotoEditor: (Uri) -> Unit,
    onNavigateToVideoEditor: (Uri) -> Unit,
    onNavigateToVideoPlayer: (Uri) -> Unit,
    onRequestConsent: (IntentSender) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var showExifSheet by remember { mutableStateOf(false) }
    val exifSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    LaunchedEffect(mediaId) {
        viewModel.loadMedia(mediaId)
    }

    if (uiState.isLoading || uiState.mediaList.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(MotoBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MotoViolet)
        }
        return
    }

    val pagerState = rememberPagerState(
        initialPage = uiState.currentIndex,
        pageCount = { uiState.mediaList.size }
    )

    LaunchedEffect(pagerState.currentPage) {
        viewModel.onPageChanged(pagerState.currentPage)
    }

    val currentItem = uiState.currentItem ?: return

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Fullscreen Horizontal Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val item = uiState.mediaList[page]

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ZoomableImageView(
                    item = item,
                    onTap = { viewModel.toggleBarsVisibility() }
                )

                // If video, show center play button overlay
                if (item.isVideo) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                            .clickable { onNavigateToVideoPlayer(item.uri) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = "Play Video",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
            }
        }

        // Floating Top Bar with gradient protection
        AnimatedVisibility(
            visible = uiState.isBarsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 6.dp)
                    ) {
                        Text(
                            text = currentItem.displayName,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1
                        )
                        Text(
                            text = DateFormatter.formatShortDate(currentItem.bestTimestamp),
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }

                    Box {
                        IconButton(onClick = { showMoreMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showMoreMenu,
                            onDismissRequest = { showMoreMenu = false },
                            modifier = Modifier.background(MotoSurface)
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Rename", color = MotoTextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    showRenameDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Set as wallpaper / contact", color = MotoTextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    IntentUtils.setAsWallpaperOrContact(context, currentItem)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(text = "Open with external app", color = MotoTextPrimary) },
                                onClick = {
                                    showMoreMenu = false
                                    IntentUtils.openWithExternalApp(context, currentItem)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Bottom Bar with gradient protection
        AnimatedVisibility(
            visible = uiState.isBarsVisible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Share
                    IconButton(onClick = { IntentUtils.shareSingleMedia(context, currentItem) }) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            tint = Color.White
                        )
                    }

                    // Edit (Photo Editor or Video Editor)
                    IconButton(
                        onClick = {
                            if (currentItem.isVideo) {
                                onNavigateToVideoEditor(currentItem.uri)
                            } else {
                                onNavigateToPhotoEditor(currentItem.uri)
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MotoViolet
                        )
                    }

                    // Favorite
                    IconButton(onClick = { viewModel.toggleFavoriteCurrent() }) {
                        Icon(
                            imageVector = if (currentItem.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (currentItem.isFavorite) MotoPink else Color.White
                        )
                    }

                    // Info / EXIF
                    IconButton(onClick = { showExifSheet = true }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Details",
                            tint = Color.White
                        )
                    }

                    // Delete
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MotoError
                        )
                    }
                }
            }
        }
    }

    // EXIF Metadata Bottom Sheet
    if (showExifSheet) {
        ExifInfoSheet(
            metadata = uiState.currentMetadata,
            sheetState = exifSheetState,
            onDismiss = { showExifSheet = false }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirmDialog) {
        ConfirmationDialog(
            title = "Delete Media",
            message = "Move \"${currentItem.displayName}\" to trash? System authorization will be requested.",
            confirmText = "Delete",
            isDestructive = true,
            onConfirm = {
                viewModel.requestDeleteCurrent(
                    onConsentRequired = onRequestConsent,
                    onDeleted = onNavigateBack
                )
            },
            onDismiss = { showDeleteConfirmDialog = false }
        )
    }

    // Rename Dialog
    if (showRenameDialog) {
        InputTextDialog(
            title = "Rename File",
            initialValue = currentItem.displayName,
            confirmText = "Rename",
            onConfirm = { newName ->
                viewModel.renameCurrent(
                    newName = newName,
                    onConsentRequired = onRequestConsent,
                    onSuccess = {
                        viewModel.loadMetadataForCurrent()
                    }
                )
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}
