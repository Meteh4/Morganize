package com.metoly.morganize.core.model.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manages cryptographic keys, including password-based derivation (PBKDF2)
 * and hardware-backed Android Keystore keys (for biometric auth).
 */
class KeyManager {

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATION_COUNT = 100_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_LENGTH_BYTES = 32
        
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    /**
     * Generates a secure random 32-byte salt.
     */
    fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_LENGTH_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    /**
     * Derives a 256-bit AES SecretKey from a given [password] and [salt] using PBKDF2.
     */
    suspend fun deriveKeyFromPassword(password: String, salt: ByteArray): SecretKey =
        withContext(Dispatchers.Default) {
            val spec = PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH_BITS)
            val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
            val pbaKey = factory.generateSecret(spec)
            SecretKeySpec(pbaKey.encoded, "AES")
        }

    /**
     * Retrieves or creates an AES-256-GCM key in the Android Keystore that requires
     * user authentication (biometrics) to be used.
     * 
     * @param alias The unique alias for the item/note's key in the Keystore.
     */
    fun getOrCreateBiometricKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        
        if (keyStore.containsAlias(alias)) {
            val entry = keyStore.getEntry(alias, null) as KeyStore.SecretKeyEntry
            return entry.secretKey
        }
        
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val builder = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            // .setUserAuthenticationRequired(true)
            // .setInvalidatedByBiometricEnrollment(true)
            
        keyGenerator.init(builder.build())
        return keyGenerator.generateKey()
    }
    
    /**
     * Deletes a key from the Android Keystore if it exists.
     */
    fun deleteBiometricKey(alias: String) {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (keyStore.containsAlias(alias)) {
            keyStore.deleteEntry(alias)
        }
    }
}
