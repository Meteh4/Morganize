package com.metoly.components.security

import android.util.Base64
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.metoly.components.model.NoteEditorDelegate
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.morganize.core.model.security.KeyManager
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec

/**
 * Centralised handler for biometric cipher initialisation and prompt display.
 * Extracted from CreateScreen / EditScreen to eliminate duplicated boilerplate (~55 lines each).
 *
 * Usage: call [handleBiometricPrompt] from the `NoteEditorUiEvent.ShowBiometricPrompt` branch
 * of your UI-event collector.
 */
object BiometricCipherHandler {

    private const val TAG = "BiometricCipherHandler"
    private const val AES_GCM = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128

    /**
     * Initialises an AES/GCM cipher from the Android Keystore, shows a biometric prompt,
     * and dispatches the appropriate [NoteEditorEvent] on success or failure.
     *
     * @param event             The prompt event containing keystoreAlias, decryptionIv, and itemId.
     * @param activity          The [FragmentActivity] required by BiometricPrompt.
     * @param biometricHelper   A pre-created [BiometricHelper] instance.
     * @param keyManager        DI-provided [KeyManager] — avoids creating ad-hoc instances.
     * @param delegate          The shared [NoteEditorDelegate] for dispatching events.
     * @param activePageId      The page id that is currently visible, used to build page-scoped events.
     * @param onError           Lambda invoked synchronously when an error message should be shown.
     * @param onSecretNoteUnlock Optional extra callback for secret-note-level biometric unlock
     *                          (only used by EditScreen which calls viewModel.unlockSecretNoteWithBiometric).
     */
    fun handleBiometricPrompt(
        event: NoteEditorUiEvent.ShowBiometricPrompt,
        activity: FragmentActivity,
        biometricHelper: BiometricHelper,
        keyManager: KeyManager,
        delegate: NoteEditorDelegate,
        activePageId: String,
        onError: (String) -> Unit,
        onSecretNoteUnlock: ((javax.crypto.SecretKey) -> Unit)? = null
    ) {
        try {
            val secretKey = keyManager.getOrCreateBiometricKey(event.keystoreAlias)
            val cipher = Cipher.getInstance(AES_GCM)
            if (event.decryptionIv != null) {
                val ivBytes = Base64.decode(event.decryptionIv, Base64.NO_WRAP)
                val spec = GCMParameterSpec(GCM_TAG_LENGTH, ivBytes)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey)
            }
            biometricHelper.showBiometricPrompt(
                activity = activity,
                cipher = cipher,
                onSuccess = { _ ->
                    val id = event.itemId
                    if (id != null) {
                        delegate.onEvent(
                            NoteEditorEvent.SecretItemUnlockWithBiometric(
                                pageId = activePageId,
                                itemId = id,
                                decryptedKey = secretKey
                            )
                        )
                    } else {
                        delegate.onEvent(NoteEditorEvent.SecretNoteUnlockWithBiometric(secretKey))
                        onSecretNoteUnlock?.invoke(secretKey)
                    }
                },
                onFailed = {
                    dispatchBiometricFailure(event.itemId, activePageId, delegate)
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to init cipher", e)
            onError("Biometric Error: ${e.message}")
            dispatchBiometricFailure(event.itemId, activePageId, delegate)
        }
    }

    private fun dispatchBiometricFailure(
        itemId: String?,
        activePageId: String,
        delegate: NoteEditorDelegate
    ) {
        if (itemId != null) {
            delegate.onEvent(
                NoteEditorEvent.SecretItemBiometricFailed(
                    pageId = activePageId,
                    itemId = itemId
                )
            )
        } else {
            delegate.onEvent(NoteEditorEvent.SecretNoteBiometricFailed)
        }
    }
}
