package com.motogallery.app.ui.screens.wallpaper

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoGlassSurface
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.ui.theme.glassmorphic
import com.motogallery.app.ui.theme.WallpaperTheme

val ACCENT_OPTIONS = listOf(
    Pair("Default", ""),
    Pair("Violet", "#A855F7"),
    Pair("Cyan", "#06B6D4"),
    Pair("Pink", "#EC65B5"),
    Pair("Orange", "#F97316"),
    Pair("Emerald", "#10B981")
)

@Composable
fun WallpaperPickerScreen(
    viewModel: WallpaperPickerViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentId by viewModel.currentWallpaperId.collectAsState()
    val dimming by viewModel.wallpaperDimming.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val accentColor by viewModel.accentColor.collectAsState()
    val previewWallpaper by viewModel.previewWallpaper.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val wallpaperConfig = LocalWallpaperConfig.current

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = wallpaperConfig.backgroundColor,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(wallpaperConfig.topBarColor.copy(alpha = 0.88f))
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
                        .height(60.dp)
                        .padding(horizontal = 8.dp),
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
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MotoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)
                    ) {
                        Text(
                            text = "Wallpaper Themes",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MotoTextPrimary,
                            letterSpacing = (-0.015).sp
                        )
                        Text(
                            text = "Whole-app personalized backgrounds",
                            fontSize = 12.sp,
                            color = MotoTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MotoGlassSurface)
                            .border(0.5.dp, MotoGlassBorderBrush, CircleShape)
                            .applePressable(pressScale = 0.88f) { viewModel.resetToDefault() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset to Default",
                            tint = MotoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header / Intro
            item(span = { GridItemSpan(2) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(shape = RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "BUILT-IN WALLPAPERS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Select a theme to transform MotoGallery's interface with an edge-to-edge frosted aesthetic. 100% offline and battery optimized.",
                            fontSize = 13.sp,
                            color = MotoTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // Wallpapers Grid
            items(viewModel.wallpapers, key = { it.id }) { theme ->
                val isSelected = currentId.equals(theme.id, ignoreCase = true)
                WallpaperItemCard(
                    theme = theme,
                    isSelected = isSelected,
                    onPreview = { viewModel.setPreviewWallpaper(theme) },
                    onApply = { viewModel.applyWallpaper(theme) }
                )
            }

            // Customization Section: Dimming, Theme Mode, Accents
            item(span = { GridItemSpan(2) }) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .glassmorphic(shape = RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "CUSTOMIZATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dimming Slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Wallpaper Dimming",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = MotoTextPrimary
                            )
                            Text(
                                text = "${(dimming * 100).toInt()}%",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = dimming,
                            onValueChange = { viewModel.setDimming(it) },
                            valueRange = 0.0f..0.80f,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MotoSurfaceVariant
                            )
                        )
                        Text(
                            text = "Adjust darkness scrim behind gallery cards for optimal readability",
                            fontSize = 12.sp,
                            color = MotoTextTertiary
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Theme Mode Selector
                        Text(
                            text = "Theme Mode",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MotoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("dark" to "Dark", "light" to "Light", "system" to "System").forEach { (mode, label) ->
                                val selected = themeMode == mode
                                FilterChip(
                                    selected = selected,
                                    onClick = { viewModel.setThemeMode(mode) },
                                    label = { Text(label, fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                        selectedLabelColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Accent Color Palette
                        Text(
                            text = "Accent Color",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MotoTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ACCENT_OPTIONS.forEach { (name, hex) ->
                                val isChosen = if (hex.isEmpty()) accentColor.isEmpty() else accentColor.equals(hex, ignoreCase = true)
                                val chipColor = if (hex.isEmpty()) MotoViolet else Color(android.graphics.Color.parseColor(hex))

                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(chipColor)
                                        .border(
                                            width = if (isChosen) 2.5.dp else 1.dp,
                                            color = if (isChosen) Color.White else Color.Transparent,
                                            shape = CircleShape
                                        )
                                        .clickable { viewModel.setAccentColor(hex) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isChosen) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = name,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Fullscreen Wallpaper Preview Modal
    previewWallpaper?.let { previewTheme ->
        FullscreenWallpaperPreviewDialog(
            theme = previewTheme,
            dimming = dimming,
            onDismiss = { viewModel.setPreviewWallpaper(null) },
            onApply = {
                viewModel.applyWallpaper(previewTheme)
                viewModel.setPreviewWallpaper(null)
            }
        )
    }
}

@Composable
private fun WallpaperItemCard(
    theme: WallpaperTheme,
    isSelected: Boolean,
    onPreview: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MotoSurfaceVariant else MotoSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 0.5.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MotoBorder
        ),
        modifier = modifier
            .fillMaxWidth()
            .applePressable(pressScale = 0.97f) { onPreview() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Wallpaper Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                if (theme.drawableRes != 0) {
                    Image(
                        painter = painterResource(theme.drawableRes),
                        contentDescription = theme.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF0B1020), Color(0xFF131A2E))
                                )
                            )
                    )
                }

                // Selected Badge
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Active",
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Preview Button Overlay
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { onPreview() }
                        .padding(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Preview",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Accents
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MotoTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // Accent dot
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(theme.primaryAccent)
                )
            }

            Text(
                text = theme.description,
                fontSize = 11.sp,
                color = MotoTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 14.sp,
                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
            )

            // Apply / Active Button
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .applePressable(pressScale = 0.93f, onClick = onApply),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.White
                )
            ) {
                Text(
                    text = if (isSelected) "Active" else "Apply",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FullscreenWallpaperPreviewDialog(
    theme: WallpaperTheme,
    dimming: Float,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full wallpaper background
            if (theme.drawableRes != 0) {
                Image(
                    painter = painterResource(theme.drawableRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MotoBackground)
                )
            }

            // Dimming scrim
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = dimming))
            )

            // Simulated Frosted Gallery UI Overlay
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(16.dp)
            ) {
                // Mock Top Bar
                Surface(
                    color = MotoSurface.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "MotoGallery Preview",
                            fontWeight = FontWeight.Bold,
                            color = MotoTextPrimary,
                            fontSize = 16.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(theme.primaryAccent)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = theme.displayName,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated Content Card
                Surface(
                    color = MotoSurface.copy(alpha = 0.82f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Sample Gallery UI",
                            fontWeight = FontWeight.Bold,
                            color = MotoTextPrimary,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Notice how content cards remain clear, high-contrast, and readable against ${theme.displayName} wallpaper background.",
                            fontSize = 12.sp,
                            color = MotoTextSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Dialog Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MotoSurfaceVariant),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Close", color = MotoTextPrimary, fontWeight = FontWeight.Medium)
                    }

                    Button(
                        onClick = onApply,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryAccent),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Apply Theme", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
