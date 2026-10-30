package com.motogallery.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Settings
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motogallery.app.ui.navigation.Screen
import com.motogallery.app.ui.theme.AppleSprings
import com.motogallery.app.ui.theme.LocalWallpaperConfig
import com.motogallery.app.ui.theme.MotoGlassBorderBrush
import com.motogallery.app.ui.theme.MotoGlassDark
import com.motogallery.app.ui.theme.MotoGlassSheenBrush
import com.motogallery.app.ui.theme.MotoTextPrimary
import com.motogallery.app.ui.theme.MotoTextTertiary
import com.motogallery.app.ui.theme.applePressable

data class NavTabItem(
    val screen: Screen,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navTabs = listOf(
    NavTabItem(
        screen = Screen.Photos,
        title = "Photos",
        selectedIcon = Icons.Filled.PhotoLibrary,
        unselectedIcon = Icons.Outlined.PhotoLibrary
    ),
    NavTabItem(
        screen = Screen.SwipeClean,
        title = "Swipe",
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome
    ),
    NavTabItem(
        screen = Screen.Albums,
        title = "Albums",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder
    ),
    NavTabItem(
        screen = Screen.Videos,
        title = "Videos",
        selectedIcon = Icons.Filled.PlayCircle,
        unselectedIcon = Icons.Outlined.PlayCircle
    ),
    NavTabItem(
        screen = Screen.Vault,
        title = "Private",
        selectedIcon = Icons.Filled.Lock,
        unselectedIcon = Icons.Outlined.Lock
    ),
    NavTabItem(
        screen = Screen.Settings,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
)

/**
 * Apple-Style Floating Glass Dock Navigation Bar
 *
 * Translucent frosted glass pill with specular reflection along top edge,
 * critically damped liquid spring transitions on tab switch,
 * and instant tactile compression on touch down.
 */
@Composable
fun MotoBottomNavBar(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val wallpaperConfig = LocalWallpaperConfig.current
    val primaryColor = MaterialTheme.colorScheme.primary

    val barBg = if (wallpaperConfig.isWallpaperActive) {
        wallpaperConfig.bottomBarColor.copy(alpha = 0.82f)
    } else {
        MotoGlassDark
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .navigationBarsPadding()
    ) {
        // Floating Frosted Glass Dock Container
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
                                Color.White.copy(alpha = 0.22f), // Apple specular top light
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    ),
                    RoundedCornerShape(26.dp)
                )
                .padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                navTabs.forEach { tab ->
                    val isSelected = currentRoute == tab.screen.route

                    val iconTint by animateColorAsState(
                        targetValue = if (isSelected) primaryColor else MotoTextTertiary,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "iconTint"
                    )

                    val textTint by animateColorAsState(
                        targetValue = if (isSelected) MotoTextPrimary else MotoTextTertiary,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "textTint"
                    )

                    val pillBgColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor.copy(alpha = 0.20f) else Color.Transparent,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "pillBgColor"
                    )

                    val pillBorderColor by animateColorAsState(
                        targetValue = if (isSelected) primaryColor.copy(alpha = 0.38f) else Color.Transparent,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "pillBorderColor"
                    )

                    val activeScale by animateFloatAsState(
                        targetValue = if (isSelected) 1.05f else 1.0f,
                        animationSpec = AppleSprings.BouncySpring,
                        label = "activeScale"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(18.dp))
                            .applePressable(
                                pressScale = 0.88f,
                                onClick = {
                                    if (!isSelected) {
                                        onNavigate(tab.screen)
                                    }
                                }
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Liquid frosted pill indicator
                            Box(
                                modifier = Modifier
                                    .scale(activeScale)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(pillBgColor)
                                    .border(
                                        BorderStroke(0.5.dp, pillBorderColor),
                                        RoundedCornerShape(14.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 3.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.title,
                                    tint = iconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = tab.title,
                                color = textTint,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                letterSpacing = (-0.1).sp,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
