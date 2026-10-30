package com.motogallery.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.theme.GlassBadge
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoBackground
import com.motogallery.app.ui.theme.MotoBorder
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoPink
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextSecondary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.MotoViolet
import com.motogallery.app.ui.theme.applePressable
import com.motogallery.app.ui.theme.glassmorphic

@Composable
fun MotoTopBar(
    title: String,
    subtitle: String? = null,
    isSelectionMode: Boolean = false,
    selectedCount: Int = 0,
    onClearSelection: () -> Unit = {},
    onSelectAll: () -> Unit = {},
    currentGridColumns: Int = 3,
    onToggleGridColumns: () -> Unit = {},
    onOpenFilterSort: () -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isSearchActive by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val wallpaperConfig = LocalWallpaperConfig.current

    val topBarBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.topBarColor.copy(alpha = 0.82f)
    } else {
        MotoGlassDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(topBarBg)
            .background(MotoGlassSheenBrush)
            .border(
                BorderStroke(
                    0.5.dp,
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.04f))
                    )
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                // Apple-style Tactile Clear Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .applePressable(pressScale = 0.88f) { onClearSelection() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear Selection",
                        tint = MotoTextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = "$selectedCount selected",
                    color = MotoTextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.3).sp,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                // Select All Pill
                GlassBadge(
                    backgroundColor = MotoViolet.copy(alpha = 0.22f),
                    borderColor = MotoViolet.copy(alpha = 0.40f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.applePressable(pressScale = 0.92f) { onSelectAll() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SelectAll,
                        contentDescription = "Select All",
                        tint = MotoViolet,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "All",
                        color = MotoTextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else if (isSearchActive) {
                // Apple-style Frosted Glass Search Pill
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color.White.copy(alpha = 0.09f))
                        .border(
                            BorderStroke(
                                0.5.dp,
                                Brush.verticalGradient(
                                    listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.04f))
                                )
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MotoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = MotoTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Normal
                            ),
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                            modifier = Modifier.weight(1f),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search media...",
                                        color = MotoTextTertiary,
                                        fontSize = 15.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.08f))
                        .applePressable(pressScale = 0.88f) {
                            isSearchActive = false
                            onSearchQueryChange("")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close search",
                        tint = MotoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        color = MotoTextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    )
                    if (subtitle != null) {
                        Text(
                            text = " · $subtitle",
                            color = MotoTextTertiary,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(start = 6.dp)
                        )
                    }
                }

                // Apple-style Glass Action Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                ) {
                    // Search Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(
                                BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                                CircleShape
                            )
                            .applePressable(pressScale = 0.88f) { isSearchActive = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MotoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Grid Columns Switcher Pill (cycles 2 -> 3 -> 4 -> 5 -> 2)
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.06f))
                            .border(
                                BorderStroke(0.5.dp, Color.White.copy(alpha = 0.12f)),
                                CircleShape
                            )
                            .applePressable(pressScale = 0.88f) { onToggleGridColumns() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GridView,
                            contentDescription = "Change Grid Columns ($currentGridColumns)",
                            tint = MotoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Filter & Sort Pill Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                            .border(
                                BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                                CircleShape
                            )
                            .applePressable(pressScale = 0.88f) { onOpenFilterSort() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filter and Sort",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
