package com.motogallery.app.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.MediaMetadata
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoSurface
import com.motogallery.app.ui.theme.MotoSurfaceVariant
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.util.FileSizeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExifInfoSheet(
    metadata: MediaMetadata?,
    sheetState: SheetState,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MotoSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
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
                .padding(horizontal = 24.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Details",
                color = MotoTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                    title = "Location on Storage",
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

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val geoUri = Uri.parse("geo:${metadata.latitude},${metadata.longitude}?q=${metadata.latitude},${metadata.longitude}(Photo+Location)")
                        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                        try {
                            context.startActivity(mapIntent)
                        } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MotoViolet),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "View on Map", color = MotoTextPrimary)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun InfoRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MotoSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MotoViolet,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = MotoTextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                color = MotoTextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}
