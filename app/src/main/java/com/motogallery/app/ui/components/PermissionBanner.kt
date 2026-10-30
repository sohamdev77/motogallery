package com.motogallery.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoOrange
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable

@Composable
fun PermissionBanner(
    isPartialAccess: Boolean,
    onGrantOrManage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isPartialAccess) MotoOrange else MotoViolet

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MotoGlassDark)
            .background(MotoGlassSheenBrush)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        listOf(
                            if (isPartialAccess) MotoOrange.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.22f),
                            Color.White.copy(alpha = 0.04f)
                        )
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.16f))
                    .border(BorderStroke(0.5.dp, accentColor.copy(alpha = 0.35f)), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPartialAccess) Icons.Default.LockOpen else Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isPartialAccess) "Selected Photos Access" else "Allow Media Access",
                    color = MotoTextPrimary,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.2).sp
                )
                Text(
                    text = if (isPartialAccess) {
                        "MotoGallery can only view selected items. Manage to grant more."
                    } else {
                        "Allow access to your device's photos and videos to browse your gallery."
                    },
                    color = MotoTextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent)
                        )
                    )
                    .border(BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)), RoundedCornerShape(12.dp))
                    .applePressable(pressScale = 0.92f) { onGrantOrManage() }
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isPartialAccess) "Manage" else "Grant",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
