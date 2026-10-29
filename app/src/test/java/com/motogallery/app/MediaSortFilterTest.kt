package com.motogallery.app

import android.net.Uri
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.MediaTypeFilter
import com.motogallery.app.data.model.SortBy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MediaSortFilterTest {

    private lateinit var sampleMedia: List<MediaItem>

    @Before
    fun setup() {
        val dummyUri = org.mockito.Mockito.mock(Uri::class.java)
        sampleMedia = listOf(
            MediaItem(
                id = 1,
                uri = dummyUri,
                displayName = "IMG_20261001.jpg",
                dateTaken = 1727740800000L,
                size = 5000000L,
                isVideo = false,
                isFavorite = true
            ),
            MediaItem(
                id = 2,
                uri = dummyUri,
                displayName = "VID_20261020.mp4",
                dateTaken = 1729382400000L,
                size = 50000000L,
                isVideo = true,
                isFavorite = false
            ),
            MediaItem(
                id = 3,
                uri = dummyUri,
                displayName = "Screenshot_20261015.png",
                dateTaken = 1728950400000L,
                size = 1500000L,
                isVideo = false,
                isFavorite = false
            )
        )
    }

    @Test
    fun testSortByDateDescending() {
        val sorted = sampleMedia.sortedByDescending { it.bestTimestamp }
        assertEquals(2L, sorted[0].id) // Most recent (Oct 20)
        assertEquals(3L, sorted[1].id) // Mid (Oct 15)
        assertEquals(1L, sorted[2].id) // Oldest (Oct 1)
    }

    @Test
    fun testSortByDateAscending() {
        val sorted = sampleMedia.sortedBy { it.bestTimestamp }
        assertEquals(1L, sorted[0].id)
        assertEquals(3L, sorted[1].id)
        assertEquals(2L, sorted[2].id)
    }

    @Test
    fun testSortByNameAscending() {
        val sorted = sampleMedia.sortedBy { it.displayName.lowercase() }
        assertEquals("IMG_20261001.jpg", sorted[0].displayName)
        assertEquals("Screenshot_20261015.png", sorted[1].displayName)
        assertEquals("VID_20261020.mp4", sorted[2].displayName)
    }

    @Test
    fun testSortBySizeDescending() {
        val sorted = sampleMedia.sortedByDescending { it.size }
        assertEquals(50000000L, sorted[0].size)
        assertEquals(5000000L, sorted[1].size)
        assertEquals(1500000L, sorted[2].size)
    }

    @Test
    fun testFilterPhotosOnly() {
        val photos = sampleMedia.filter { !it.isVideo }
        assertEquals(2, photos.size)
        assertTrue(photos.all { !it.isVideo })
    }

    @Test
    fun testFilterVideosOnly() {
        val videos = sampleMedia.filter { it.isVideo }
        assertEquals(1, videos.size)
        assertEquals(2L, videos[0].id)
    }

    @Test
    fun testFilterFavoritesOnly() {
        val favs = sampleMedia.filter { it.isFavorite }
        assertEquals(1, favs.size)
        assertEquals(1L, favs[0].id)
    }
}
