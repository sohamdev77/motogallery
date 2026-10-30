package com.motogallery.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.data.model.MediaTypeFilter
import com.motogallery.app.data.model.SortBy
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSortSheet(
    sheetState: SheetState,
    currentSort: SortBy,
    currentFilter: MediaTypeFilter,
    onSortSelected: (SortBy) -> Unit,
    onFilterSelected: (MediaTypeFilter) -> Unit,
    onDismiss: () -> Unit
) {
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MotoGlassSheenBrush)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Sort & Filter",
                color = MotoTextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.4).sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "FILTER MEDIA",
                color = primaryColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Inset Grouped Frosted Glass Card for Filters
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.03f))
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(4.dp)
            ) {
                Column {
                    MediaTypeFilter.values().forEach { filter ->
                        val isSelected = filter == currentFilter
                        val rowBg by animateColorAsState(
                            targetValue = if (isSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "filterRowBg"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(rowBg)
                                .applePressable(pressScale = 0.98f) { onFilterSelected(filter) }
                                .padding(vertical = 11.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = filter.label,
                                color = if (isSelected) MotoTextPrimary else MotoTextSecondary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "SORT ORDER",
                color = primaryColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Inset Grouped Frosted Glass Card for Sort
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(
                        BorderStroke(
                            0.5.dp,
                            Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.03f))
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(4.dp)
            ) {
                Column {
                    SortBy.values().forEach { sort ->
                        val isSelected = sort == currentSort
                        val rowBg by animateColorAsState(
                            targetValue = if (isSelected) primaryColor.copy(alpha = 0.16f) else Color.Transparent,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                            label = "sortRowBg"
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(rowBg)
                                .applePressable(pressScale = 0.98f) { onSortSelected(sort) }
                                .padding(vertical = 11.dp, horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sort.label,
                                color = if (isSelected) MotoTextPrimary else MotoTextSecondary,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = primaryColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
