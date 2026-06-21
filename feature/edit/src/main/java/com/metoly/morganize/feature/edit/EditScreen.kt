package com.metoly.morganize.feature.edit
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.components.AddItemBottomSheet
import com.metoly.components.DeleteNoteDialog
import com.metoly.components.NoteEditorBottomBar
import com.metoly.components.NoteEditorContent
import com.metoly.components.RichTextEditorState
import com.metoly.components.model.NoteEditorEvent
import com.metoly.components.model.NoteEditorUiEvent
import com.metoly.components.rememberNoteImagePicker
import com.metoly.components.security.BiometricHelper
import com.metoly.components.security.SetCredentialsBottomSheet
import com.metoly.components.security.UnlockBottomSheet
import com.metoly.components.security.SecretItemTypePickerBottomSheet
import com.metoly.components.model.SecretItemInnerType
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.metoly.morganize.feature.edit.components.EditTopBar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.activity.compose.BackHandler
import android.content.Intent

/**
 * Screen designed for modifying an existing Note.
 * Handles the display and dynamic unlocking of secured notes, standard grid editing features,
 * and saving modifications with persisted encryption wrappers.
 *
 * @param viewModel The ViewModel actively coordinating local note state and editing events.
 * @param onBack Callback invoked when leaving without completing the save successfully.
 * @param onDone Callback invoked when user clicks save and the view model commits updates.
 */
