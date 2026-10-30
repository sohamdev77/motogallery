package com.motogallery.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.theme.AppleSprings
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoError
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable

/**
 * Apple-Style Floating Glassmorphic Selection Action Bar
 *
 * Appears with physical spring overshoot, features translucent frosted glass,
 * specular light-catching top border, and instant tactile compression on buttons.
 */
@Composable
fun SelectionActionBar(
    visible: Boolean,
    onShare: () -> Unit,
    onToggleFavorite: () -> Unit,
    onAddToAlbum: () -> Unit,
    onMoveToVault: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val wallpaperConfig = LocalWallpaperConfig.current

    val barBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.surfaceColor.copy(alpha = 0.85f)
    } else {
        MotoGlassDark
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it }
        ) + fadeIn(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        exit = slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioNoBouncy,
                stiffness = Spring.StiffnessMedium
            ),
            targetOffsetY = { it }
        ) + fadeOut(animationSpec = spring(stiffness = Spring.StiffnessMedium)),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .navigationBarsPadding()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(barBg)
                    .background(MotoGlassSheenBrush)
                    .border(
                        BorderStroke(
                            1.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.28f), // Specular light rim
                                    Color.White.copy(alpha = 0.05f)
                                )
                            )
                        ),
                        RoundedCornerShape(26.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SelectionActionItem(
                        icon = Icons.Default.Share,
                        label = "Share",
                        tint = MotoTextPrimary,
                        onClick = onShare
                    )
                    SelectionActionItem(
                        icon = Icons.Default.Favorite,
                        label = "Favorite",
                        tint = MotoPink,
                        onClick = onToggleFavorite
                    )
                    SelectionActionItem(
                        icon = Icons.Default.CreateNewFolder,
                        label = "Album",
                        tint = MotoViolet,
                        onClick = onAddToAlbum
                    )
                    SelectionActionItem(
                        icon = Icons.Default.Lock,
                        label = "Vault",
                        tint = MotoViolet,
                        onClick = onMoveToVault
                    )
                    SelectionActionItem(
                        icon = Icons.Default.Delete,
                        label = "Delete",
                        tint = MotoError,
                        onClick = onDelete
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionActionItem(
    icon: ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .applePressable(pressScale = 0.86f, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = MotoTextPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-0.1).sp,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
