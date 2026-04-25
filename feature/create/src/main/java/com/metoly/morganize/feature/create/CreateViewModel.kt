package com.metoly.morganize.feature.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.NoteEditorDelegate
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Base64
import com.metoly.morganize.core.model.security.EncryptionManager
import com.metoly.morganize.core.model.security.KeyManager

/**
 * ViewModel managing the creation of a new Note.
 * Utilizes a shared [NoteEditorDelegate] for handling deep grid editing logic and formatting.
 * Owns the final save step, committing the newly constructed Note (with optional encryption) to the database.
 */
class CreateViewModel(
    private val noteRepository: NoteRepository,
    private val categoryRepository: CategoryRepository,
    private val encryptionManager: EncryptionManager,
    private val keyManager: KeyManager
) : ViewModel() {

    val delegate = NoteEditorDelegate(
        encryptionManager = encryptionManager,
        keyManager = keyManager,
        coroutineScope = viewModelScope,
        onCategoryCreate = { name, colorArgb ->
            categoryRepository.insertCategory(
                Category(name = name, colorArgb = colorArgb)
            ).launchIn(viewModelScope)
        }
    )

    val uiState = delegate.state
    val uiEvent = delegate.uiEvent

    init {
        observeCategories()
    }

    /**
     * Commits the current editing state as a new Note to the repository.
     * Applies AES encryption to the note body if it was marked as a Secret Note.
     */
    fun save() {
        val state = delegate.state.value
        if (state.title.isBlank() && state.pages.all { it.items.isEmpty() }) return
        
        viewModelScope.launch {
            try {
                var finalPages = delegate.getPagesForSaving()
                var encryptedContent: String? = null
                var saltBase64: String? = null
                var ivBase64: String? = null

                var biometricPwdCipher: String? = null
                var biometricPwdIv: String? = null

                val password = state.transientSecretNotePassword
                if (state.isSecretNote && password != null) {
                    val pagesJson = Json.encodeToString(finalPages)
                    val salt = keyManager.generateSalt()
                    val secretKey = keyManager.deriveKeyFromPassword(password, salt)
                    val (cipherBase64, currentIvBase64, _) = encryptionManager.encryptString(pagesJson, secretKey)
                    
                    if (state.transientSecretNoteBiometric) {
                        val biometricKey = keyManager.getOrCreateBiometricKey("secret_note_master_key")
                        val (encPwd, encIv, _) = encryptionManager.encryptString(password, biometricKey)
                        biometricPwdCipher = encPwd
                        biometricPwdIv = encIv
                    }
                    
                    finalPages = emptyList()
                    encryptedContent = cipherBase64
                    saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
                    ivBase64 = currentIvBase64
                }

                val note = Note(
                    title = state.title.trim(),
                    pages = finalPages,
                    backgroundColor = state.backgroundColor,
                    categoryId = state.categoryId,
                    isSecret = state.isSecretNote,
                    encryptedContent = encryptedContent,
                    salt = saltBase64,
                    iv = ivBase64,
                    hasBiometric = state.transientSecretNoteBiometric,
                    biometricWrappedPassword = biometricPwdCipher,
                    biometricWrappedPasswordIv = biometricPwdIv
                )
                
                noteRepository.insertNote(note)
                    .onEach { result ->
                        when (result) {
                            is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                            is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                            else -> Unit
                        }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Failed to encrypt note: ${e.message}"))
            }
        }
    }

    private fun observeCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    delegate.updateState { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }
}