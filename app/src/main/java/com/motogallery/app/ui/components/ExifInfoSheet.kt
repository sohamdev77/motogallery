package com.motogallery.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.MediaMetadata
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExifInfoSheet(
    metadata: MediaMetadata?,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val wallpaperConfig = LocalWallpaperConfig.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val sheetBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.surfaceColor.copy(alpha = 0.90f)
    } else {
        MotoGlassDark
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetBg,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color.White.copy(alpha = 0.28f))
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        if (metadata == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Loading metadata...", color = MotoTextSecondary)
            }
            return@ModalBottomSheet
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MotoGlassSheenBrush)
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Details",
                color = MotoTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Inset Grouped Frosted Glass Card for Metadata
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.03f))
                            )
                        ),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Column {
                    // File Name & Path
                    InfoRow(
                        icon = Icons.Default.Info,
                        title = metadata.title,
                        subtitle = "${metadata.mimeType} · ${FileSizeFormatter.format(metadata.sizeBytes)}"
                    )

                    // Date & Time
                    if (metadata.dateTakenFormatted.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.Schedule,
                            title = "Date Taken",
                            subtitle = metadata.dateTakenFormatted
                        )
                    }

                    // Dimensions & Megapixels
                    if (metadata.width > 0 && metadata.height > 0) {
                        val mp = (metadata.width.toLong() * metadata.height.toLong()) / 1_000_000.0
                        val mpStr = if (mp >= 1.0) String.format("%.1f MP", mp) else ""
                        InfoRow(
                            icon = Icons.Default.Straighten,
                            title = "Resolution",
                            subtitle = "${metadata.width} × ${metadata.height} $mpStr"
                        )
                    }

                    // Camera Hardware / EXIF parameters
                    val cameraDetails = buildList {
                        metadata.cameraModel?.let { add(it) }
                        metadata.aperture?.let { add(it) }
                        metadata.shutterSpeed?.let { add(it) }
                        metadata.iso?.let { add("ISO $it") }
                        metadata.focalLength?.let { add(it) }
                    }.joinToString(" · ")

                    if (cameraDetails.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.CameraAlt,
                            title = metadata.cameraMake ?: "Camera Details",
                            subtitle = cameraDetails
                        )
                    }

                    // File path
                    if (metadata.path.isNotBlank()) {
                        InfoRow(
                            icon = Icons.Default.Folder,
                            title = "Storage Path",
                            subtitle = metadata.path
                        )
                    }

                    // GPS Coordinates if present
                    if (metadata.latitude != null && metadata.longitude != null) {
                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            title = "GPS Coordinates",
                            subtitle = "${String.format("%.4f", metadata.latitude)}°, ${String.format("%.4f", metadata.longitude)}°"
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Apple-style Glass Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(primaryColor)
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                                    )
                                )
                                .border(
                                    BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                                    RoundedCornerShape(14.dp)
                                )
                                .applePressable(pressScale = 0.96f) {
                                    val geoUri = Uri.parse("geo:${metadata.latitude},${metadata.longitude}?q=${metadata.latitude},${metadata.longitude}(Photo+Location)")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {}
                                }
                                .padding(vertical = 11.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "View on Map",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 9.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f)),
                    RoundedCornerShape(10.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MotoTextPrimary,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            )
            Text(
                text = subtitle,
                color = MotoTextSecondary,
                fontSize = 12.5.sp,
                lineHeight = 17.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
