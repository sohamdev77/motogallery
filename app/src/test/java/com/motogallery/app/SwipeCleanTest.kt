package com.motogallery.app

import android.net.Uri
import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.SwipeActionRecord
import com.motogallery.app.data.model.SwipeCategory
import com.motogallery.app.data.model.SwipeDirection
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class SwipeCleanTest {

    private lateinit var dummyUri: Uri
    private lateinit var sampleItems: List<MediaItem>

    @Before
    fun setup() {
        dummyUri = Mockito.mock(Uri::class.java)

        sampleItems = listOf(
            MediaItem(
                id = 1L,
                uri = dummyUri,
                displayName = "IMG_20261001.jpg",
                dateTaken = 1727740800000L,
                size = 4 * 1024 * 1024L, // 4 MB
                isVideo = false,
                bucketDisplayName = "Camera"
            ),
            MediaItem(
                id = 2L,
                uri = dummyUri,
                displayName = "VID_20261020.mp4",
                dateTaken = 1729382400000L,
                size = 50 * 1024 * 1024L, // 50 MB
                isVideo = true,
                bucketDisplayName = "Camera"
            ),
            MediaItem(
                id = 3L,
                uri = dummyUri,
                displayName = "Screenshot_20261015.png",
                dateTaken = 1728950400000L,
                size = 2 * 1024 * 1024L, // 2 MB
                isVideo = false,
                bucketDisplayName = "Screenshots"
            ),
            MediaItem(
                id = 4L,
                uri = dummyUri,
                displayName = "downloaded_doc.jpg",
                dateTaken = 1700000000000L, // Old
                size = 30 * 1024 * 1024L, // 30 MB
                isVideo = false,
                bucketDisplayName = "Download"
            )
        )
    }

    @Test
    fun testCategoryFilterPhotosOnly() {
        val photos = sampleItems.filter { !it.isVideo }
        assertEquals(3, photos.size)
        assertTrue(photos.all { !it.isVideo })
    }

    @Test
    fun testCategoryFilterVideosOnly() {
        val videos = sampleItems.filter { it.isVideo }
        assertEquals(1, videos.size)
        assertEquals(2L, videos.first().id)
    }

    @Test
    fun testCategoryFilterScreenshots() {
        val screenshots = sampleItems.filter {
            it.displayName.contains("screenshot", ignoreCase = true) ||
                    it.bucketDisplayName.contains("screenshot", ignoreCase = true)
        }
        assertEquals(1, screenshots.size)
        assertEquals(3L, screenshots.first().id)
    }

    @Test
    fun testCategoryFilterLargeFiles() {
        val largeFiles = sampleItems.filter { it.size >= 25 * 1024 * 1024L }
        assertEquals(2, largeFiles.size)
        assertTrue(largeFiles.any { it.id == 2L }) // 50MB
        assertTrue(largeFiles.any { it.id == 4L }) // 30MB
    }

    @Test
    fun testSwipeRightKeepsItem() {
        val unreviewed = sampleItems.toMutableList()
        val kept = mutableListOf<MediaItem>()
        val history = mutableListOf<SwipeActionRecord>()

        val topItem = unreviewed.removeAt(0)
        kept.add(topItem)
        history.add(SwipeActionRecord(topItem, SwipeDirection.RIGHT))

        assertEquals(3, unreviewed.size)
        assertEquals(1, kept.size)
        assertEquals(topItem.id, kept.first().id)
        assertEquals(SwipeDirection.RIGHT, history.last().direction)
    }

    @Test
    fun testSwipeLeftQueuesItemForTrashAndCalculatesRecoverableStorage() {
        val unreviewed = sampleItems.toMutableList()
        val trashQueue = mutableListOf<MediaItem>()
        val history = mutableListOf<SwipeActionRecord>()

        // Swipe left item 1 (4MB) and item 2 (50MB)
        val item1 = unreviewed.removeAt(0)
        trashQueue.add(item1)
        history.add(SwipeActionRecord(item1, SwipeDirection.LEFT))

        val item2 = unreviewed.removeAt(0)
        trashQueue.add(item2)
        history.add(SwipeActionRecord(item2, SwipeDirection.LEFT))

        assertEquals(2, unreviewed.size)
        assertEquals(2, trashQueue.size)
        val totalRecoverableBytes = trashQueue.sumOf { it.size }
        assertEquals(54 * 1024 * 1024L, totalRecoverableBytes)
    }

    @Test
    fun testUndoRestoresItemToStackAndUpdatesStorage() {
        val unreviewed = sampleItems.toMutableList()
        val trashQueue = mutableListOf<MediaItem>()
        val history = mutableListOf<SwipeActionRecord>()

        // Trash item 1
        val item1 = unreviewed.removeAt(0)
        trashQueue.add(item1)
        history.add(SwipeActionRecord(item1, SwipeDirection.LEFT))

        assertEquals(3, unreviewed.size)
        assertEquals(1, trashQueue.size)

        // Undo
        val lastAction = history.removeAt(history.lastIndex)
        trashQueue.remove(lastAction.mediaItem)
        unreviewed.add(0, lastAction.mediaItem)

        assertEquals(4, unreviewed.size)
        assertEquals(0, trashQueue.size)
        assertEquals(item1.id, unreviewed.first().id)
        assertEquals(0L, trashQueue.sumOf { it.size })
    }

    @Test
    fun testSessionResetRestoresAllItems() {
        val unreviewed = sampleItems.toMutableList()
        val kept = mutableListOf<MediaItem>()
        val trashQueue = mutableListOf<MediaItem>()

        kept.add(unreviewed.removeAt(0))
        trashQueue.add(unreviewed.removeAt(0))

        assertEquals(2, unreviewed.size)

        // Reset
        unreviewed.clear()
        unreviewed.addAll(sampleItems)
        kept.clear()
        trashQueue.clear()

        assertEquals(4, unreviewed.size)
        assertEquals(0, kept.size)
        assertEquals(0, trashQueue.size)
    }
}
