package com.metoly.morganize.core.model.grid

import com.metoly.morganize.core.model.RichSpan
import kotlinx.serialization.Serializable

/**
 * Represents a draggable, resizable wrapper over standard note components.
 */
@Serializable
sealed class GridItem {
    abstract val id: String
    abstract val x: Int
    abstract val y: Int
    abstract val width: Int
    abstract val height: Int

    @Serializable
    data class Text(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val textContent: String,
        val richSpans: List<RichSpan> = emptyList(),
        val fontSize: Float = 14f,        // sp
        val textAlign: TextAlignment = TextAlignment.Start,
        val lineHeight: Float = 1.4f      // multiplier
    ) : GridItem()

    @Serializable
    data class Image(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val imageUri: String
    ) : GridItem()

    @Serializable
    data class Checklist(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val title: String = "",
        val entries: List<CheckboxEntry> = emptyList()
    ) : GridItem()

    /**
     * A wrapper item that encrypts and hides any existing [GridItem] type inside it.
     * When locked: renders only a lock icon — zero content leakage.
     * When unlocked: the decrypted inner item is managed transiently in [NoteEditorState].
     *
     * @param encryptedPayload Base64-encoded AES-256-GCM ciphertext of the inner GridItem JSON
     * @param salt             Base64-encoded PBKDF2 salt used for password-based key derivation
     * @param iv               Base64-encoded GCM initialization vector (nonce)
     * @param biometricFailureCount Persisted count of consecutive biometric failures
     * @param isBiometricDisabled   True if biometric was permanently disabled (≥5 failures)
     * @param hasBiometric          Whether biometric auth was configured at creation time
     */
    @Serializable
    data class SecretItem(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val encryptedPayload: String,
        val salt: String,
        val iv: String,
        val biometricFailureCount: Int = 0,
        val isBiometricDisabled: Boolean = false,
        val hasBiometric: Boolean = false,
        val biometricWrappedPassword: String? = null,
        val biometricWrappedPasswordIv: String? = null
    ) : GridItem()
}

@Serializable
data class CheckboxEntry(
    val id: String,
    val text: String = "",
    val isChecked: Boolean = false
)
