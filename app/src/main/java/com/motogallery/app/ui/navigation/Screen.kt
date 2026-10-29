package com.motogallery.app.ui.navigation

import android.net.Uri

sealed class Screen(val route: String) {
    data object Photos : Screen("photos")
    data object Albums : Screen("albums")
    data object AlbumDetail : Screen("album_detail/{albumId}/{albumName}") {
        fun createRoute(albumId: String, albumName: String): String {
            return "album_detail/${Uri.encode(albumId)}/${Uri.encode(albumName)}"
        }
    }
    data object Videos : Screen("videos")
    data object MediaViewer : Screen("viewer/{mediaId}") {
        fun createRoute(mediaId: Long): String {
            return "viewer/$mediaId"
        }
    }
    data object VideoPlayer : Screen("video_player/{videoUri}") {
        fun createRoute(videoUri: Uri): String {
            return "video_player/${Uri.encode(videoUri.toString())}"
        }
    }
    data object Vault : Screen("vault")
    data object VaultViewer : Screen("vault_viewer/{itemId}") {
        fun createRoute(itemId: Long): String {
            return "vault_viewer/$itemId"
        }
    }
    data object Trash : Screen("trash")
    data object PhotoEditor : Screen("photo_editor/{mediaUri}") {
        fun createRoute(mediaUri: Uri): String {
            return "photo_editor/${Uri.encode(mediaUri.toString())}"
        }
    }
    data object VideoEditor : Screen("video_editor/{mediaUri}") {
        fun createRoute(mediaUri: Uri): String {
            return "video_editor/${Uri.encode(mediaUri.toString())}"
        }
    }
    data object Settings : Screen("settings")
}
