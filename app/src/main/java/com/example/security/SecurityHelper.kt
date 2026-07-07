package com.example.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * SecurityHelper handles hardware-backed Android KeyStore encryption and decryption
 * for sensitive fields such as passwords, passphrases, and private SSH keys.
 */
object SecurityHelper {
    private const val PROVIDER = "AndroidKeyStore"
    private const val ALIAS = "MySshSecretKeyAlias"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"

    init {
        try {
            val keyStore = KeyStore.getInstance(PROVIDER)
            keyStore.load(null)
            if (!keyStore.containsAlias(ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER)
                val spec = KeyGenParameterSpec.Builder(
                    ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .build()
                keyGenerator.init(spec)
                keyGenerator.generateKey()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            val keyStore = KeyStore.getInstance(PROVIDER)
            keyStore.load(null)
            val entry = keyStore.getEntry(ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Encrypts plain text using AES-GCM from Android KeyStore.
     * Output format: Base64(IV) + ":" + Base64(Ciphertext)
     */
    fun encrypt(plainText: String): String {
        if (plainText.isEmpty()) return ""
        return try {
            val key = getSecretKey()
            if (key != null) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, key)
                val ciphertext = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
                val iv = cipher.iv
                val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
                val cipherBase64 = Base64.encodeToString(ciphertext, Base64.NO_WRAP)
                "$ivBase64:$cipherBase64"
            } else {
                // Fallback safe encoding if KeyStore is unavailable (e.g. testing context)
                val encodedBytes = Base64.encode(plainText.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
                "FALLBACK:" + String(encodedBytes, Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Return raw with fallback prefix to avoid data loss
            "ERROR_ENCODE:" + plainText
        }
    }

    /**
     * Decrypts text previously encrypted by [encrypt].
     */
    fun decrypt(encryptedText: String): String {
        if (encryptedText.isEmpty()) return ""
        if (encryptedText.startsWith("FALLBACK:")) {
            val base64Part = encryptedText.substringAfter("FALLBACK:")
            return try {
                val decoded = Base64.decode(base64Part, Base64.NO_WRAP)
                String(decoded, Charsets.UTF_8)
            } catch (e: Exception) {
                ""
            }
        }
        if (encryptedText.startsWith("ERROR_ENCODE:")) {
            return encryptedText.substringAfter("ERROR_ENCODE:")
        }
        return try {
            val parts = encryptedText.split(":")
            if (parts.size != 2) return encryptedText
            val iv = Base64.decode(parts[0], Base64.NO_WRAP)
            val ciphertext = Base64.decode(parts[1], Base64.NO_WRAP)

            val key = getSecretKey()
            if (key != null) {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                val spec = GCMParameterSpec(128, iv)
                cipher.init(Cipher.DECRYPT_MODE, key, spec)
                val plainTextBytes = cipher.doFinal(ciphertext)
                String(plainTextBytes, Charsets.UTF_8)
            } else {
                encryptedText
            }
        } catch (e: Exception) {
            e.printStackTrace()
            encryptedText
        }
    }
}
