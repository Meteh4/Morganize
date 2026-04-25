package com.metoly.morganize.feature.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metoly.components.model.NoteEditorDelegate
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.Note
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.grid.GridItemFactory
import com.metoly.morganize.feature.edit.model.EditSpecificState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import android.util.Base64
import com.metoly.morganize.core.model.security.EncryptionManager
import com.metoly.morganize.core.model.security.KeyManager
import com.metoly.morganize.core.model.grid.NotePage

/**
 * ViewModel orchestrating the modification and updating of an existing Note.
 * Controls data re-hydration from local storage, decodes secured content via biometric prompts,
 * manages delete requests, and persists state modifications back to the repository.
 */
class EditViewModel(
    private val noteId: Long,
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

    private val _editState = MutableStateFlow(EditSpecificState())
    val editState: StateFlow<EditSpecificState> = _editState.asStateFlow()

    private var originalNote: Note? = null

    init {
        loadCategories()
        loadNote()
    }

    fun requestDelete() {
        _editState.update { it.copy(showDeleteDialog = true) }
    }

    fun dismissDelete() {
        _editState.update { it.copy(showDeleteDialog = false) }
    }

    fun confirmDelete() {
        _editState.update { it.copy(showDeleteDialog = false) }
        deleteNote()
    }

    /**
     * Re-encodes and updates the database with current grid edits and structural states.
     * Retains pre-existing encryption wrappers passively if the user modifies an unlocked note
     * without attempting to disable or alter its security layout.
     */
    fun save() {
        val current = originalNote ?: return
        val state = delegate.state.value
        
        viewModelScope.launch {
            try {
                var finalPages = delegate.getPagesForSaving()
                var encryptedContent = current.encryptedContent
                var saltBase64 = current.salt
                var ivBase64 = current.iv

                var biometricPwdCipher = current.biometricWrappedPassword
                var biometricPwdIv = current.biometricWrappedPasswordIv
                var hasBiometric = current.hasBiometric

                if (state.isSecretNote) {
                    val password = state.transientSecretNotePassword
                    if (password != null) {
                        val pagesJson = Json.encodeToString(finalPages)
                        val salt = keyManager.generateSalt()
                        val secretKey = keyManager.deriveKeyFromPassword(password, salt)
                        val (cipherBase64, currentIvBase64, _) = encryptionManager.encryptString(pagesJson, secretKey)
                        
                        if (state.transientSecretNoteBiometric) {
                            val biometricKey = keyManager.getOrCreateBiometricKey("secret_note_master_key")
                            val (encPwd, encIv, _) = encryptionManager.encryptString(password, biometricKey)
                            biometricPwdCipher = encPwd
                            biometricPwdIv = encIv
                            hasBiometric = true
                        }
                        
                        finalPages = emptyList()
                        encryptedContent = cipherBase64
                        saltBase64 = Base64.encodeToString(salt, Base64.NO_WRAP)
                        ivBase64 = currentIvBase64
                    } else if (current.isSecret && !state.isSecretNoteUnlocked) {
                        finalPages = emptyList()
                    }
                } else {
                    encryptedContent = null
                    saltBase64 = null
                    ivBase64 = null
                    biometricPwdCipher = null
                    biometricPwdIv = null
                    hasBiometric = false
                }

                val updatedNote = current.copy(
                    title = state.title.trim(),
                    pages = finalPages,
                    backgroundColor = state.backgroundColor,
                    categoryId = state.categoryId,
                    isSecret = state.isSecretNote,
                    encryptedContent = encryptedContent,
                    salt = saltBase64,
                    iv = ivBase64,
                    hasBiometric = hasBiometric,
                    biometricWrappedPassword = biometricPwdCipher,
                    biometricWrappedPasswordIv = biometricPwdIv,
                    updatedAt = System.currentTimeMillis()
                )
                
                noteRepository.updateNote(updatedNote)
                    .onEach { result ->
                        when (result) {
                            is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                            is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                            else -> Unit
                        }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Failed to save note: ${e.message}"))
            }
        }
    }

    /**
     * Attempts to unlock the Note overlay via standard text password.
     */
    fun unlockSecretNote(password: String) {
        val current = originalNote ?: return
        val currentEncryptedContent = current.encryptedContent ?: return
        val currentSalt = current.salt ?: return
        val currentIv = current.iv ?: return
        
        viewModelScope.launch {
            try {
                val saltBytes = Base64.decode(currentSalt, Base64.NO_WRAP)
                val secretKey = keyManager.deriveKeyFromPassword(password, saltBytes)
                val decryptedJson = encryptionManager.decryptString(currentEncryptedContent, currentIv, secretKey)
                val decryptedPages = Json.decodeFromString<List<NotePage>>(decryptedJson)
                
                delegate.updateState {
                    it.copy(
                        pages = decryptedPages,
                        isSecretNoteUnlocked = true,
                        transientSecretNotePassword = password
                    )
                }
            } catch (e: Exception) {
                delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Incorrect password"))
            }
        }
    }

    /**
     * Executes the overarching secret note unlock sequence once provided with the
     * valid OS-generated hardware backed biometric cipher object.
     */
    fun unlockSecretNoteWithBiometric(decryptedKey: javax.crypto.SecretKey) {
        val current = originalNote ?: return
        val biometricPwdCipher = current.biometricWrappedPassword ?: return
        val biometricPwdIv = current.biometricWrappedPasswordIv ?: return

        viewModelScope.launch {
            try {
                val password = encryptionManager.decryptString(biometricPwdCipher, biometricPwdIv, decryptedKey)
                unlockSecretNote(password)
            } catch (e: Exception) {
                delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Biometric decryption failed"))
                delegate.onEvent(NoteEditorEvent.SecretNoteBiometricFailed)
            }
        }
    }

    private fun loadCategories() {
        categoryRepository.getAllCategories()
            .onEach { state ->
                if (state is ResponseState.Success) {
                    delegate.updateState { it.copy(categories = state.data) }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadNote() {
        noteRepository.getNoteById(noteId)
            .onEach { state ->
                when (state) {
                    ResponseState.Loading ->
                        _editState.update { it.copy(noteState = ResponseState.Loading) }

                    is ResponseState.Success -> {
                        val note = state.data
                        if (note != null) {
                            originalNote = note
                            val loadedPages = if (note.isSecret) {
                                emptyList()
                            } else {
                                note.pages.ifEmpty { listOf(GridItemFactory.createNotePage()) }
                            }
                            delegate.updateState {
                                it.copy(
                                    title = if (note.isSecret) "" else note.title,
                                    pages = loadedPages,
                                    backgroundColor = note.backgroundColor,
                                    categoryId = note.categoryId,
                                    isSecretNote = note.isSecret,
                                    isSecretNoteUnlocked = false,
                                    transientSecretNoteBiometric = note.hasBiometric,
                                    secretNoteBiometricIv = note.biometricWrappedPasswordIv
                                )
                            }
                            
                            if (note.isSecret && note.hasBiometric && note.biometricWrappedPasswordIv != null) {
                                delegate.sendUiEvent(NoteEditorUiEvent.ShowBiometricPrompt(
                                    itemId = null,
                                    keystoreAlias = "secret_note_master_key",
                                    decryptionIv = note.biometricWrappedPasswordIv
                                ))
                            }
                            
                            _editState.update { it.copy(noteState = ResponseState.Success(Unit)) }
                        } else {
                            _editState.update {
                                it.copy(noteState = ResponseState.Error("Note not found"))
                            }
                        }
                    }

                    is ResponseState.Error ->
                        _editState.update { it.copy(noteState = ResponseState.Error(state.message)) }

                    ResponseState.Idle -> Unit
                }
            }
            .launchIn(viewModelScope)
    }

    private fun deleteNote() {
        val current = originalNote ?: return
        noteRepository.deleteNote(current)
            .onEach { result ->
                when (result) {
                    is ResponseState.Success -> delegate.sendUiEvent(NoteEditorUiEvent.SaveSuccess)
                    is ResponseState.Error -> delegate.sendUiEvent(NoteEditorUiEvent.ShowSnackbar(result.message))
                    else -> Unit
                }
            }
            .launchIn(viewModelScope)
    }
}