package com.motogallery.app

import com.motogallery.app.util.DateFormatter
import com.motogallery.app.util.FileSizeFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {

    @Test
    fun testFileSizeFormatter() {
        assertEquals("0 B", FileSizeFormatter.format(0L))
        assertEquals("512 B", FileSizeFormatter.format(512L))
        assertEquals("1 KB", FileSizeFormatter.format(1024L))
        assertEquals("1.5 MB", FileSizeFormatter.format(1572864L))
        assertEquals("2 GB", FileSizeFormatter.format(2147483648L))
    }

    @Test
    fun testVideoDurationFormatter() {
        assertEquals("0:00", DateFormatter.formatDuration(0L))
        assertEquals("0:45", DateFormatter.formatDuration(45000L))
        assertEquals("3:12", DateFormatter.formatDuration(192000L))
        assertEquals("1:15:30", DateFormatter.formatDuration(4530000L))
    }
}
