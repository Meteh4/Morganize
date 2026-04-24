package com.metoly.components.model

import com.metoly.morganize.core.model.grid.ChecklistActionType
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.GridItemFactory
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.metoly.morganize.core.model.security.EncryptionManager
import com.metoly.morganize.core.model.security.KeyManager
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * UI'dan bağımsız state holder. Grid, Drawing, Text, Checklist ve RichText
 * işlemlerinin tümünü yönetir. ViewModel'lar bu sınıfı delegation ile kullanır.
 */
class NoteEditorDelegate(
    val encryptionManager: EncryptionManager? = null,
    val keyManager: KeyManager? = null,
    val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob()),
    private val onCategoryCreate: (name: String, colorArgb: Int) -> Unit = { _, _ -> }
) {

    private val _state = MutableStateFlow(NoteEditorState())
    val state: StateFlow<NoteEditorState> = _state.asStateFlow()

    private val _uiEvent = Channel<NoteEditorUiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()

    /** ViewModel'ın doğrudan state'i güncellemesi gerektiğinde (ör. loadNote). */
    fun updateState(transform: (NoteEditorState) -> NoteEditorState) {
        _state.update(transform)
    }

    /** One-time UI event gönder. */
    fun sendUiEvent(event: NoteEditorUiEvent) {
        _uiEvent.trySend(event)
    }

    /** Ana event router. */
    fun onEvent(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.AddPage ->
                _state.update { state ->
                    state.copy(pages = state.pages + GridItemFactory.createNotePage())
                }

            is NoteEditorEvent.BackgroundColorChanged ->
                _state.update { it.copy(backgroundColor = event.colorArgb) }

            is NoteEditorEvent.CategorySelected ->
                _state.update { it.copy(categoryId = event.categoryId) }

            is NoteEditorEvent.ChecklistGridItemAdded,
            is NoteEditorEvent.ChecklistAction -> handleChecklist(event)

            is NoteEditorEvent.DrawingModeToggled,
            is NoteEditorEvent.DrawingColorChanged,
            is NoteEditorEvent.DrawingStrokeWidthChanged,
            is NoteEditorEvent.DrawingEraserWidthChanged,
            is NoteEditorEvent.DrawingEraserToggled,
            is NoteEditorEvent.DrawingStrokeAdded,
            is NoteEditorEvent.DrawingStrokeReverted,
            is NoteEditorEvent.DrawingStrokesUpdated -> handleDrawing(event)

            is NoteEditorEvent.EditingTextItemChanged,
            is NoteEditorEvent.RichStateUpdated -> handleRichText(event)

            is NoteEditorEvent.ImageGridItemAdded -> handleImageGridItem(event)

            is NoteEditorEvent.ItemSelected,
            is NoteEditorEvent.ItemMoved,
            is NoteEditorEvent.ItemResized,
            is NoteEditorEvent.ItemDeleted -> handleGridItem(event)

            is NoteEditorEvent.TextGridItemTextChanged,
            is NoteEditorEvent.TextGridItemRichSpansChanged,
            is NoteEditorEvent.TextGridItemTypographyChanged,
            is NoteEditorEvent.TextGridItemAdded -> handleTextGridItem(event)

            is NoteEditorEvent.TitleChanged ->
                _state.update { it.copy(title = event.value) }

            is NoteEditorEvent.CreateCategory -> {
                onCategoryCreate(event.name, event.colorArgb)
            }
            
            // ── Security ────────────────────────────────────────────────────────
            is NoteEditorEvent.SecretItemAdded,
            is NoteEditorEvent.SecretItemUnlockRequested,
            is NoteEditorEvent.SecretItemUnlockWithPassword,
            is NoteEditorEvent.SecretItemUnlockWithBiometric,
            is NoteEditorEvent.SecretItemLock,
            is NoteEditorEvent.LockAllSecretItems,
            is NoteEditorEvent.SecretItemBiometricFailed -> handleSecretItem(event)

            is NoteEditorEvent.ToggleSecretNote,
            is NoteEditorEvent.SecretNoteUnlockWithPassword,
            is NoteEditorEvent.SecretNoteLock -> handleSecretNote(event)
        }
    }

    // ── Grid Item ────────────────────────────────────────────────────────

    private fun handleGridItem(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ItemDeleted ->
                _state.update { state ->
                    state.copy(pages = state.pages.removeItem(event.pageId, event.itemId))
                }

            is NoteEditorEvent.ItemMoved ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId, event.itemId, revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Image -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.Text -> item.copy(x = event.newX, y = event.newY)
                                is GridItem.SecretItem -> item.copy(x = event.newX, y = event.newY)
                            }
                        }
                    )
                }

            is NoteEditorEvent.ItemResized ->
                _state.update { state ->
                    state.copy(
                        pages = state.pages.updateItem(
                            event.pageId, event.itemId, revertOnOverlap = true
                        ) { item ->
                            when (item) {
                                is GridItem.Checklist -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                                is GridItem.Image -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                                is GridItem.Text -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                                is GridItem.SecretItem -> item.copy(
                                    width = event.newWidth, height = event.newHeight,
                                    x = event.newX, y = event.newY
                                )
                            }
                        }
                    )
                }

            is NoteEditorEvent.ItemSelected ->
                _state.update { it.copy(selectedItemId = event.itemId) }

            else -> Unit
        }
    }

    // ── Text Grid Item ───────────────────────────────────────────────────

    private fun handleTextGridItem(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.TextGridItemAdded ->
                _state.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItemFactory.createTextItem(
                            width = event.width, height = event.height,
                            textContent = event.text
                        )
                    )
                    _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
                    state.copy(pages = newPages)
                }

            is NoteEditorEvent.TextGridItemRichSpansChanged ->
                if (state.value.transientDecryptedItems.containsKey(event.itemId)) {
                    updateTransientItem(event.itemId) { item ->
                        if (item is GridItem.Text) item.copy(richSpans = event.richSpans) else item
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                                if (item is GridItem.Text) item.copy(richSpans = event.richSpans) else item
                            }
                        )
                    }
                }

            is NoteEditorEvent.TextGridItemTextChanged ->
                if (state.value.transientDecryptedItems.containsKey(event.itemId)) {
                    updateTransientItem(event.itemId) { item ->
                        if (item is GridItem.Text) item.copy(textContent = event.text) else item
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                                if (item is GridItem.Text) item.copy(textContent = event.text) else item
                            }
                        )
                    }
                }

            is NoteEditorEvent.TextGridItemTypographyChanged ->
                if (state.value.transientDecryptedItems.containsKey(event.itemId)) {
                    updateTransientItem(event.itemId) { item ->
                        if (item is GridItem.Text) item.copy(
                            fontSize = event.fontSize,
                            textAlign = event.textAlign,
                            lineHeight = event.lineHeight
                        ) else item
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                                if (item is GridItem.Text) item.copy(
                                    fontSize = event.fontSize,
                                    textAlign = event.textAlign,
                                    lineHeight = event.lineHeight
                                ) else item
                            }
                        )
                    }
                }

            else -> Unit
        }
    }

    // ── Image Grid Item ──────────────────────────────────────────────────

    private fun handleImageGridItem(event: NoteEditorEvent.ImageGridItemAdded) {
        _state.update { state ->
            val (newPages, addedIndex) = state.pages.addItemToPage(
                event.targetPageIndex,
                GridItemFactory.createImageItem(
                    width = event.width, height = event.height,
                    imageUri = event.path
                )
            )
            _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
            state.copy(pages = newPages)
        }
    }

    // ── Checklist ────────────────────────────────────────────────────────

    private fun handleChecklist(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.ChecklistAction ->
                if (state.value.transientDecryptedItems.containsKey(event.itemId)) {
                    updateTransientItem(event.itemId) { item ->
                        if (item is GridItem.Checklist) {
                            applyChecklistAction(item, event.action)
                        } else item
                    }
                } else {
                    _state.update { state ->
                        state.copy(
                            pages = state.pages.updateItemSimple(event.pageId, event.itemId) { item ->
                                if (item is GridItem.Checklist) {
                                    applyChecklistAction(item, event.action)
                                } else item
                            }
                        )
                    }
                }

            is NoteEditorEvent.ChecklistGridItemAdded ->
                _state.update { state ->
                    val (newPages, addedIndex) = state.pages.addItemToPage(
                        event.targetPageIndex,
                        GridItemFactory.createChecklistItem(
                            width = event.width, height = event.height
                        )
                    )
                    _uiEvent.trySend(NoteEditorUiEvent.ScrollToPage(addedIndex))
                    state.copy(pages = newPages)
                }

            else -> Unit
        }
    }

    private fun applyChecklistAction(
        item: GridItem.Checklist,
        action: ChecklistActionType
    ): GridItem.Checklist = when (action) {
        is ChecklistActionType.EntryAdded ->
            item.copy(entries = item.entries + GridItemFactory.createCheckboxEntry())

        is ChecklistActionType.EntryDeleted ->
            item.copy(entries = item.entries.filter { it.id != action.entryId })

        is ChecklistActionType.EntryTextChanged ->
            item.copy(entries = item.entries.map { entry ->
                if (entry.id == action.entryId) entry.copy(text = action.text) else entry
            })

        is ChecklistActionType.EntryToggled ->
            item.copy(entries = item.entries.map { entry ->
                if (entry.id == action.entryId) entry.copy(isChecked = !entry.isChecked) else entry
            })

        is ChecklistActionType.TitleChanged ->
            item.copy(title = action.title)
    }

    // ── Rich Text ────────────────────────────────────────────────────────

    private fun handleRichText(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.EditingTextItemChanged ->
                _state.update { it.copy(editingTextItemId = event.itemId) }

            is NoteEditorEvent.RichStateUpdated -> {
                val oldState = _state.value.editingRichState
                _state.update { it.copy(editingRichState = event.state) }
                if (oldState != null &&
                    (oldState.fontSize != event.state.fontSize ||
                            oldState.textAlign != event.state.textAlign ||
                            oldState.lineHeight != event.state.lineHeight)
                ) {
                    persistTypography()
                }
            }

            else -> Unit
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────

    private fun handleDrawing(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.DrawingColorChanged ->
                _state.update { it.copy(drawingPenColorArgb = event.colorArgb, isEraserMode = false) }

            is NoteEditorEvent.DrawingEraserToggled ->
                _state.update { it.copy(isEraserMode = !it.isEraserMode) }

            is NoteEditorEvent.DrawingEraserWidthChanged ->
                _state.update { it.copy(drawingEraserWidthFraction = event.widthFraction) }

            is NoteEditorEvent.DrawingModeToggled ->
                _state.update { state ->
                    state.copy(
                        isDrawingMode = !state.isDrawingMode,
                        isEraserMode = if (state.isDrawingMode) false else state.isEraserMode
                    )
                }

            is NoteEditorEvent.DrawingStrokeAdded ->
                _state.update { state ->
                    val page = state.pages.find { it.id == event.pageId }
                    val currentStrokes = page?.strokes ?: emptyList()
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { p ->
                            if (p.id == event.pageId) p.copy(strokes = currentStrokes + event.stroke) else p
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokeReverted ->
                _state.update { state ->
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    if (pageStack.isEmpty()) return@update state
                    val previousStrokes = pageStack.last()
                    newStack[event.pageId] = pageStack.dropLast(1)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { page ->
                            if (page.id == event.pageId) page.copy(strokes = previousStrokes) else page
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokesUpdated ->
                _state.update { state ->
                    val page = state.pages.find { it.id == event.pageId }
                    val currentStrokes = page?.strokes ?: emptyList()
                    val newStack = state.drawingUndoStack.toMutableMap()
                    val pageStack = newStack[event.pageId] ?: emptyList()
                    newStack[event.pageId] = pageStack + listOf(currentStrokes)
                    state.copy(
                        drawingUndoStack = newStack,
                        pages = state.pages.map { p ->
                            if (p.id == event.pageId) p.copy(strokes = event.strokes) else p
                        }
                    )
                }

            is NoteEditorEvent.DrawingStrokeWidthChanged ->
                _state.update { it.copy(drawingStrokeWidthFraction = event.widthFraction) }

            else -> Unit
        }
    }

    // ── Persist Typography ───────────────────────────────────────────────

    private fun persistTypography() {
        val state = _state.value
        val itemId = state.editingTextItemId ?: return
        val richState = state.editingRichState ?: return
        
        if (state.transientDecryptedItems.containsKey(itemId)) {
            updateTransientItem(itemId) { item ->
                if (item is GridItem.Text) item.copy(
                    fontSize = richState.fontSize,
                    textAlign = richState.textAlign,
                    lineHeight = richState.lineHeight
                ) else item
            }
        } else {
            _state.update { currentState ->
                currentState.copy(
                    pages = currentState.pages.map { page ->
                        page.copy(
                            items = page.items.map { item ->
                                if (item is GridItem.Text && item.id == itemId) {
                                    item.copy(
                                        fontSize = richState.fontSize,
                                        textAlign = richState.textAlign,
                                        lineHeight = richState.lineHeight
                                    )
                                } else item
                            }
                        )
                    }
                )
            }
        }
    }

    /** Updates a transient decrypted item in-place. */
    private fun updateTransientItem(itemId: String, transform: (GridItem) -> GridItem) {
        _state.update { state ->
            val current = state.transientDecryptedItems[itemId] ?: return@update state
            state.copy(
                transientDecryptedItems = state.transientDecryptedItems + (itemId to transform(current))
            )
        }
    }

    // ── Secret Item ──────────────────────────────────────────────────────

    private fun handleSecretItem(event: NoteEditorEvent) {
        when (event) {
            is NoteEditorEvent.SecretItemAdded -> {
                if (encryptionManager == null || keyManager == null) return
                coroutineScope.launch {
                    try {
                        // Create the inner item based on the chosen type
                        val innerItem: GridItem = when (event.innerType) {
                            is SecretItemInnerType.Text -> GridItemFactory.createTextItem(
                                width = event.width, height = event.height
                            )
                            is SecretItemInnerType.Checklist -> GridItemFactory.createChecklistItem(
                                width = event.width, height = event.height
                            )
                            is SecretItemInnerType.Image -> GridItemFactory.createImageItem(
                                width = event.width, height = event.height,
                                imageUri = event.innerType.imageUri
                            )
                        }
                        val innerJson = Json.encodeToString(GridItem.serializer(), innerItem)
                        val salt = keyManager.generateSalt()
                        val secretKey = keyManager.deriveKeyFromPassword(event.password, salt)
                        val (ciphertextParams, ivParams, _) = encryptionManager.encryptString(innerJson, secretKey)
                        
                        val (newPages, addedIndex) = state.value.pages.addItemToPage(
                            event.targetPageIndex,
                            GridItemFactory.createSecretItem(
                                width = event.width, height = event.height,
                                encryptedPayload = ciphertextParams,
                                salt = android.util.Base64.encodeToString(salt, android.util.Base64.NO_WRAP),
                                iv = ivParams,
                                hasBiometric = event.useBiometric
                            )
                        )
                        _state.update { it.copy(pages = newPages) }
                        sendUiEvent(NoteEditorUiEvent.ScrollToPage(addedIndex))
                    } catch (e: Exception) {
                        sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Encryption failed: ${e.message}"))
                    }
                }
            }
            is NoteEditorEvent.SecretItemUnlockRequested -> {
                val item = findGridItem(event.pageId, event.itemId) as? GridItem.SecretItem ?: return
                if (item.hasBiometric && !item.isBiometricDisabled) {
                    val alias = "secret_item_${item.id}"
                    sendUiEvent(NoteEditorUiEvent.ShowBiometricPrompt(itemId = item.id, keystoreAlias = alias))
                } else {
                    _state.update { it.copy(showUnlockDialog = true, unlockTargetItemId = item.id) }
                }
            }
            is NoteEditorEvent.SecretItemUnlockWithPassword -> {
                if (encryptionManager == null || keyManager == null) return
                val item = findGridItem(event.pageId, event.itemId) as? GridItem.SecretItem ?: return
                coroutineScope.launch {
                    try {
                        val saltBytes = android.util.Base64.decode(item.salt, android.util.Base64.NO_WRAP)
                        val secretKey = keyManager.deriveKeyFromPassword(event.password, saltBytes)
                        val decryptedJson = encryptionManager.decryptString(item.encryptedPayload, item.iv, secretKey)
                        val decryptedItem = Json.decodeFromString(GridItem.serializer(), decryptedJson)
                        
                        _state.update { it.copy(
                            unlockedItemIds = it.unlockedItemIds + item.id,
                            transientDecryptedItems = it.transientDecryptedItems + (item.id to decryptedItem),
                            transientSecretItemKeys = it.transientSecretItemKeys + (item.id to secretKey),
                            showUnlockDialog = false,
                            unlockTargetItemId = null
                        ) }
                    } catch (e: Exception) {
                        sendUiEvent(NoteEditorUiEvent.UnlockFailed("Incorrect password", 0, 0))
                    }
                }
            }
            is NoteEditorEvent.SecretItemUnlockWithBiometric -> {
                if (encryptionManager == null) return
                val item = findGridItem(event.pageId, event.itemId) as? GridItem.SecretItem ?: return
                coroutineScope.launch {
                    try {
                        val decryptedJson = encryptionManager.decryptString(item.encryptedPayload, item.iv, event.decryptedKey)
                        val decryptedItem = Json.decodeFromString(GridItem.serializer(), decryptedJson)
                        
                        _state.update { it.copy(
                            unlockedItemIds = it.unlockedItemIds + item.id,
                            transientDecryptedItems = it.transientDecryptedItems + (item.id to decryptedItem),
                            transientSecretItemKeys = it.transientSecretItemKeys + (item.id to event.decryptedKey)
                        ) }
                    } catch (e: Exception) {
                        sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Biometric decryption failed"))
                    }
                }
            }
            is NoteEditorEvent.SecretItemLock -> {
                reEncryptSecretItem(event.itemId)
            }
            is NoteEditorEvent.LockAllSecretItems -> {
                // Re-encrypt all unlocked items before clearing
                val currentState = state.value
                for (itemId in currentState.unlockedItemIds) {
                    reEncryptSecretItem(itemId)
                }
                _state.update { it.copy(
                    unlockedItemIds = emptySet(),
                    transientDecryptedItems = emptyMap(),
                    transientSecretItemKeys = emptyMap(),
                    isSecretNoteUnlocked = if (it.isSecretNote) false else it.isSecretNoteUnlocked,
                    title = if (it.isSecretNote) "" else it.title,
                    pages = if (it.isSecretNote) emptyList() else it.pages
                ) }
            }
            is NoteEditorEvent.SecretItemBiometricFailed -> {
                _state.update { it.copy(showUnlockDialog = true, unlockTargetItemId = event.itemId) }
            }
            else -> Unit
        }
    }

    /** Re-encrypts the (possibly modified) transient inner item back into the SecretItem on the page. */
    private fun reEncryptSecretItem(itemId: String) {
        if (encryptionManager == null) return
        val currentState = state.value
        val modifiedInner = currentState.transientDecryptedItems[itemId] ?: return
        val secretKey = currentState.transientSecretItemKeys[itemId] ?: return
        
        coroutineScope.launch {
            try {
                val innerJson = Json.encodeToString(GridItem.serializer(), modifiedInner)
                val (cipherBase64, ivBase64, _) = encryptionManager.encryptString(innerJson, secretKey)
                
                _state.update { state ->
                    state.copy(
                        pages = state.pages.map { page ->
                            page.copy(
                                items = page.items.map { item ->
                                    if (item is GridItem.SecretItem && item.id == itemId) {
                                        item.copy(
                                            encryptedPayload = cipherBase64,
                                            iv = ivBase64
                                        )
                                    } else item
                                }
                            )
                        },
                        unlockedItemIds = state.unlockedItemIds - itemId,
                        transientDecryptedItems = state.transientDecryptedItems - itemId,
                        transientSecretItemKeys = state.transientSecretItemKeys - itemId
                    )
                }
            } catch (e: Exception) {
                sendUiEvent(NoteEditorUiEvent.ShowSnackbar("Failed to re-encrypt: ${e.message}"))
            }
        }
    }

    // ── Secret Note ──────────────────────────────────────────────────────
    private fun handleSecretNote(event: NoteEditorEvent) {
        // Will be called by viewmodel when saving/loading based on full state.
        when (event) {
            is NoteEditorEvent.ToggleSecretNote -> {
                _state.update { it.copy(
                    isSecretNote = true,
                    isSecretNoteUnlocked = true, // creator has it unlocked
                    transientSecretNotePassword = event.password,
                    transientSecretNoteBiometric = event.useBiometric
                ) }
            }
            is NoteEditorEvent.SecretNoteUnlockWithPassword -> {
                // The ViewModel handles the actual decryption and populates state.pages because
                // encrypted content exists on the Note entity, not in NoteEditorState.
            }
            is NoteEditorEvent.SecretNoteLock -> {
                _state.update { it.copy(
                    isSecretNoteUnlocked = false,
                    pages = emptyList(),
                    title = ""
                ) }
            }
            else -> Unit
        }
    }

    private fun findGridItem(pageId: String, itemId: String): GridItem? {
        val page = state.value.pages.find { it.id == pageId } ?: return null
        return page.items.find { it.id == itemId }
    }
}
