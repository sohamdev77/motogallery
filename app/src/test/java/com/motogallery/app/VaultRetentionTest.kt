package com.motogallery.app

import com.motogallery.app.data.model.VaultItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class VaultRetentionTest {

    @Test
    fun testActiveVaultItemHasFullRetention() {
        val activeItem = VaultItem(
            id = 1L,
            originalName = "photo.jpg",
            encryptedFileName = "enc_1.enc",
            mimeType = "image/jpeg",
            size = 1024L,
            isTrashed = false,
            trashedTimestamp = 0L
        )

        assertEquals(30, activeItem.daysRemainingInTrash)
    }

    @Test
    fun testFreshlyTrashedVaultItemHas30Days() {
        val trashedItem = VaultItem(
            id = 2L,
            originalName = "document.jpg",
            encryptedFileName = "enc_2.enc",
            mimeType = "image/jpeg",
            size = 2048L,
            isTrashed = true,
            trashedTimestamp = System.currentTimeMillis() - (1000 * 60 * 60) // 1 hour ago
        )

        assertEquals(29, trashedItem.daysRemainingInTrash)
    }

    @Test
    fun testTenDaysTrashedVaultItemHas20DaysLeft() {
        val tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000L)
        val item = VaultItem(
            id = 3L,
            originalName = "trip.mp4",
            encryptedFileName = "enc_3.enc",
            mimeType = "video/mp4",
            size = 10000L,
            isTrashed = true,
            trashedTimestamp = tenDaysAgo
        )

        assertEquals(20, item.daysRemainingInTrash)
    }

    @Test
    fun testExpiredTrashItemClampsToZero() {
        val thirtyFiveDaysAgo = System.currentTimeMillis() - (35L * 24 * 60 * 60 * 1000L)
        val item = VaultItem(
            id = 4L,
            originalName = "old.jpg",
            encryptedFileName = "enc_4.enc",
            mimeType = "image/jpeg",
            size = 1000L,
            isTrashed = true,
            trashedTimestamp = thirtyFiveDaysAgo
        )

        assertEquals(0, item.daysRemainingInTrash)
    }
}
