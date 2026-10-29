package com.motogallery.app.ui.screens.editor

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.media.editor.PhotoFilter
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import kotlinx.coroutines.launch

enum class AdjustmentProperty(val label: String) {
    BRIGHTNESS("Brightness"),
    CONTRAST("Contrast"),
    SATURATION("Saturation"),
    WARMTH("Warmth"),
    SHADOWS("Shadows")
}

@Composable
fun PhotoEditorScreen(
    mediaUri: Uri,
    viewModel: PhotoEditorViewModel,
    onNavigateBack: () -> Unit,
    onExportComplete: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedAdjustment by remember { mutableStateOf(AdjustmentProperty.BRIGHTNESS) }

    LaunchedEffect(mediaUri) {
        viewModel.loadSource(mediaUri)
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Black,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Cancel", tint = Color.White)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = { viewModel.undo() },
                        enabled = uiState.canUndo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = if (uiState.canUndo) Color.White else MotoTextTertiary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.redo() },
                        enabled = uiState.canRedo
                    ) {
                        Icon(
                            imageVector = Icons.Default.Redo,
                            contentDescription = "Redo",
                            tint = if (uiState.canRedo) Color.White else MotoTextTertiary
                        )
                    }

                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            viewModel.exportCopy { exportedUri ->
                                onExportComplete(exportedUri)
                            }
                        },
                        enabled = !uiState.isExporting,
                        colors = ButtonDefaults.buttonColors(containerColor = MotoViolet),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.isExporting) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Save Copy", color = Color.White, fontWeight = FontWeight.SemiBold)
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
        ) {
            // Live Preview Canvas
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = MotoViolet)
                } else if (uiState.previewBitmap != null) {
                    Image(
                        bitmap = uiState.previewBitmap!!.asImageBitmap(),
                        contentDescription = "Photo Preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Bottom Control Panel
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MotoSurface,
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(bottom = 8.dp)
                ) {
                    // Contextual Controls based on active tab
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        when (uiState.activeTab) {
                            EditorTab.CROP -> {
                                Column {
                                    // Aspect Ratio Selector
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val ratios = listOf(
                                            "Free" to null,
                                            "1:1" to 1f,
                                            "4:3" to 4f / 3f,
                                            "16:9" to 16f / 9f,
                                            "9:16" to 9f / 16f
                                        )
                                        ratios.forEach { (label, ratio) ->
                                            val isSelected = uiState.currentState.cropAspectRatio == ratio
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) MotoViolet else MotoSurfaceVariant)
                                                    .clickable { viewModel.setCropAspectRatio(ratio) }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = label,
                                                    color = if (isSelected) Color.White else MotoTextSecondary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    // Rotate & Flip buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        IconButton(onClick = { viewModel.rotate90() }) {
                                            Icon(imageVector = Icons.Default.RotateRight, contentDescription = "Rotate 90°", tint = Color.White)
                                        }
                                        IconButton(onClick = { viewModel.flipHorizontal() }) {
                                            Icon(imageVector = Icons.Default.Flip, contentDescription = "Flip Horizontal", tint = Color.White)
                                        }
                                        IconButton(onClick = { viewModel.flipVertical() }) {
                                            Icon(imageVector = Icons.Default.Crop, contentDescription = "Flip Vertical", tint = Color.White)
                                        }
                                    }

                                    // Straighten slider
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "Straighten: ${uiState.currentState.straightenAngle.toInt()}°", color = MotoTextSecondary, fontSize = 12.sp)
                                        Slider(
                                            value = uiState.currentState.straightenAngle,
                                            onValueChange = { viewModel.setStraighten(it) },
                                            valueRange = -45f..45f,
                                            colors = SliderDefaults.colors(thumbColor = MotoViolet, activeTrackColor = MotoViolet),
                                            modifier = Modifier.weight(1f).padding(start = 8.dp)
                                        )
                                    }
                                }
                            }

                            EditorTab.ADJUST -> {
                                Column {
                                    // Adjustment selector pills
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        AdjustmentProperty.values().forEach { prop ->
                                            val isSelected = selectedAdjustment == prop
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(if (isSelected) MotoViolet else MotoSurfaceVariant)
                                                    .clickable { selectedAdjustment = prop }
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = prop.label,
                                                    color = if (isSelected) Color.White else MotoTextSecondary,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Value Slider
                                    val currentValue = when (selectedAdjustment) {
                                        AdjustmentProperty.BRIGHTNESS -> uiState.currentState.brightness
                                        AdjustmentProperty.CONTRAST -> uiState.currentState.contrast
                                        AdjustmentProperty.SATURATION -> uiState.currentState.saturation
                                        AdjustmentProperty.WARMTH -> uiState.currentState.warmth
                                        AdjustmentProperty.SHADOWS -> uiState.currentState.shadowsHighlights
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${currentValue.toInt()}",
                                            color = MotoTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.width(36.dp)
                                        )
                                        Slider(
                                            value = currentValue,
                                            onValueChange = { valChange ->
                                                when (selectedAdjustment) {
                                                    AdjustmentProperty.BRIGHTNESS -> viewModel.setBrightness(valChange)
                                                    AdjustmentProperty.CONTRAST -> viewModel.setContrast(valChange)
                                                    AdjustmentProperty.SATURATION -> viewModel.setSaturation(valChange)
                                                    AdjustmentProperty.WARMTH -> viewModel.setWarmth(valChange)
                                                    AdjustmentProperty.SHADOWS -> viewModel.setShadowsHighlights(valChange)
                                                }
                                            },
                                            valueRange = -100f..100f,
                                            colors = SliderDefaults.colors(
                                                thumbColor = MotoViolet,
                                                activeTrackColor = MotoViolet,
                                                inactiveTrackColor = MotoSurfaceVariant
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }

                            EditorTab.FILTERS -> {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    PhotoFilter.values().forEach { filter ->
                                        val isSelected = uiState.currentState.filter == filter
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) MotoViolet else MotoSurfaceVariant)
                                                .border(1.dp, if (isSelected) MotoViolet else MotoBorder, RoundedCornerShape(12.dp))
                                                .clickable { viewModel.setFilter(filter) }
                                                .padding(horizontal = 14.dp, vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = filter.displayName,
                                                color = if (isSelected) Color.White else MotoTextPrimary,
                                                fontSize = 13.sp,
                                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Main Tab Switcher (Crop, Adjust, Filters)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(androidx.compose.foundation.BorderStroke(0.5.dp, MotoBorder))
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        TabItem(
                            icon = Icons.Default.Crop,
                            label = "Crop",
                            isSelected = uiState.activeTab == EditorTab.CROP,
                            onClick = { viewModel.setActiveTab(EditorTab.CROP) }
                        )
                        TabItem(
                            icon = Icons.Default.Tune,
                            label = "Adjust",
                            isSelected = uiState.activeTab == EditorTab.ADJUST,
                            onClick = { viewModel.setActiveTab(EditorTab.ADJUST) }
                        )
                        TabItem(
                            icon = Icons.Default.AutoFixHigh,
                            label = "Filters",
                            isSelected = uiState.activeTab == EditorTab.FILTERS,
                            onClick = { viewModel.setActiveTab(EditorTab.FILTERS) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) MotoViolet else MotoTextSecondary,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = label,
            color = if (isSelected) MotoTextPrimary else MotoTextSecondary,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}