@Composable
fun EditScreen(viewModel: EditViewModel, onBack: () -> Unit, onDone: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    var activeRichState by remember { mutableStateOf<RichTextEditorState?>(null) }
    var activeEditingTextItemId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.editingTextItemId) {
        activeEditingTextItemId = uiState.editingTextItemId
        if (uiState.editingTextItemId == null) {
            activeRichState = null
        } else {
            activeRichState = uiState.editingRichState
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.delegate.onEvent(NoteEditorEvent.LockAllSecretItems)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val updateRichState: (RichTextEditorState) -> Unit = { newState ->
        activeRichState = newState
        viewModel.delegate.onEvent(NoteEditorEvent.RichStateUpdated(newState))
    }

    var showAddItemSheet by remember { mutableStateOf(false) }
    var isPending5x5Image by remember { mutableStateOf(false) }
    var activePageIndex by remember { mutableStateOf(0) }

    val imagePickerLauncher = rememberNoteImagePicker { path ->
        if (isPending5x5Image) {
            viewModel.delegate.onEvent(NoteEditorEvent.ImageGridItemAdded(path, targetPageIndex = activePageIndex, width = 5, height = 5))
            isPending5x5Image = false
        } else {
            viewModel.delegate.onEvent(NoteEditorEvent.ImageGridItemAdded(path = path, targetPageIndex = activePageIndex))
        }
    }

    val context = LocalContext.current
    val biometricHelper = remember { BiometricHelper(context) }
    val isBiometricAvailable = remember { biometricHelper.isBiometricAvailable() }

    var showSecretItemTypePicker by remember { mutableStateOf(false) }
    var pendingSecretItemType by remember { mutableStateOf<SecretItemInnerType?>(null) }
    var showSetCredentialsForSecretItem by remember { mutableStateOf(false) }
    var showSetCredentialsForSecretNote by remember { mutableStateOf(false) }
    var isPendingSecretImage by remember { mutableStateOf(false) }
    
    var showReminderPicker by remember { mutableStateOf(false) }

    val secretImagePickerLauncher = rememberNoteImagePicker { path ->
        isPendingSecretImage = false
        pendingSecretItemType = SecretItemInnerType.Image(path)
        showSetCredentialsForSecretItem = true
    }

    var showUnsavedDialog by remember { mutableStateOf(false) }

    BackHandler {
        if (viewModel.hasUnsavedChanges) {
            showUnsavedDialog = true
        } else {
            onBack()
        }
    }

    if (showUnsavedDialog) {
        com.metoly.components.UnsavedChangesDialog(
            onConfirmSave = {
                showUnsavedDialog = false
                viewModel.save()
            },
            onDiscard = {
                showUnsavedDialog = false
                onBack()
            },
            onDismiss = {
                showUnsavedDialog = false
            }
        )
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoteEditorUiEvent.SaveSuccess -> onDone()
                is NoteEditorUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.asString(context))
                is NoteEditorUiEvent.ScrollToPage -> lazyListState.animateScrollToItem(event.pageIndex + 1)
                is NoteEditorUiEvent.ShowBiometricPrompt -> {
                    com.metoly.components.security.BiometricCipherHandler.handleBiometricPrompt(
                        event = event,
                        activity = context as FragmentActivity,
                        biometricHelper = biometricHelper,
                        keyManager = viewModel.delegate.keyManager!!,
                        delegate = viewModel.delegate,
                        activePageId = uiState.pages[activePageIndex].id,
                        onError = { msg -> coroutineScope.launch { snackbarHostState.showSnackbar(msg) } },
                        onSecretNoteUnlock = { secretKey ->
                            viewModel.unlockSecretNoteWithBiometric(secretKey)
                        }
                    )
                }
                is NoteEditorUiEvent.UnlockFailed -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    if (editState.showDeleteDialog) {
        DeleteNoteDialog(
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.dismissDelete() }
        )
    }

    if (showReminderPicker) {
        com.metoly.components.ReminderDateTimePickerDialog(
            initialReminderAt = uiState.reminderAt,
            onDismissRequest = { showReminderPicker = false },
            onReminderSet = { timestamp ->
                viewModel.delegate.onEvent(NoteEditorEvent.ReminderChanged(timestamp))
                showReminderPicker = false
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            EditTopBar(
                onBack = {
                    if (viewModel.hasUnsavedChanges) {
                        showUnsavedDialog = true
                    } else {
                        onBack()
                    }
                },
                onDeleteClick = { viewModel.requestDelete() },
                selectedColor = uiState.backgroundColor,
                onColorSelected = { viewModel.delegate.onEvent(NoteEditorEvent.BackgroundColorChanged(it)) },
                isSecretNote = uiState.isSecretNote,
                onToggleSecretNote = {
                    if (!uiState.isSecretNote) {
                        showSetCredentialsForSecretNote = true
                    } else {
                        viewModel.delegate.onEvent(NoteEditorEvent.SecretNoteLock)
                    }
                },
                hasReminder = uiState.reminderAt != null,
                onReminderClick = { showReminderPicker = true },
                onShareClick = {
                    val currentNote = viewModel.originalNote ?: return@EditTopBar
                    // We also need current edit state. To keep it simple, we share the existing note
                    val shareText = currentNote.toShareText()
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, shareText)
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, null))
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NoteEditorBottomBar(
                state = uiState,
                activePageIndex = activePageIndex,
                activeEditingTextItemId = activeEditingTextItemId,
                activeRichState = activeRichState,
                onRichStateUpdate = updateRichState,
                onEvent = viewModel.delegate::onEvent,
                onAddSecretItem = { showSecretItemTypePicker = true },
                onSave = viewModel::save,
                saveContentDescription = stringResource(R.string.feature_edit_save),
                imagePickerLauncher = imagePickerLauncher
            )
        }
    ) { padding ->
        NoteEditorContent(
            state = uiState,
            onEvent = viewModel.delegate::onEvent,
            titleHint = stringResource(R.string.feature_edit_title_hint),
            activeEditingTextItemId = activeEditingTextItemId,
            activeRichState = activeRichState,
            onActiveRichStateChange = updateRichState,
            onEmptyGridAddClicked = { showAddItemSheet = true },
            onActivePageChanged = { activePageIndex = it },
            lazyListState = lazyListState,
            unlockedItemIds = uiState.unlockedItemIds,
            transientDecryptedItems = uiState.transientDecryptedItems,
            onSecretItemUnlockRequested = { pageId, itemId ->
                viewModel.delegate.onEvent(NoteEditorEvent.SecretItemUnlockRequested(
                    pageId = pageId,
                    itemId = itemId
                ))
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        )
    }

    if (showAddItemSheet) {
        AddItemBottomSheet(
            onDismiss = { showAddItemSheet = false },
            onAddText = {
                viewModel.delegate.onEvent(NoteEditorEvent.TextGridItemAdded("", activePageIndex, width = 5, height = 5))
            },
            onAddImage = { isPending5x5Image = true },
            onAddChecklist = {
                viewModel.delegate.onEvent(NoteEditorEvent.ChecklistGridItemAdded(activePageIndex, width = 5, height = 5))
            },
            onAddSecretItem = {
                showSecretItemTypePicker = true
            },
            imagePickerLauncher = imagePickerLauncher
        )
    }

    if (showSecretItemTypePicker) {
        SecretItemTypePickerBottomSheet(
            onTextSelected = {
                pendingSecretItemType = SecretItemInnerType.Text
                showSetCredentialsForSecretItem = true
            },
            onChecklistSelected = {
                pendingSecretItemType = SecretItemInnerType.Checklist
                showSetCredentialsForSecretItem = true
            },
            onImageSelected = {
                isPendingSecretImage = true
                secretImagePickerLauncher.launch(
                    androidx.activity.result.PickVisualMediaRequest(
                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            },
            onDismiss = { showSecretItemTypePicker = false }
        )
    }

    if (showSetCredentialsForSecretItem && pendingSecretItemType != null) {
        SetCredentialsBottomSheet(
            isBiometricAvailable = isBiometricAvailable,
            onConfirm = { password, useBiometric ->
                showSetCredentialsForSecretItem = false
                val innerType = pendingSecretItemType!!
                pendingSecretItemType = null
                viewModel.delegate.onEvent(NoteEditorEvent.SecretItemAdded(
                    targetPageIndex = activePageIndex,
                    innerType = innerType,
                    password = password,
                    useBiometric = useBiometric
                ))
            },
            onDismiss = {
                showSetCredentialsForSecretItem = false
                pendingSecretItemType = null
            }
        )
    }

    if (showSetCredentialsForSecretNote) {
        SetCredentialsBottomSheet(
            title = "Set Note Password",
            isBiometricAvailable = isBiometricAvailable,
            onConfirm = { password, useBiometric ->
                showSetCredentialsForSecretNote = false
                viewModel.delegate.onEvent(NoteEditorEvent.ToggleSecretNote(
                    password = password,
                    useBiometric = useBiometric
                ))
            },
            onDismiss = { showSetCredentialsForSecretNote = false }
        )
    }

    if (uiState.showUnlockDialog) {
        UnlockBottomSheet(
            title = "Unlock Item",
            onConfirm = { password ->
                uiState.unlockTargetItemId?.let { itemId ->
                    viewModel.delegate.onEvent(NoteEditorEvent.SecretItemUnlockWithPassword(
                        pageId = uiState.pages[activePageIndex].id,
                        itemId = itemId,
                        password = password
                    ))
                }
            },
            onDismiss = {
                viewModel.delegate.updateState { it.copy(showUnlockDialog = false) }
            }
        )
    }

    if (uiState.isSecretNote && !uiState.isSecretNoteUnlocked) {
        UnlockBottomSheet(
            title = "Unlock Note",
            showBiometricButton = uiState.transientSecretNoteBiometric && uiState.secretNoteBiometricIv != null,
            onBiometricRequested = {
                uiState.secretNoteBiometricIv?.let { iv ->
                    viewModel.delegate.sendUiEvent(NoteEditorUiEvent.ShowBiometricPrompt(
                        itemId = null,
                        keystoreAlias = "secret_note_master_key",
                        decryptionIv = iv
                    ))
                }
            },
            onConfirm = { password ->
                viewModel.unlockSecretNote(password)
            },
            onDismiss = {
                onBack()
            }
        )
    }
}
