// NoteContent.kt
package com.metoly.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import com.metoly.components.grid.DrawingCanvas
import com.metoly.components.grid.GridCanvas
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.grid.DrawingStroke
import com.metoly.morganize.core.model.grid.NotePage
import com.metoly.morganize.core.model.grid.TextAlignment
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val TransparentFieldColors
    @Composable get() = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent
    )

/**
 * The main scrollable content area for a note.
 * Renders the title, category chips, and a vertically list of grid pages.
 * Handles scrolling, overscroll-to-add-page behaviour, and directs all grid events.
 *
 * @param modifier Standard Compose modifier.
 * @param title The title of the note.
 * @param onTitleChange Callback for title updates.
 * @param titleHint Placeholder text for empty title.
 * @param pages The ordered list of pages containing grid items and strokes.
 * @param selectedItemId The ID of the currently focused grid item, if any.
 * @param onItemSelected Callback when a grid item is focused.
 * @param onItemMoved Callback for completing an item drag operation.
 * @param onItemResized Callback for completing an item resize operation.
 * @param onItemTextChanged Callback when text changes inside a typed grid item.
 * @param onItemRichSpansChanged Callback when rich text spans are updated.
 * @param onItemTypographyChanged Callback when item typography updates.
 * @param onItemDeleted Callback when an item is removed.
 * @param onEditingTextItemChanged Callback indicating which text item is actively receiving text input.
 * @param editingTextItemId The ID of the currently active text input item.
 * @param activeRichState The rich text formatting state of the active text input box.
 * @param onActiveRichStateChange Sink for active rich text formatting state.
 * @param categories List of available categories for the horizontal filter row.
 * @param selectedCategoryId The ID of the applied category filter.
 * @param onCategorySelected Callback when a category filter is applied or removed.
 * @param onAddCategory Callback to invoke the add category bottom sheet.
 * @param onAddPage Callback to append a new page to the end of the note.
 * @param isReadOnly Prevents modifications to the content when true (e.g. List Screen).
 * @param isDrawingMode Locks scrolling and enables canvas strokes if true.
 * @param isEraserMode Whether strokes tapped/drawn will be removed.
 * @param penColorArgb Color applied to new drawn strokes.
 * @param strokeWidthFraction Canvas-relative width multiplier for new strokes.
 * @param eraserWidthFraction Canvas-relative width multiplier for eraser interactions.
 * @param onStrokeAdded Callback when a new stroke completes.
 * @param onStrokesUpdated Callback when strokes are modified (e.g., partially erased).
 * @param onChecklistTitleChanged Callback when checklist header text changes.
 * @param onCheckboxToggled Callback when checklist entry checked state toggles.
 * @param onCheckboxTextChanged Callback when checklist entry label text changes.
 * @param onCheckboxAdded Callback when a new entry is appended to a checklist.
 * @param onCheckboxDeleted Callback when an entry is removed from a checklist.
 * @param onEmptyGridAddClicked Callback when the placeholder add button in an empty grid is tapped.
 * @param onActivePageChanged Callback yielding the index of the page most prominent by scroll position.
 * @param lazyListState Shared scroll state for the vertical column.
 * @param unlockedItemIds Set of Secret Item IDs that are temporarily visible.
 * @param transientDecryptedItems Map of unpersisted, decrypted content for Secret Items.
 * @param onSecretItemUnlockRequested Callback to prompt the user to decrypt a locked secret item.
 */
