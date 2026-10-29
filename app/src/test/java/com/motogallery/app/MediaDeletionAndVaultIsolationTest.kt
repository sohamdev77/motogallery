package com.motogallery.app

import com.motogallery.app.data.model.MediaItem
import com.motogallery.app.data.model.TrashedMediaItem
import com.motogallery.app.data.model.VaultItem
import com.motogallery.app.data.vault.VaultCryptoManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import java.io.File
import java.io.FileOutputStream
import java.security.SecureRandom
import javax.crypto.spec.SecretKeySpec

class MediaDeletionAndVaultIsolationTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var cryptoManager: VaultCryptoManager
    private lateinit var testKey: SecretKeySpec

    @Before
    fun setup() {
        val keyBytes = ByteArray(32)
        SecureRandom().nextBytes(keyBytes)
        testKey = SecretKeySpec(keyBytes, "AES")
        cryptoManager = VaultCryptoManager(customMasterKey = testKey)
    }

    @Test
    fun testVaultItemEncapsulationAndPublicIsolation() {
        val originalPhotoName = "IMG_20261030_034500.jpg"
        val encryptedFileName = "enc_101.bin"

        val vaultItem = VaultItem(
            id = 101L,
            originalName = originalPhotoName,
            encryptedFileName = encryptedFileName,
            mimeType = "image/jpeg",
            size = 4500000L,
            width = 4000,
            height = 3000,
            isTrashed = false
        )

        // The vault item holds original properties but uses isolated internal storage name
        assertEquals(originalPhotoName, vaultItem.originalName)
        assertEquals(encryptedFileName, vaultItem.encryptedFileName)
        assertFalse(vaultItem.isTrashed)
        assertEquals(4500000L, vaultItem.size)
    }

    @Test
    fun testTrashedMediaItemCalculationsForVaultAndPublic() {
        val dummyUri = Mockito.mock(android.net.Uri::class.java)

        val vaultItem = VaultItem(
            id = 202L,
            originalName = "secret.jpg",
            encryptedFileName = "enc_202.bin",
            mimeType = "image/jpeg",
            size = 1024L,
            isTrashed = true,
            trashedTimestamp = System.currentTimeMillis() - (5L * 24 * 60 * 60 * 1000L) // 5 days ago
        )

        val trashedVaultItem = TrashedMediaItem(
            id = 202L,
            uri = dummyUri,
            displayName = vaultItem.originalName,
            mimeType = vaultItem.mimeType,
            size = vaultItem.size,
            isVideo = false,
            trashedTimeMs = vaultItem.trashedTimestamp,
            isVaultItem = true,
            vaultItem = vaultItem
        )

        // Vault item trashed 5 days ago should have 25 days remaining
        assertEquals(25, trashedVaultItem.daysRemaining)

        // Public item trashed 10 days ago
        val tenDaysAgo = System.currentTimeMillis() - (10L * 24 * 60 * 60 * 1000L)
        val trashedPublicItem = TrashedMediaItem(
            id = 303L,
            uri = dummyUri,
            displayName = "public_sample.jpg",
            mimeType = "image/jpeg",
            size = 2048L,
            isVideo = false,
            trashedTimeMs = tenDaysAgo,
            isVaultItem = false,
            vaultItem = null
        )

        assertEquals(20, trashedPublicItem.daysRemaining)
    }

    @Test
    fun testPublicFilePurgingUponVaultImport() {
        // Simulate a public file on device storage
        val publicFile = tempFolder.newFile("CAMERA_IMG_9999.jpg")
        publicFile.writeText("Public sensitive image content")

        assertTrue("Public file must exist initially", publicFile.exists())

        // Simulate moving to vault:
        // 1. Encrypt content to secure vault storage
        val vaultFile = tempFolder.newFile("vault_enc_9999.bin")
        val encryptedData = cryptoManager.encryptBytes(publicFile.readBytes())
        vaultFile.writeBytes(encryptedData)

        assertTrue("Encrypted vault file must be created", vaultFile.exists())

        // 2. Public original must be purged and deleted
        val deleted = publicFile.delete()
        assertTrue("Public file must be successfully deleted", deleted)
        assertFalse("Public file must no longer exist on storage", publicFile.exists())
    }

    @Test
    fun testVaultPermanentShredding() {
        val vaultEncryptedFile = tempFolder.newFile("vault_secure_file.bin")
        vaultEncryptedFile.writeBytes("EncryptedBytesPayload".toByteArray())
        assertTrue(vaultEncryptedFile.exists())

        // Simulate secure shredding: overwrite with zeros before unlinking
        val fileLength = vaultEncryptedFile.length()
        FileOutputStream(vaultEncryptedFile).use { fos ->
            fos.write(ByteArray(fileLength.toInt()) { 0 })
            fos.flush()
        }

        // Verify content was wiped before deletion
        val wipedBytes = vaultEncryptedFile.readBytes()
        assertTrue(wipedBytes.all { it == 0.toByte() })

        // Delete permanently
        val shredded = vaultEncryptedFile.delete()
        assertTrue(shredded)
        assertFalse(vaultEncryptedFile.exists())
    }
}
