package com.motogallery.app.util

import java.text.DecimalFormat

object FileSizeFormatter {

    private val df = DecimalFormat("#.##")

    fun format(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        val index = digitGroups.coerceIn(0, units.size - 1)
        val value = bytes / Math.pow(1024.0, index.toDouble())
        return "${df.format(value)} ${units[index]}"
    }
}
