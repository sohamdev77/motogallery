package com.motogallery.app.ui.navigation

import android.content.IntentSender
import android.net.Uri
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.motogallery.app.ui.components.MotoBottomNavBar
import com.motogallery.app.ui.screens.albums.AlbumDetailScreen
import com.motogallery.app.ui.screens.albums.AlbumsScreen
import com.motogallery.app.ui.screens.albums.AlbumsViewModel
import com.motogallery.app.ui.screens.editor.PhotoEditorScreen
import com.motogallery.app.ui.screens.editor.PhotoEditorViewModel
import com.motogallery.app.ui.screens.editor.VideoEditorScreen
import com.motogallery.app.ui.screens.editor.VideoEditorViewModel
import com.motogallery.app.ui.screens.photos.PhotosScreen
import com.motogallery.app.ui.screens.photos.PhotosViewModel
import com.motogallery.app.ui.screens.settings.SettingsScreen
import com.motogallery.app.ui.screens.settings.SettingsViewModel
import com.motogallery.app.ui.screens.trash.TrashScreen
import com.motogallery.app.ui.screens.trash.TrashViewModel
import com.motogallery.app.ui.screens.vault.VaultScreen
import com.motogallery.app.ui.screens.vault.VaultViewModel
import com.motogallery.app.ui.screens.vault.VaultViewerScreen
import com.motogallery.app.ui.screens.videos.VideosScreen
import com.motogallery.app.ui.screens.videos.VideosViewModel
import com.motogallery.app.ui.screens.viewer.MediaViewerScreen
import com.motogallery.app.ui.screens.viewer.MediaViewerViewModel
import com.motogallery.app.ui.screens.viewer.VideoPlayerScreen
import com.motogallery.app.ui.theme.MotoBackground

@Composable
fun AppNavGraph(
    navController: NavHostController,
    onRequestConsent: (IntentSender) -> Unit,
    onRequestPermissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Hide bottom navigation bar on fullscreen viewers and editors
    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Photos.route,
        Screen.Albums.route,
        Screen.Videos.route,
        Screen.Vault.route,
        Screen.Settings.route
    )

    // Shared / scoped view models
    val photosViewModel: PhotosViewModel = viewModel()
    val albumsViewModel: AlbumsViewModel = viewModel()
    val videosViewModel: VideosViewModel = viewModel()
    val vaultViewModel: VaultViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()
    val trashViewModel: TrashViewModel = viewModel()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MotoBackground,
        bottomBar = {
            if (shouldShowBottomBar) {
                MotoBottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Photos.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Photos.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Photos Screen
            composable(Screen.Photos.route) {
                PhotosScreen(
                    viewModel = photosViewModel,
                    onNavigateToViewer = { mediaId ->
                        navController.navigate(Screen.MediaViewer.createRoute(mediaId))
                    },
                    onRequestConsent = onRequestConsent,
                    onRequestPermissions = onRequestPermissions
                )
            }

            // Albums Screen
            composable(Screen.Albums.route) {
                AlbumsScreen(
                    viewModel = albumsViewModel,
                    onNavigateToAlbumDetail = { albumId, albumName ->
                        navController.navigate(Screen.AlbumDetail.createRoute(albumId, albumName))
                    }
                )
            }

            // Album Detail Screen
            composable(
                route = Screen.AlbumDetail.route,
                arguments = listOf(
                    navArgument("albumId") { type = NavType.StringType },
                    navArgument("albumName") { type = NavType.StringType }
                )
            ) { backStack ->
                val albumId = Uri.decode(backStack.arguments?.getString("albumId") ?: "")
                val albumName = Uri.decode(backStack.arguments?.getString("albumName") ?: "")
                AlbumDetailScreen(
                    albumId = albumId,
                    albumName = albumName,
                    viewModel = albumsViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToViewer = { mediaId ->
                        navController.navigate(Screen.MediaViewer.createRoute(mediaId))
                    },
                    onRequestConsent = onRequestConsent
                )
            }

            // Videos Screen
            composable(Screen.Videos.route) {
                VideosScreen(
                    viewModel = videosViewModel,
                    onNavigateToVideoPlayer = { uri ->
                        navController.navigate(Screen.VideoPlayer.createRoute(uri))
                    }
                )
            }

            // Private Vault Screen
            composable(Screen.Vault.route) {
                VaultScreen(
                    viewModel = vaultViewModel,
                    onNavigateToVaultViewer = { itemId ->
                        navController.navigate(Screen.VaultViewer.createRoute(itemId))
                    },
                    onRequestConsent = onRequestConsent
                )
            }

            // Private Vault Viewer Screen
            composable(
                route = Screen.VaultViewer.route,
                arguments = listOf(navArgument("itemId") { type = NavType.LongType })
            ) { backStack ->
                val itemId = backStack.arguments?.getLong("itemId") ?: 0L
                VaultViewerScreen(
                    itemId = itemId,
                    viewModel = vaultViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Trash / Recently Deleted Screen
            composable(Screen.Trash.route) {
                TrashScreen(
                    viewModel = trashViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRequestConsent = onRequestConsent
                )
            }

            // Fullscreen Media Viewer
            composable(
                route = Screen.MediaViewer.route,
                arguments = listOf(navArgument("mediaId") { type = NavType.LongType })
            ) { backStack ->
                val mediaId = backStack.arguments?.getLong("mediaId") ?: 0L
                val viewerViewModel: MediaViewerViewModel = viewModel()
                MediaViewerScreen(
                    mediaId = mediaId,
                    viewModel = viewerViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToPhotoEditor = { uri ->
                        navController.navigate(Screen.PhotoEditor.createRoute(uri))
                    },
                    onNavigateToVideoEditor = { uri ->
                        navController.navigate(Screen.VideoEditor.createRoute(uri))
                    },
                    onNavigateToVideoPlayer = { uri ->
                        navController.navigate(Screen.VideoPlayer.createRoute(uri))
                    },
                    onRequestConsent = onRequestConsent
                )
            }

            // Video Player Screen
            composable(
                route = Screen.VideoPlayer.route,
                arguments = listOf(navArgument("videoUri") { type = NavType.StringType })
            ) { backStack ->
                val uriStr = Uri.decode(backStack.arguments?.getString("videoUri") ?: "")
                VideoPlayerScreen(
                    videoUri = Uri.parse(uriStr),
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Built-in Photo Editor Screen
            composable(
                route = Screen.PhotoEditor.route,
                arguments = listOf(navArgument("mediaUri") { type = NavType.StringType })
            ) { backStack ->
                val uriStr = Uri.decode(backStack.arguments?.getString("mediaUri") ?: "")
                val editorViewModel: PhotoEditorViewModel = viewModel()
                PhotoEditorScreen(
                    mediaUri = Uri.parse(uriStr),
                    viewModel = editorViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onExportComplete = {
                        navController.popBackStack()
                    }
                )
            }

            // Built-in Video Editor Screen
            composable(
                route = Screen.VideoEditor.route,
                arguments = listOf(navArgument("mediaUri") { type = NavType.StringType })
            ) { backStack ->
                val uriStr = Uri.decode(backStack.arguments?.getString("mediaUri") ?: "")
                val videoEditorViewModel: VideoEditorViewModel = viewModel()
                VideoEditorScreen(
                    videoUri = Uri.parse(uriStr),
                    viewModel = videoEditorViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onExportComplete = {
                        navController.popBackStack()
                    }
                )
            }

            // Settings Screen
            composable(Screen.Settings.route) {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    onNavigateToTrash = {
                        navController.navigate(Screen.Trash.route)
                    }
                )
            }
        }
    }
}
