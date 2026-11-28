package com.kryptos.browser

import android.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

object EncryptionHelper {
    // Static AES Key (In production, this should be fetched from Android Keystore)
    private const val ALGORITHM = "AES"
    private const val SECRET_KEY = "KryptosBrowserV6SecretKey!!" // 32 chars

    fun encrypt(data: String): String {
        return try {
            val key = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptedBytes = cipher.doFinal(data.toByteArray())
            Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        } catch (e: Exception) {
            data // Fallback (not recommended)
        }
    }

    fun decrypt(data: String): String {
        return try {
            val key = SecretKeySpec(SECRET_KEY.toByteArray(), ALGORITHM)
            val cipher = Cipher.getInstance(ALGORITHM)
            cipher.init(Cipher.DECRYPT_MODE, key)
            val decodedBytes = Base64.decode(data, Base64.DEFAULT)
            String(cipher.doFinal(decodedBytes))
        } catch (e: Exception) {
            data
        }
    }
}
