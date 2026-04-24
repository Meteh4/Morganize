package com.metoly.morganize.core.model.security

/**
 * Holds the result of an AES-256-GCM encryption operation.
 *
 * @param ciphertext The encrypted data bytes
 * @param iv         The initialization vector (nonce) used for GCM
 * @param salt       The salt used for PBKDF2 key derivation
 */
data class EncryptionResult(
    val ciphertext: ByteArray,
    val iv: ByteArray,
    val salt: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncryptionResult) return false
        return ciphertext.contentEquals(other.ciphertext) &&
                iv.contentEquals(other.iv) &&
                salt.contentEquals(other.salt)
    }

    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + iv.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}