@Composable
fun NoteContent(
    modifier: Modifier = Modifier,
    title: String,
    onTitleChange: (String) -> Unit,
    titleHint: String,
    pages: List<NotePage>,
    selectedItemId: String?,
    onItemSelected: (String?) -> Unit,
    onItemMoved: (pageId: String, itemId: String, newX: Int, newY: Int) -> Unit,
    onItemResized: (pageId: String, itemId: String, newWidth: Int, newHeight: Int, newX: Int, newY: Int) -> Unit,
    onItemTextChanged: (pageId: String, itemId: String, text: String) -> Unit,
    onItemRichSpansChanged: (pageId: String, itemId: String, richSpans: List<RichSpan>) -> Unit,
    onItemTypographyChanged: (pageId: String, itemId: String, fontSize: Float, textAlign: TextAlignment, lineHeight: Float) -> Unit,
    onItemDeleted: (pageId: String, itemId: String) -> Unit,
    onEditingTextItemChanged: (itemId: String?, richState: RichTextEditorState?) -> Unit,
    editingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    categories: List<Category>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    onAddCategory: () -> Unit = {},
    onAddPage: () -> Unit,
    isReadOnly: Boolean = false,
    isDrawingMode: Boolean = false,
    isEraserMode: Boolean = false,
    penColorArgb: Long = 0xFF000000L,
    strokeWidthFraction: Float = 0.008f,
    eraserWidthFraction: Float = 0.04f,
    onStrokeAdded: (pageId: String, stroke: DrawingStroke) -> Unit = { _, _ -> },
    onStrokesUpdated: (pageId: String, strokes: List<DrawingStroke>) -> Unit = { _, _ -> },
    onChecklistTitleChanged: (pageId: String, itemId: String, title: String) -> Unit = { _, _, _ -> },
    onCheckboxToggled: (pageId: String, itemId: String, entryId: String) -> Unit = { _, _, _ -> },
    onCheckboxTextChanged: (pageId: String, itemId: String, entryId: String, text: String) -> Unit = { _, _, _, _ -> },
    onCheckboxAdded: (pageId: String, itemId: String) -> Unit = { _, _ -> },
    onCheckboxDeleted: (pageId: String, itemId: String, entryId: String) -> Unit = { _, _, _ -> },
    onEmptyGridAddClicked: () -> Unit = {},
    onActivePageChanged: (Int) -> Unit = {},
    lazyListState: androidx.compose.foundation.lazy.LazyListState = rememberLazyListState(),
    unlockedItemIds: Set<String> = emptySet(),
    transientDecryptedItems: Map<String, com.metoly.morganize.core.model.grid.GridItem> = emptyMap(),
    onSecretItemUnlockRequested: (pageId: String, itemId: String) -> Unit = { _, _ -> }
) {
    val focusManager = LocalFocusManager.current
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }
    val thresholdPx = screenHeightPx * 0.2f

    val overscrollPx = remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()
    
    val firstVisibleItemIndex by remember { derivedStateOf { lazyListState.firstVisibleItemIndex } }
    val activePageIndex = maxOf(0, firstVisibleItemIndex - 1)

    LaunchedEffect(activePageIndex) {
        onActivePageChanged(activePageIndex)
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollPx.floatValue < 0f && source == NestedScrollSource.UserInput) {
                    val newVal = overscrollPx.floatValue + available.y
                    if (newVal > 0f) {
                        val consumed = -overscrollPx.floatValue
                        overscrollPx.floatValue = 0f
                        return Offset(0f, consumed)
                    } else {
                        overscrollPx.floatValue = newVal
                        return available
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                if (source == NestedScrollSource.UserInput && available.y < 0) {
                    overscrollPx.floatValue += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (overscrollPx.floatValue < 0f) {
                    if (overscrollPx.floatValue <= -thresholdPx) {
                        onAddPage()
                    }
                    coroutineScope.launch {
                        Animatable(overscrollPx.floatValue).animateTo(0f) {
                            overscrollPx.floatValue = value
                        }
                    }
                    return available
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().nestedScroll(nestedScrollConnection)) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(0, overscrollPx.floatValue.roundToInt()) },
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
        item(key = "header") {
            Spacer(Modifier.height(MorgDimens.spacingLg))

            CategoryChipRow(
                categories = categories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = onCategorySelected,
                onAddCategory = onAddCategory
            )

            Spacer(Modifier.height(MorgDimens.spacingLg))

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                modifier = Modifier.fillMaxWidth().padding(horizontal = MorgDimens.spacingSm),
                placeholder = { Text(titleHint, style = MaterialTheme.typography.headlineSmall) },
                textStyle = MaterialTheme.typography.headlineSmall,
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = TransparentFieldColors
            )

            Spacer(Modifier.height(MorgDimens.spacingLg))
        }

        itemsIndexed(pages, key = { _, page -> page.id }) { index, page ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Page ${index + 1}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(
                        horizontal = MorgDimens.screenPaddingHorizontal,
                        vertical = MorgDimens.spacingSm
                    )
                )


                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    GridCanvas(
                        page = page,
                        selectedItemId = selectedItemId,
                        onItemSelected = onItemSelected,
                        onItemMoved = { itemId, newX, newY -> onItemMoved(page.id, itemId, newX, newY) },
                        onItemResized = { itemId, newW, newH, newX, newY ->
                            onItemResized(page.id, itemId, newW, newH, newX, newY)
                        },
                        onItemTextChanged = { itemId, text -> onItemTextChanged(page.id, itemId, text) },
                        onItemRichSpansChanged = { itemId, spansJson ->
                            onItemRichSpansChanged(page.id, itemId, spansJson)
                        },
                        onItemTypographyChanged = { itemId, fontSize, textAlign, lineHeight ->
                            onItemTypographyChanged(page.id, itemId, fontSize, textAlign, lineHeight)
                        },
                        onItemDeleted = { itemId -> onItemDeleted(page.id, itemId) },
                        onEditingTextItemChanged = onEditingTextItemChanged,
                        editingTextItemId = editingTextItemId,
                        activeRichState = activeRichState,
                        onActiveRichStateChange = onActiveRichStateChange,
                        onChecklistTitleChanged = { itemId, title -> onChecklistTitleChanged(page.id, itemId, title) },
                        onCheckboxToggled = { itemId, entryId -> onCheckboxToggled(page.id, itemId, entryId) },
                        onCheckboxTextChanged = { itemId, entryId, text -> onCheckboxTextChanged(page.id, itemId, entryId, text) },
                        onCheckboxAdded = { itemId -> onCheckboxAdded(page.id, itemId) },
                        onCheckboxDeleted = { itemId, entryId -> onCheckboxDeleted(page.id, itemId, entryId) },
                        onEmptyGridAddClicked = onEmptyGridAddClicked,
                        modifier = Modifier.padding(horizontal = 0.dp),
                        isReadOnly = isReadOnly || isDrawingMode,
                        showEmptyGridPlaceholder = index == 0,
                        unlockedItemIds = unlockedItemIds,
                        transientDecryptedItems = transientDecryptedItems,
                        onSecretItemUnlockRequested = { itemId -> onSecretItemUnlockRequested(page.id, itemId) }
                    )

                    if (isDrawingMode || page.strokes.isNotEmpty()) {
                        DrawingCanvas(
                            strokes = page.strokes,
                            isActive = isDrawingMode,
                            isEraserMode = isEraserMode,
                            penColorArgb = penColorArgb,
                            strokeWidthFraction = strokeWidthFraction,
                            eraserWidthFraction = eraserWidthFraction,
                            onStrokeFinished = { stroke ->
                                onStrokeAdded(page.id, stroke)
                            },
                            onStrokesChanged = { updatedStrokes ->
                                onStrokesUpdated(page.id, updatedStrokes)
                            },
                            modifier = Modifier.matchParentSize()
                        )
                    }
                }
            }
        }
    }
    if (overscrollPx.floatValue < 0f) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .height(with(density) { (-overscrollPx.floatValue).toDp() }),
            contentAlignment = Alignment.Center
        ) {
            val progress = (-overscrollPx.floatValue / thresholdPx).coerceIn(0f, 1f)
            val isReady = progress >= 1f
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AnimatedContent(targetState = isReady, label = "add_icon") { ready ->
                    if (ready) {
                        Icon(Icons.Default.Add, contentDescription = "Add page", modifier = Modifier.size(32.dp))
                    } else {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Drag up", modifier = Modifier.size(32.dp))
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isReady) "release to add page" else "(drag for new page)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
}