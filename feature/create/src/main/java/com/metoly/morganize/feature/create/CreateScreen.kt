package com.metoly.morganize.feature.create

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.metoly.components.AddItemBottomSheet
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
import javax.crypto.Cipher
import com.metoly.components.rememberNoteImagePicker
import com.metoly.morganize.feature.create.components.CreateTopBar
import kotlinx.coroutines.flow.collectLatest

/**
 * Screen for creating a new note.
 * Initializes an empty grid canvas and handles all user interactions for drafting,
 * styling, and optionally securing note content before persisting it to the database.
 *
 * @param viewModel The ViewModel overseeing note creation state.
 * @param onBack Callback when pressing back or navigating away without saving.
 * @param onSaved Callback indicating the note was persisted successfully.
 */
@Composable
fun CreateScreen(viewModel: CreateViewModel, onBack: () -> Unit, onSaved: () -> Unit) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lazyListState = rememberLazyListState()

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
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.delegate.onEvent(NoteEditorEvent.LockAllSecretItems)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
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

    val secretImagePickerLauncher = rememberNoteImagePicker { path ->
        isPendingSecretImage = false
        pendingSecretItemType = SecretItemInnerType.Image(path)
        showSetCredentialsForSecretItem = true
    }

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collectLatest { event ->
            when (event) {
                is NoteEditorUiEvent.SaveSuccess -> onSaved()
                is NoteEditorUiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is NoteEditorUiEvent.ScrollToPage -> lazyListState.animateScrollToItem(event.pageIndex + 1)
                is NoteEditorUiEvent.ShowBiometricPrompt -> {

                    val keyManager = com.metoly.morganize.core.model.security.KeyManager()
                    try {
                        val secretKey = keyManager.getOrCreateBiometricKey(event.keystoreAlias)
                        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
                        if (event.decryptionIv != null) {
                            val ivBytes = android.util.Base64.decode(event.decryptionIv, android.util.Base64.NO_WRAP)
                            val spec = javax.crypto.spec.GCMParameterSpec(128, ivBytes)
                            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
                        } else {
                            cipher.init(Cipher.DECRYPT_MODE, secretKey)
                        }
                        biometricHelper.showBiometricPrompt(
                            activity = context as FragmentActivity,
                            cipher = cipher,
                            onSuccess = { _ ->
                                val id = event.itemId
                                if (id != null) {
                                    viewModel.delegate.onEvent(NoteEditorEvent.SecretItemUnlockWithBiometric(
                                       pageId = uiState.pages[activePageIndex].id,
                                       itemId = id,
                                       decryptedKey = secretKey
                                    ))
                                } else {
                                    viewModel.delegate.onEvent(NoteEditorEvent.SecretNoteUnlockWithBiometric(secretKey))

                                }
                            },
                            onFailed = {
                                val id = event.itemId
                                if (id != null) {
                                    viewModel.delegate.onEvent(NoteEditorEvent.SecretItemBiometricFailed(
                                       pageId = uiState.pages[activePageIndex].id,
                                       itemId = id
                                    ))
                                } else {
                                    viewModel.delegate.onEvent(NoteEditorEvent.SecretNoteBiometricFailed)
                                }
                            }
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("BIOMETRIC", "Failed to init cipher", e)
                        snackbarHostState.showSnackbar("Biometric Error: ${e.message}")
                        val id = event.itemId
                        if (id != null) {
                             viewModel.delegate.onEvent(NoteEditorEvent.SecretItemBiometricFailed(
                                pageId = uiState.pages[activePageIndex].id,
                                itemId = id
                             ))
                        } else {
                             viewModel.delegate.onEvent(NoteEditorEvent.SecretNoteBiometricFailed)
                        }
                    }
                }
                is NoteEditorUiEvent.UnlockFailed -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CreateTopBar(
                onBack = onBack,
                selectedColor = uiState.backgroundColor,
                onColorSelected = { colorArgb ->
                    viewModel.delegate.onEvent(NoteEditorEvent.BackgroundColorChanged(colorArgb = colorArgb))
                },
                isSecretNote = uiState.isSecretNote,
                onToggleSecretNote = {
                    if (!uiState.isSecretNote) {
                        showSetCredentialsForSecretNote = true
                    }
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
                saveContentDescription = stringResource(R.string.feature_create_save),
                imagePickerLauncher = imagePickerLauncher
            )
        }
    ) { padding ->
        NoteEditorContent(
            state = uiState,
            onEvent = viewModel.delegate::onEvent,
            titleHint = stringResource(R.string.feature_create_title_hint),
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
}