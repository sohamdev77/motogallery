package com.motogallery.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.ui.theme.AppleSprings
import com.motogallery.app.ui.theme.GlassBadge
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.util.DateFormatter

/**
 * Apple-Style Media Thumbnail Card
 *
 * Implements:
 * - Instant tactile compression on pointer-down (Response Principle - Kill Latency)
 * - Critically damped spring rebound on release
 * - Frosted glass video duration pill and favorite badges
 * - Smooth spring selection scaling (0.92f) with specular neon rim
 */
@Composable
fun MediaThumbnailCard(
    item: MediaItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 0.92f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "thumbnailSelectionScale"
    )

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .scale(selectionScale)
            .clip(RoundedCornerShape(8.dp))
            .background(MotoSurfaceVariant)
            .applePressable(
                pressScale = 0.95f,
                onLongClick = onLongClick,
                onClick = onClick
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(item.uri)
                .crossfade(true)
                .build(),
            contentDescription = item.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Apple-style Glassmorphic Selection Overlay
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MotoViolet.copy(alpha = 0.32f))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
                    .border(
                        BorderStroke(
                            2.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.6f), MotoViolet)
                            )
                        ),
                        RoundedCornerShape(8.dp)
                    )
            )
        }

        // Selection Checkbox
        if (isSelectionMode) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) MotoViolet else Color.Black.copy(alpha = 0.50f)
                    )
                    .border(
                        BorderStroke(
                            1.5.dp,
                            if (isSelected) Color.White.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.4f)
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }

        // Frosted Glass Favorite Badge
        if (item.isFavorite && !isSelectionMode) {
            GlassBadge(
                backgroundColor = Color.Black.copy(alpha = 0.55f),
                borderColor = Color.White.copy(alpha = 0.25f),
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Favorite",
                    tint = MotoPink,
                    modifier = Modifier.size(12.dp)
                )
            }
        }

        // Frosted Glass Video Duration Badge
        if (item.isVideo) {
            GlassBadge(
                backgroundColor = Color.Black.copy(alpha = 0.65f),
                borderColor = Color.White.copy(alpha = 0.22f),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = DateFormatter.formatDuration(item.durationMs),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp,
                    modifier = Modifier.padding(start = 3.dp)
                )
            }
        }
    }
}
