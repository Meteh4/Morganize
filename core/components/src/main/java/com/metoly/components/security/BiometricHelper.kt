package com.metoly.components.security

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.crypto.Cipher

private val Context.biometricDataStore: DataStore<Preferences> by preferencesDataStore(name = "biometric_prefs")

/**
 * Helper for managing biometric authentication flows and tracking consecutive failures.
 * Persists failure counts per item to implement a 5-failure lockout rule.
 */
class BiometricHelper(private val context: Context) {

    /**
     * Checks if biometric hardware is available and the user has enrolled biometrics.
     */
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Shows the biometric prompt.
     * @param activity Required to host the BiometricPrompt UI.
     * @param cipher The Cipher initialized with the Android Keystore biometric key.
     * @param onSuccess Callback invoked when auth succeeds, passing the authenticated Cipher.
     * @param onFailed Callback invoked on failure or error.
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        cipher: Cipher,
        onSuccess: (Cipher) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onFailed()
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    result.cryptoObject?.cipher?.let { onSuccess(it) } ?: onFailed()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock Item")
            .setSubtitle("Confirm your biometric to decrypt")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            .setNegativeButtonText("Use Password")
            .build()

        try {
            biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
        } catch (e: Exception) {
            e.printStackTrace()
            onFailed()
        }
    }

    // ── Failure counting logic ────────────────────────────────────────────────

    suspend fun getFailureCount(itemId: String): Int {
        val key = intPreferencesKey("fails_$itemId")
        return context.biometricDataStore.data.map { prefs -> prefs[key] ?: 0 }.first()
    }

    suspend fun incrementFailureCount(itemId: String): Int {
        val key = intPreferencesKey("fails_$itemId")
        val prefs = context.biometricDataStore.edit { prefs ->
            val current = prefs[key] ?: 0
            prefs[key] = current + 1
        }
        return prefs[key] ?: 1
    }

    suspend fun resetFailureCount(itemId: String) {
        val key = intPreferencesKey("fails_$itemId")
        context.biometricDataStore.edit { prefs ->
            prefs.remove(key)
        }
    }
}
