package com.motogallery.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateFormatter {

    private val fullDateTimeFormat = SimpleDateFormat("MMM d, yyyy · h:mm a", Locale.getDefault())
    private val dayHeaderFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
    private val monthHeaderFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun formatFullDateTime(timestampMs: Long): String {
        if (timestampMs <= 0L) return "Unknown date"
        return fullDateTimeFormat.format(Date(timestampMs))
    }

    fun formatShortDate(timestampMs: Long): String {
        if (timestampMs <= 0L) return "Unknown"
        return shortDateFormat.format(Date(timestampMs))
    }

    fun formatHeaderDate(timestampMs: Long): String {
        if (timestampMs <= 0L) return "Unknown Date"
        val now = Calendar.getInstance()
        val itemCal = Calendar.getInstance().apply { timeInMillis = timestampMs }

        return when {
            isSameDay(now, itemCal) -> "Today"
            isYesterday(now, itemCal) -> "Yesterday"
            now.get(Calendar.YEAR) == itemCal.get(Calendar.YEAR) -> {
                SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timestampMs))
            }
            else -> dayHeaderFormat.format(Date(timestampMs))
        }
    }

    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0L) return "0:00"
        val totalSeconds = durationMs / 1000
        val seconds = totalSeconds % 60
        val minutes = (totalSeconds / 60) % 60
        val hours = totalSeconds / 3600

        return if (hours > 0) {
            String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.US, "%d:%02d", minutes, seconds)
        }
    }

    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(today: Calendar, other: Calendar): Boolean {
        val yesterday = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -1) }
        return isSameDay(yesterday, other)
    }
}
