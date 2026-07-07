package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.security.SecurityHelper

@Entity(tableName = "ssh_hosts")
data class SshHost(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val alias: String,
    val ip: String,
    val port: Int = 22,
    val username: String,
    val authType: String = "PASSWORD", // "PASSWORD" or "PRIVATE_KEY"
    val encryptedPassword: String = "",
    val encryptedPrivateKey: String = "",
    val encryptedPassphrase: String = "",
    val timeout: Int = 10, // in seconds
    val keepAlive: Int = 30, // in seconds
    val isBookmarked: Boolean = false,
    val lastConnected: Long = 0L
) {
    // Cleartext helper properties (transparently encrypted/decrypted)
    val password: String
        get() = SecurityHelper.decrypt(encryptedPassword)

    val privateKey: String
        get() = SecurityHelper.decrypt(encryptedPrivateKey)

    val passphrase: String
        get() = SecurityHelper.decrypt(encryptedPassphrase)

    companion object {
        fun create(
            id: Int = 0,
            alias: String,
            ip: String,
            port: Int = 22,
            username: String,
            authType: String = "PASSWORD",
            passwordRaw: String = "",
            privateKeyRaw: String = "",
            passphraseRaw: String = "",
            timeout: Int = 10,
            keepAlive: Int = 30,
            isBookmarked: Boolean = false,
            lastConnected: Long = 0L
        ): SshHost {
            return SshHost(
                id = id,
                alias = alias,
                ip = ip,
                port = port,
                username = username,
                authType = authType,
                encryptedPassword = SecurityHelper.encrypt(passwordRaw),
                encryptedPrivateKey = SecurityHelper.encrypt(privateKeyRaw),
                encryptedPassphrase = SecurityHelper.encrypt(passphraseRaw),
                timeout = timeout,
                keepAlive = keepAlive,
                isBookmarked = isBookmarked,
                lastConnected = lastConnected
            )
        }
    }
}
