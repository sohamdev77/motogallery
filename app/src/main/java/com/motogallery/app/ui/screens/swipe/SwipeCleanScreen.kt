package com.motogallery.app.ui.screens.swipe

import android.content.IntentSender
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.SwipeCategory
import com.motogallery.app.data.model.SwipeMinSize
import com.motogallery.app.data.model.SwipeSort
import com.motogallery.app.ui.components.ConfirmationDialog
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoEmerald
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoOrange
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.DateFormatter
import com.motogallery.app.util.FileSizeFormatter
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeCleanScreen(
    viewModel: SwipeCleanViewModel,
    onRequestConsent: (IntentSender) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val wallpaperConfig = LocalWallpaperConfig.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = wallpaperConfig.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = wallpaperConfig.topBarColor,
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 10.dp, bottom = 8.dp)
                ) {
                    // Header Title & Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Swipe Clean",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MotoTextPrimary
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    imageVector = Icons.Default.FilterList,
                                    contentDescription = "Filter & Sort",
                                    tint = MotoTextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.resetSession() }) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Session",
                                    tint = MotoTextSecondary
                                )
                            }
                        }
                    }

                    // Smart Categories Horizontal Carousel
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SwipeCategory.entries.forEach { cat ->
                            val isSelected = uiState.category == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.loadCategoryMedia(cat) },
                                label = { Text(cat.displayName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                    selectedLabelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Storage & Review Metric Dashboard Card
            Surface(
                color = wallpaperConfig.surfaceColor,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        val reviewed = uiState.reviewedKept.size + uiState.reviewedTrashQueue.size
                        Text(
                            text = "Reviewed $reviewed / ${uiState.totalOriginalCount}",
                            fontSize = 12.sp,
                            color = MotoTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = FileSizeFormatter.format(uiState.recoverableBytes),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.recoverableBytes > 0) MotoOrange else MotoTextPrimary
                            )
                            Text(
                                text = " potentially recoverable",
                                fontSize = 11.sp,
                                color = MotoTextTertiary
                            )
                        }
                    }

                    // Move Queued Trash Button
                    if (uiState.reviewedTrashQueue.isNotEmpty()) {
                        Button(
                            onClick = { viewModel.setShowTrashConfirmDialog(true) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MotoError),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Trash (${uiState.reviewedTrashQueue.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Card Stack Area or Finished State
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else if (uiState.unreviewedItems.isEmpty()) {
                    // Empty or Completed State
                    CategoryCompletedCard(
                        category = uiState.category,
                        keptCount = uiState.reviewedKept.size,
                        trashedCount = uiState.reviewedTrashQueue.size,
                        recoverableBytes = uiState.recoverableBytes,
                        onTrashClick = { viewModel.setShowTrashConfirmDialog(true) },
                        onResetClick = { viewModel.resetSession() }
                    )
                } else {
                    // Interactive Card Stack (renders top 3 items)
                    val items = uiState.unreviewedItems
                    val topIndex = 0

                    if (items.size > 2) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.88f)
                                .aspectRatio(0.72f)
                                .offset(y = 20.dp)
                                .scale(0.90f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MotoSurfaceVariant.copy(alpha = 0.5f))
                        ) {}
                    }

                    if (items.size > 1) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(0.72f)
                                .offset(y = 10.dp)
                                .scale(0.95f),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MotoSurfaceVariant.copy(alpha = 0.8f))
                        ) {}
                    }

                    // Top Active Swipable Card
                    val topItem = items[topIndex]
                    SwipableCard(
                        item = topItem,
                        onSwipeLeft = { viewModel.swipeLeft(topItem) },
                        onSwipeRight = { viewModel.swipeRight(topItem) },
                        onSwipeUp = { viewModel.swipeUp(topItem) }
                    )
                }
            }

            // Bottom Tap Buttons: Undo, Trash, Preview, Keep
            Surface(
                color = wallpaperConfig.surfaceColor,
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Undo Button
                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (uiState.canUndo) MotoSurfaceVariant else MotoSurfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) MotoTextPrimary else MotoTextTertiary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Left Trash Button
                    IconButton(
                        onClick = {
                            if (uiState.unreviewedItems.isNotEmpty()) {
                                viewModel.swipeLeft(uiState.unreviewedItems.first())
                            }
                        },
                        enabled = uiState.unreviewedItems.isNotEmpty(),
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MotoError.copy(alpha = 0.15f))
                            .border(1.dp, MotoError, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Trash",
                            tint = MotoError,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Preview Button
                    IconButton(
                        onClick = {
                            if (uiState.unreviewedItems.isNotEmpty()) {
                                viewModel.swipeUp(uiState.unreviewedItems.first())
                            }
                        },
                        enabled = uiState.unreviewedItems.isNotEmpty(),
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MotoSurfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Preview",
                            tint = MotoTextPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    // Right Keep Button
                    IconButton(
                        onClick = {
                            if (uiState.unreviewedItems.isNotEmpty()) {
                                viewModel.swipeRight(uiState.unreviewedItems.first())
                            }
                        },
                        enabled = uiState.unreviewedItems.isNotEmpty(),
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MotoEmerald.copy(alpha = 0.15f))
                            .border(1.dp, MotoEmerald, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Keep",
                            tint = MotoEmerald,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }

    // Filter & Sort Bottom Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            containerColor = MotoSurface,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "SORT BY",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SwipeSort.entries.forEach { sortOption ->
                        FilterChip(
                            selected = uiState.sort == sortOption,
                            onClick = { viewModel.setSort(sortOption) },
                            label = { Text(sortOption.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = "MINIMUM FILE SIZE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SwipeMinSize.entries.forEach { sizeOption ->
                        FilterChip(
                            selected = uiState.minSize == sizeOption,
                            onClick = { viewModel.setMinSize(sizeOption) },
                            label = { Text(sizeOption.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Trash Confirmation Dialog
    if (uiState.showTrashConfirmDialog) {
        ConfirmationDialog(
            title = "Move to Recently Deleted",
            message = "Move ${uiState.reviewedTrashQueue.size} items (${FileSizeFormatter.format(uiState.recoverableBytes)}) to Recently Deleted?\n\nAndroid system permission will be requested. Items can be restored anytime within 30 days.",
            confirmText = "Move to Trash",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                viewModel.executeTrashQueue(onRequestConsent)
            },
            onDismiss = {
                viewModel.setShowTrashConfirmDialog(false)
            }
        )
    }

    // Fullscreen Preview Dialog (When swiped up or tapped preview)
    uiState.previewMediaItem?.let { previewItem ->
        FullscreenSwipePreviewDialog(
            item = previewItem,
            onDismiss = { viewModel.setPreviewMediaItem(null) },
            onKeep = {
                viewModel.swipeRight(previewItem)
                viewModel.setPreviewMediaItem(null)
            },
            onTrash = {
                viewModel.swipeLeft(previewItem)
                viewModel.setPreviewMediaItem(null)
            }
        )
    }
}

@Composable
private fun SwipableCard(
    item: MediaItem,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isPlayingVideo by remember { mutableStateOf(false) }

    val rotation = (offsetX.value / 25f).coerceIn(-22f, 22f)
    val swipeThresholdPx = with(density) { 130.dp.toPx() }
    val upThresholdPx = with(density) { -140.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.72f)
            .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
            .rotate(rotation)
            .pointerInput(item.id) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch {
                            offsetX.snapTo(offsetX.value + dragAmount.x)
                            offsetY.snapTo(offsetY.value + dragAmount.y)
                        }
                    },
                    onDragEnd = {
                        scope.launch {
                            when {
                                offsetX.value < -swipeThresholdPx -> {
                                    // Fling off left (Trash)
                                    offsetX.animateTo(-1200f, tween(200))
                                    onSwipeLeft()
                                }
                                offsetX.value > swipeThresholdPx -> {
                                    // Fling off right (Keep)
                                    offsetX.animateTo(1200f, tween(200))
                                    onSwipeRight()
                                }
                                offsetY.value < upThresholdPx -> {
                                    // Swipe up (Preview)
                                    offsetY.animateTo(-1200f, tween(200))
                                    onSwipeUp()
                                }
                                else -> {
                                    // Snap back with spring physics
                                    launch { offsetX.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
                                    launch { offsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) }
                                }
                            }
                        }
                    }
                )
            }
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MotoSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MotoBorder),
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    if (item.isVideo) {
                        isPlayingVideo = !isPlayingVideo
                    }
                }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (item.isVideo && isPlayingVideo) {
                    // Inline Video Playback using ExoPlayer
                    InlineVideoPlayer(videoUri = item.uri)
                } else {
                    // Image / Video Thumbnail
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.uri)
                            .crossfade(true)
                            .size(1080)
                            .build(),
                        contentDescription = item.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Video Play Icon Overlay
                    if (item.isVideo) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Video",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Bottom Metadata Gradient Scrim
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = item.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = DateFormatter.formatDate(item.bestTimestamp),
                                fontSize = 12.sp,
                                color = MotoTextSecondary
                            )
                            Text(
                                text = FileSizeFormatter.format(item.size),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MotoOrange
                            )
                        }
                    }
                }

                // Dynamic Swipe Badges Overlay
                val badgeAlpha = (Math.abs(offsetX.value) / swipeThresholdPx).coerceIn(0f, 1f)
                val upBadgeAlpha = (Math.abs(offsetY.value) / Math.abs(upThresholdPx)).coerceIn(0f, 1f)

                if (offsetX.value > 20f) {
                    // KEEP Badge (Green)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(20.dp)
                            .rotate(-15f)
                            .border(2.5.dp, MotoEmerald.copy(alpha = badgeAlpha), RoundedCornerShape(10.dp))
                            .background(MotoEmerald.copy(alpha = 0.25f * badgeAlpha))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "KEEP",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MotoEmerald.copy(alpha = badgeAlpha),
                            letterSpacing = 2.sp
                        )
                    }
                } else if (offsetX.value < -20f) {
                    // TRASH Badge (Red)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(20.dp)
                            .rotate(15f)
                            .border(2.5.dp, MotoError.copy(alpha = badgeAlpha), RoundedCornerShape(10.dp))
                            .background(MotoError.copy(alpha = 0.25f * badgeAlpha))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "TRASH",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = MotoError.copy(alpha = badgeAlpha),
                            letterSpacing = 2.sp
                        )
                    }
                } else if (offsetY.value < -20f) {
                    // PREVIEW Badge (Cyan/Violet)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(20.dp)
                            .border(2.5.dp, MotoViolet.copy(alpha = upBadgeAlpha), RoundedCornerShape(10.dp))
                            .background(MotoViolet.copy(alpha = 0.25f * upBadgeAlpha))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "PREVIEW",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MotoViolet.copy(alpha = upBadgeAlpha),
                            letterSpacing = 2.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InlineVideoPlayer(videoUri: android.net.Uri) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(ExoMediaItem.fromUri(videoUri))
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(videoUri) {
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = false
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun CategoryCompletedCard(
    category: SwipeCategory,
    keptCount: Int,
    trashedCount: Int,
    recoverableBytes: Long,
    onTrashClick: () -> Unit,
    onResetClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MotoSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MotoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MotoEmerald.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.TaskAlt,
                    contentDescription = null,
                    tint = MotoEmerald,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "All ${category.displayName} Reviewed!",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MotoTextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$keptCount items kept • $trashedCount marked for trash",
                fontSize = 13.sp,
                color = MotoTextSecondary,
                textAlign = TextAlign.Center
            )

            if (trashedCount > 0) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    color = MotoOrange.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MotoOrange.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${FileSizeFormatter.format(recoverableBytes)} potentially recoverable",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MotoOrange,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onTrashClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Move $trashedCount Items to Trash",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onResetClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MotoSurfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Review Again",
                    fontWeight = FontWeight.Medium,
                    color = MotoTextPrimary
                )
            }
        }
    }
}

@Composable
private fun FullscreenSwipePreviewDialog(
    item: MediaItem,
    onDismiss: () -> Unit,
    onKeep: () -> Unit,
    onTrash: () -> Unit
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .crossfade(true)
                    .build(),
                contentDescription = item.displayName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )

            // Top Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.displayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${FileSizeFormatter.format(item.size)} • ${DateFormatter.formatDate(item.bestTimestamp)}",
                        fontSize = 12.sp,
                        color = MotoTextSecondary
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }

            // Bottom Floating Actions: Trash vs Keep
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onTrash,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoError),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Trash", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onKeep,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoEmerald),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Keep", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
