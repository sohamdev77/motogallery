package com.motogallery.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import com.motogallery.app.data.model.MediaItem
import java.util.ArrayList

object IntentUtils {

    fun shareSingleMedia(context: Context, item: MediaItem) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = item.mimeType
            putExtra(Intent.EXTRA_STREAM, item.uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share via"))
    }

    fun shareMultipleMedia(context: Context, items: List<MediaItem>) {
        if (items.isEmpty()) return
        if (items.size == 1) {
            shareSingleMedia(context, items.first())
            return
        }

        val uris = ArrayList<Uri>(items.map { it.uri })
        val commonMime = if (items.all { !it.isVideo }) "image/*" else if (items.all { it.isVideo }) "video/*" else "*/*"

        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = commonMime
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share ${items.size} items via"))
    }

    fun setAsWallpaperOrContact(context: Context, item: MediaItem) {
        val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
            setDataAndType(item.uri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra("mimeType", item.mimeType)
        }
        context.startActivity(Intent.createChooser(intent, "Set as"))
    }

    fun openWithExternalApp(context: Context, item: MediaItem) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(item.uri, item.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            context.startActivity(Intent.createChooser(intent, "Open with"))
        } catch (_: Exception) {}
    }

    fun openAppSettings(context: Context) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
