package com.motogallery.app

import com.motogallery.app.data.vault.VaultCryptoManager
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import javax.crypto.spec.SecretKeySpec

class VaultCryptoTest {

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
    fun testPasswordHashingAndVerification() {
        val password = "MotoGalleryPin1234"
        val salt = cryptoManager.generateSalt()
        val hash = cryptoManager.hashPassword(password, salt)

        // Correct password verifies successfully
        assertTrue("Password verification should succeed with correct PIN",
            cryptoManager.verifyPassword(password, salt, hash))

        // Incorrect password fails
        assertFalse("Password verification should fail with wrong PIN",
            cryptoManager.verifyPassword("WrongPin", salt, hash))
    }

    @Test
    fun testAes256GcmEncryptDecrypt() {
        val originalText = "TopSecretPrivatePhotoDataForMotoGallery"
        val plainBytes = originalText.toByteArray(Charsets.UTF_8)

        val encryptedBytes = cryptoManager.encryptBytes(plainBytes)

        // Encrypted length must include 12-byte IV + ciphertext + 16-byte GCM tag
        assertTrue(encryptedBytes.size >= plainBytes.size + VaultCryptoManager.GCM_IV_LENGTH_BYTES + 16)

        val decryptedBytes = cryptoManager.decryptBytes(encryptedBytes)
        val decryptedText = String(decryptedBytes, Charsets.UTF_8)

        assertEquals(originalText, decryptedText)
    }

    @Test
    fun testRandomIvPerEncryption() {
        val plainBytes = "IdenticalDataSample".toByteArray(Charsets.UTF_8)

        val enc1 = cryptoManager.encryptBytes(plainBytes)
        val enc2 = cryptoManager.encryptBytes(plainBytes)

        val iv1 = enc1.copyOfRange(0, VaultCryptoManager.GCM_IV_LENGTH_BYTES)
        val iv2 = enc2.copyOfRange(0, VaultCryptoManager.GCM_IV_LENGTH_BYTES)

        // Each encryption must use a unique random IV to prevent replay & nonce reuse attacks
        assertFalse("Consecutive encryptions must have distinct random IVs", iv1.contentEquals(iv2))
    }

    @Test(expected = Exception::class)
    fun testTamperDetectionThrowsException() {
        val plainBytes = "SensitiveFinancialInformation".toByteArray(Charsets.UTF_8)
        val encrypted = cryptoManager.encryptBytes(plainBytes)

        // Flip a bit in the ciphertext to simulate data tampering
        val tampered = encrypted.clone()
        val targetIndex = VaultCryptoManager.GCM_IV_LENGTH_BYTES + 2
        tampered[targetIndex] = (tampered[targetIndex].toInt() xor 0xFF).toByte()

        // Decryption of tampered ciphertext must throw AEADBadTagException / Exception
        cryptoManager.decryptBytes(tampered)
    }

    @Test
    fun testStreamEncryptionAndDecryption() {
        val originalData = ByteArray(128 * 1024) { (it % 256).toByte() } // 128 KB
        val inputStream = ByteArrayInputStream(originalData)
        val encryptedOutputStream = ByteArrayOutputStream()

        cryptoManager.encryptStream(inputStream, encryptedOutputStream)
        val encryptedBytes = encryptedOutputStream.toByteArray()

        val encryptedInputStream = ByteArrayInputStream(encryptedBytes)
        val decryptedOutputStream = ByteArrayOutputStream()
        cryptoManager.decryptStream(encryptedInputStream, decryptedOutputStream)

        val decryptedBytes = decryptedOutputStream.toByteArray()
        assertArrayEquals("Decrypted stream data must match original", originalData, decryptedBytes)
    }
}
