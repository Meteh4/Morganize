// GridCanvas.kt
package com.metoly.components.grid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.zIndex
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.metoly.components.RichTextEditor
import com.metoly.components.RichTextEditorState
import com.metoly.components.continueList
import com.metoly.components.richTextStateFromPersisted
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.NotePage
import com.metoly.morganize.core.model.grid.TextAlignment
import com.metoly.morganize.core.ui.theme.MorgAnimation
import com.metoly.morganize.core.ui.theme.MorgColors
import com.metoly.morganize.core.ui.theme.MorgDimens
import com.metoly.morganize.core.ui.theme.MorgShapes
import kotlin.math.roundToInt


/**
 * Standard visual properties for rendering any grid item on the canvas.
 * Consolidates paddings, borders, shapes, and semi-transparent backgrounds.
 */
object GridItemDefaults {
    val DefaultPadding = MorgDimens.gridItemPadding
    val SelectedPadding = MorgDimens.gridItemPadding
    val CornerRadius = MorgDimens.gridItemCorner
    val SelectedBorderWidth = MorgDimens.gridItemSelectedBorder
    val UnselectedBorderWidth = 0.dp
    val BackgroundAlpha = MorgColors.GridItemBackgroundAlpha
    val InnerPadding = MorgDimens.gridItemInnerPadding

    val Shape: RoundedCornerShape
        get() = MorgShapes.gridItem

    @Composable
    fun backgroundColor(): Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = BackgroundAlpha)
}

/**
 * An interactive 2D grid responsible for rendering and positioning Note items.
 * Allows items (Text, Checklists, Images, SecretItems) to be dragged, resized, and modified.
 * Also handles coordinating between locked/unlocked states for Secret Items via the [transientDecryptedItems].
 *
 * @param modifier Compose modifier.
 * @param page The specific note page data model this canvas renders.
 * @param selectedItemId The ID of the currently focused grid item.
 * @param onItemSelected Callback when an item is tapped.
 * @param onItemMoved Callback when an item's position changes via drag.
 * @param onItemResized Callback when an item's dimension changes via drag handle.
 * @param onItemTextChanged Callback for text mutations inside an item.
 * @param onItemRichSpansChanged Callback for formatted range updates.
 * @param onItemTypographyChanged Callback for structural typography changes.
 * @param onItemDeleted Callback to remove an item.
 * @param onEditingTextItemChanged Callback indicating which text field is focused.
 * @param editingTextItemId The ID of the currently focused text field.
 * @param activeRichState The underlying formatting state of the active text field.
 * @param onActiveRichStateChange Sink for formatting state.
 * @param onChecklistTitleChanged Callback for checklist header changes.
 * @param onCheckboxToggled Callback to toggle a checklist entry.
 * @param onCheckboxTextChanged Callback for editing a checklist entry label.
 * @param onCheckboxAdded Callback to append a new entry to a checklist.
 * @param onCheckboxDeleted Callback to remove an entry from a checklist.
 * @param onEmptyGridAddClicked Callback triggered when tapping the center of an empty grid.
 * @param unlockedItemIds IDs of SecretItems that have been authenticated and are currently visible.
 * @param transientDecryptedItems Ephemeral map storing decrypted SecretItem data locally in memory, never persisted.
 * @param onSecretItemUnlockRequested Callback to prompt the user for credentials to unlock a specific secret item.
 * @param columns Number of logical columns in the grid.
 * @param rows Number of logical rows in the grid.
 * @param isReadOnly Disables interactions and edits if true.
 * @param showEmptyGridPlaceholder Toggles the empty "Tap to add" placeholder.
 */
@Composable
fun GridCanvas(
    modifier: Modifier = Modifier,
    page: NotePage,
    selectedItemId: String?,
    onItemSelected: (String?) -> Unit = {},
    onItemMoved: (itemId: String, newX: Int, newY: Int) -> Unit = { _, _, _ -> },
    onItemResized: (itemId: String, newWidth: Int, newHeight: Int, newX: Int, newY: Int) -> Unit = { _, _, _, _, _ -> },
    onItemTextChanged: (itemId: String, newText: String) -> Unit = { _, _ -> },
    onItemRichSpansChanged: (itemId: String, richSpans: List<RichSpan>) -> Unit = { _, _ -> },
    onItemTypographyChanged: (itemId: String, fontSize: Float, textAlign: TextAlignment, lineHeight: Float) -> Unit = { _, _, _, _ -> },
    onItemDeleted: (itemId: String) -> Unit = {},
    onEditingTextItemChanged: (itemId: String?, richState: RichTextEditorState?) -> Unit = { _, _ -> },
    editingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    onChecklistTitleChanged: (itemId: String, title: String) -> Unit = { _, _ -> },
    onCheckboxToggled: (itemId: String, entryId: String) -> Unit = { _, _ -> },
    onCheckboxTextChanged: (itemId: String, entryId: String, text: String) -> Unit = { _, _, _ -> },
    onCheckboxAdded: (itemId: String) -> Unit = {},
    onCheckboxDeleted: (itemId: String, entryId: String) -> Unit = { _, _ -> },
    onEmptyGridAddClicked: () -> Unit = {},
    unlockedItemIds: Set<String> = emptySet(),
    transientDecryptedItems: Map<String, GridItem> = emptyMap(),
    onSecretItemUnlockRequested: (itemId: String) -> Unit = {},
    columns: Int = 10,
    rows: Int = 20,
    isReadOnly: Boolean = false,
    showEmptyGridPlaceholder: Boolean = true
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    val cellSize = if (columns > 0) canvasWidth / columns else 0f
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(shape = RoundedCornerShape(16.dp))
            .fillMaxWidth()
            .onSizeChanged { canvasWidth = it.width.toFloat() }
            .then(
                if (cellSize > 0f) Modifier.height(with(density) { (cellSize * rows).toDp() })
                else Modifier
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onItemSelected(null) }
            .background(MaterialTheme.colorScheme.surface)
    ) {
        if (cellSize > 0f) {
            if (page.items.isEmpty() && !isReadOnly && showEmptyGridPlaceholder) {
                // Empty grid — show a 5×5 "Add Item" button in the top-left corner
                val addSize = cellSize * 5
                Box(
                    modifier = Modifier
                        .size(with(density) { addSize.toDp() })
                        .padding(GridItemDefaults.DefaultPadding)
                        .clip(GridItemDefaults.Shape)
                        .background(GridItemDefaults.backgroundColor())
                        .clickable { onEmptyGridAddClicked() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add item",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            page.items.forEach { item ->
                GridDraggableItem(
                    item = item,
                    cellSize = cellSize,
                    columns = columns,
                    rows = rows,
                    isSelected = selectedItemId == item.id,
                    isReadOnly = isReadOnly,
                    onClick = { onItemSelected(item.id) },
                    onMove = { newX, newY -> onItemMoved(item.id, newX, newY) },
                    onResize = { newW, newH, newX, newY ->
                        onItemResized(item.id, newW, newH, newX, newY)
                    },
                    onTextChanged = { text -> onItemTextChanged(item.id, text) },
                    onRichSpansChanged = { spans -> onItemRichSpansChanged(item.id, spans) },
                    onTypographyChanged = { fontSize, textAlign, lineHeight -> onItemTypographyChanged(item.id, fontSize, textAlign, lineHeight) },
                    onDelete = { onItemDeleted(item.id) },
                    onEditingChanged = { isEditing, richState ->
                        onEditingTextItemChanged(
                            if (isEditing) item.id else null,
                            if (isEditing) richState else null
                        )
                    },
                    editingTextItemId = editingTextItemId,
                    activeRichState = activeRichState,
                    onActiveRichStateChange = onActiveRichStateChange,
                    onChecklistTitleChanged = { title -> onChecklistTitleChanged(item.id, title) },
                    onCheckboxToggled = { entryId -> onCheckboxToggled(item.id, entryId) },
                    onCheckboxTextChanged = { entryId, text -> onCheckboxTextChanged(item.id, entryId, text) },
                    onCheckboxAdded = { onCheckboxAdded(item.id) },
                    onCheckboxDeleted = { entryId -> onCheckboxDeleted(item.id, entryId) },
                    isUnlocked = unlockedItemIds.contains(item.id),
                    decryptedItem = transientDecryptedItems[item.id],
                    onSecretItemUnlockRequested = { onSecretItemUnlockRequested(item.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GridDraggableItem(
    item: GridItem,
    cellSize: Float,
    columns: Int,
    rows: Int,
    isSelected: Boolean,
    isReadOnly: Boolean,
    onClick: () -> Unit,
    onMove: (Int, Int) -> Unit,
    onResize: (Int, Int, Int, Int) -> Unit,
    onTextChanged: (String) -> Unit,
    onRichSpansChanged: (List<RichSpan>) -> Unit,
    onTypographyChanged: (Float, TextAlignment, Float) -> Unit,
    onDelete: () -> Unit,
    onEditingChanged: (Boolean, RichTextEditorState) -> Unit,
    editingTextItemId: String?,
    activeRichState: RichTextEditorState?,
    onActiveRichStateChange: (RichTextEditorState) -> Unit,
    onChecklistTitleChanged: (String) -> Unit = {},
    onCheckboxToggled: (String) -> Unit = {},
    onCheckboxTextChanged: (String, String) -> Unit = { _, _ -> },
    onCheckboxAdded: () -> Unit = {},
    onCheckboxDeleted: (String) -> Unit = {},
    isUnlocked: Boolean = false,
    decryptedItem: GridItem? = null,
    onSecretItemUnlockRequested: (String) -> Unit = {}
) {
    val density = LocalDensity.current

    var dragOffsetX by remember(item.x) { mutableFloatStateOf(0f) }
    var dragOffsetY by remember(item.y) { mutableFloatStateOf(0f) }

    var resizeW by remember(item.width) { mutableFloatStateOf(0f) }
    var resizeH by remember(item.height) { mutableFloatStateOf(0f) }
    var resizeX by remember(item.x) { mutableFloatStateOf(0f) }
    var resizeY by remember(item.y) { mutableFloatStateOf(0f) }


    var isEditingText by remember { mutableStateOf(false) }

    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(visibleState.currentState, visibleState.targetState) {
        if (!visibleState.targetState && !visibleState.currentState) {
            onDelete()
        }
    }

    // Removed unused richState state

    LaunchedEffect(isSelected) {
        if (!isSelected) {
            isEditingText = false
        }
    }

    val isEditorActive = (editingTextItemId == item.id)
    val effectiveItem = decryptedItem ?: item
    val currentRichState = if (isEditorActive && activeRichState != null) {
        activeRichState
    } else {
        remember(
            if (effectiveItem is GridItem.Text) effectiveItem.textContent else "",
            if (effectiveItem is GridItem.Text) effectiveItem.richSpans else emptyList(),
            if (effectiveItem is GridItem.Text) effectiveItem.fontSize else 16f,
            if (effectiveItem is GridItem.Text) effectiveItem.textAlign else TextAlignment.Start,
            if (effectiveItem is GridItem.Text) effectiveItem.lineHeight else 1.5f
        ) {
            if (effectiveItem is GridItem.Text) {
                richTextStateFromPersisted(
                    text = effectiveItem.textContent,
                    spans = effectiveItem.richSpans,
                    fontSize = effectiveItem.fontSize,
                    textAlign = effectiveItem.textAlign,
                    lineHeight = effectiveItem.lineHeight
                )
            } else {
                RichTextEditorState()
            }
        }
    }

    // Report editing state to parent whenever it changes
    LaunchedEffect(isEditingText) {
        if (effectiveItem is GridItem.Text || effectiveItem is GridItem.Checklist) {
            onEditingChanged(isEditingText && !isReadOnly, currentRichState)
        }
    }

    val preview = remember(
        item.x, item.y, item.width, item.height,
        dragOffsetX, dragOffsetY, resizeW, resizeH, resizeX, resizeY,
        cellSize, columns, rows
    ) {
        computePreview(item, cellSize, dragOffsetX, dragOffsetY, resizeW, resizeH, resizeX, resizeY, columns, rows)
    }

    val springSpec = spring<Float>(stiffness = Spring.StiffnessMediumLow)
    val animX by animateFloatAsState(preview.pixelX, springSpec)
    val animY by animateFloatAsState(preview.pixelY, springSpec)
    val animW by animateFloatAsState(preview.pixelW, springSpec)
    val animH by animateFloatAsState(preview.pixelH, springSpec)

    val currentPreview by rememberUpdatedState(preview)
    val currentOnMove by rememberUpdatedState(onMove)
    val currentOnResize by rememberUpdatedState(onResize)

    val animPadding by animateDpAsState(
        if (isSelected) GridItemDefaults.SelectedPadding else GridItemDefaults.DefaultPadding,
        animationSpec = MorgAnimation.gentle()
    )
    val animBorderWidth by animateDpAsState(
        if (isSelected) GridItemDefaults.SelectedBorderWidth else GridItemDefaults.UnselectedBorderWidth,
        MorgAnimation.gentle()
    )
    val animBorderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        MorgAnimation.gentle()
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(animX.roundToInt(), animY.roundToInt()) }
            .size(
                width = with(density) { animW.toDp() },
                height = with(density) { animH.toDp() }
            )
    ) {
        AnimatedVisibility(
            visibleState = visibleState,
            enter = scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
            ) + fadeIn(spring(stiffness = Spring.StiffnessMedium)),
            exit = scaleOut(
                targetScale = 0.8f,
                animationSpec = spring(stiffness = Spring.StiffnessMedium)
            ) + fadeOut(spring(stiffness = Spring.StiffnessMedium)),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(animPadding)
                .clip(GridItemDefaults.Shape)
                .border(animBorderWidth, animBorderColor, GridItemDefaults.Shape)
                .background(GridItemDefaults.backgroundColor())
                .then(
                    if (!isReadOnly) {
                        Modifier.combinedClickable(
                            onClick = { 
                                onClick()
                                val effectiveItem = decryptedItem ?: item
                                if (effectiveItem is GridItem.Text || effectiveItem is GridItem.Checklist) {
                                    isEditingText = true
                                }
                            },
                            onLongClick = { 
                                onClick()
                                isEditingText = false
                            }
                        )
                    } else Modifier
                )
                .pointerInput(isSelected, isReadOnly, isEditingText) {
                    if (isSelected && !isReadOnly && !isEditingText) {
                        detectDragGestures(
                            onDragStart = { onClick() },
                            onDragEnd = {
                                currentOnMove(currentPreview.gridX, currentPreview.gridY)
                                dragOffsetX = 0f
                                dragOffsetY = 0f
                            },
                            onDragCancel = { dragOffsetX = 0f; dragOffsetY = 0f }
                        ) { change, dragAmount ->
                            change.consume()
                            dragOffsetX += dragAmount.x
                            dragOffsetY += dragAmount.y
                        }
                    }
                }
        ) {
            val renderItem = decryptedItem ?: item
            val effectiveReadOnly = isReadOnly

            when (renderItem) {
                is GridItem.Text -> {
                    RichTextEditor(
                        state = currentRichState,
                        onStateChange = { newState ->
                            if (isEditorActive) {
                                onActiveRichStateChange(newState)
                            }
                            onTextChanged(newState.text)
                            onRichSpansChanged(newState.spans)
                            onTypographyChanged(newState.fontSize, newState.textAlign, newState.lineHeight)
                        },
                        onEnterPressed = {
                            val next = currentRichState.continueList()
                            if (isEditorActive) {
                                onActiveRichStateChange(next)
                            }
                            onTextChanged(next.text)
                            onRichSpansChanged(next.spans)
                        },
                        enabled = isEditingText && !effectiveReadOnly,
                        modifier = Modifier.fillMaxSize().padding(GridItemDefaults.InnerPadding)
                    )
                }
                is GridItem.Image -> {
                    AsyncImage(
                        model = renderItem.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                is GridItem.Checklist -> {
                    ChecklistContent(
                        item = renderItem,
                        isSelected = isSelected,
                        isReadOnly = effectiveReadOnly,
                        onTitleChanged = onChecklistTitleChanged,
                        onCheckboxToggled = onCheckboxToggled,
                        onCheckboxTextChanged = onCheckboxTextChanged,
                        onCheckboxAdded = onCheckboxAdded,
                        onCheckboxDeleted = onCheckboxDeleted,
                        modifier = Modifier.fillMaxSize().padding(GridItemDefaults.InnerPadding)
                    )
                }
                is GridItem.SecretItem -> {
                    SecretItemContent(
                        isBiometricDisabled = renderItem.isBiometricDisabled,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { onSecretItemUnlockRequested(item.id) }
                    )
                }
            }


        }

                AnimatedVisibility(
                    visible = isSelected && !isReadOnly && !isEditingText,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
                    exit = fadeOut(spring(stiffness = Spring.StiffnessMediumLow)),
                    modifier = Modifier.matchParentSize()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        EdgeResizeHandles(
                            onRightDrag = { resizeW += it },
                            onBottomDrag = { resizeH += it },
                            onLeftDrag = { resizeX += it },
                            onTopDrag = { resizeY += it },
                            onResizeEnd = {
                                currentOnResize(
                                    currentPreview.gridW, currentPreview.gridH,
                                    currentPreview.gridX, currentPreview.gridY
                                )
                                resizeW = 0f; resizeH = 0f; resizeX = 0f; resizeY = 0f
                            }
                        )
                    }
                }

                // Delete button — top-right corner, visible in move/resize mode
                AnimatedVisibility(
                    visible = isSelected && !isReadOnly && !isEditingText,
                    enter = scaleIn(
                        initialScale = 0.6f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium)
                    ) + fadeIn(spring(stiffness = Spring.StiffnessMedium)),
                    exit = scaleOut(
                        targetScale = 0.6f,
                        animationSpec = spring(stiffness = Spring.StiffnessMedium)
                    ) + fadeOut(spring(stiffness = Spring.StiffnessMedium)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .zIndex(10f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = MaterialTheme.colorScheme.error,
                                shape = CircleShape
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                visibleState.targetState = false
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Delete item",
                            tint = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class ItemPreview(
    val gridX: Int, val gridY: Int,
    val gridW: Int, val gridH: Int,
    val pixelX: Float, val pixelY: Float,
    val pixelW: Float, val pixelH: Float
)

private fun computePreview(
    item: GridItem,
    cellSize: Float,
    dragX: Float, dragY: Float,
    resW: Float, resH: Float,
    resX: Float, resY: Float,
    columns: Int, rows: Int
): ItemPreview {
    val snapDragX = (dragX / cellSize).roundToInt()
    val snapDragY = (dragY / cellSize).roundToInt()
    val snapResW = (resW / cellSize).roundToInt()
    val snapResH = (resH / cellSize).roundToInt()
    val snapResX = (resX / cellSize).roundToInt()
    val snapResY = (resY / cellSize).roundToInt()

    var w = item.width + snapResW - snapResX
    var h = item.height + snapResH - snapResY
    var x = item.x + snapDragX + snapResX
    var y = item.y + snapDragY + snapResY

    if (w < 1) { w = 1; x = item.x + snapDragX + item.width - 1 }
    if (h < 1) { h = 1; y = item.y + snapDragY + item.height - 1 }
    x = x.coerceAtLeast(0)
    y = y.coerceAtLeast(0)
    if (x + w > columns) w = columns - x
    if (y + h > rows) h = rows - y

    return ItemPreview(
        gridX = x, gridY = y, gridW = w, gridH = h,
        pixelX = x * cellSize, pixelY = y * cellSize,
        pixelW = w * cellSize, pixelH = h * cellSize
    )
}

@Composable
private fun BoxScope.EdgeResizeHandles(
    onRightDrag: (Float) -> Unit,
    onBottomDrag: (Float) -> Unit,
    onLeftDrag: (Float) -> Unit,
    onTopDrag: (Float) -> Unit,
    onResizeEnd: () -> Unit
) {
    val handleColor = MaterialTheme.colorScheme.primary
    val hitArea = 10.dp
    val visibleSize = 4.dp

    val curRight by rememberUpdatedState(onRightDrag)
    val curBottom by rememberUpdatedState(onBottomDrag)
    val curLeft by rememberUpdatedState(onLeftDrag)
    val curTop by rememberUpdatedState(onTopDrag)
    val curEnd by rememberUpdatedState(onResizeEnd)

    // Left
    Box(
        modifier = Modifier
            .align(Alignment.CenterStart)
            .width(hitArea).fillMaxHeight()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { curEnd() },
                    onDragCancel = { curEnd() }
                ) { change, drag -> change.consume(); curLeft(drag.x) }
            }
    ) {
        Box(Modifier.align(Alignment.Center).width(visibleSize).height(40.dp)
            .background(handleColor, RoundedCornerShape(2.dp)))
    }

    Box(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .width(hitArea).fillMaxHeight()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { curEnd() },
                    onDragCancel = { curEnd() }
                ) { change, drag -> change.consume(); curRight(drag.x) }
            }
    ) {
        Box(Modifier.align(Alignment.Center).width(visibleSize).height(40.dp)
            .background(handleColor, RoundedCornerShape(2.dp)))
    }

    Box(
        modifier = Modifier
            .align(Alignment.TopCenter)
            .height(hitArea).fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { curEnd() },
                    onDragCancel = { curEnd() }
                ) { change, drag -> change.consume(); curTop(drag.y) }
            }
    ) {
        Box(Modifier.align(Alignment.Center).height(visibleSize).width(40.dp)
            .background(handleColor, RoundedCornerShape(2.dp)))
    }

    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .height(hitArea).fillMaxWidth()
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = { curEnd() },
                    onDragCancel = { curEnd() }
                ) { change, drag -> change.consume(); curBottom(drag.y) }
            }
    ) {
        Box(Modifier.align(Alignment.Center).height(visibleSize).width(40.dp)
            .background(handleColor, RoundedCornerShape(2.dp)))
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Checklist content composable
// ────────────────────────────────────────────────────────────────────────────

@Composable
private fun ChecklistContent(
    item: GridItem.Checklist,
    isSelected: Boolean,
    isReadOnly: Boolean,
    onTitleChanged: (String) -> Unit,
    onCheckboxToggled: (entryId: String) -> Unit,
    onCheckboxTextChanged: (entryId: String, text: String) -> Unit,
    onCheckboxAdded: () -> Unit,
    onCheckboxDeleted: (entryId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        BasicTextField(
            value = item.title,
            onValueChange = onTitleChanged,
            textStyle = MaterialTheme.typography.titleSmall.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            enabled = isSelected && !isReadOnly,
            decorationBox = { innerTextField ->
                Box {
                    if (item.title.isEmpty()) {
                        Text(
                            "Checklist title",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(4.dp))
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            items(item.entries, key = { it.id }) { entry ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = entry.isChecked,
                        onCheckedChange = { onCheckboxToggled(entry.id) },
                        enabled = isSelected && !isReadOnly,
                        modifier = Modifier.size(32.dp)
                    )

                    BasicTextField(
                        value = entry.text,
                        onValueChange = { onCheckboxTextChanged(entry.id, it) },
                        textStyle = TextStyle(
                            fontSize = 13.sp,
                            color = if (entry.isChecked)
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            else
                                MaterialTheme.colorScheme.onSurface,
                            textDecoration = if (entry.isChecked) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        enabled = isSelected && !isReadOnly,
                        decorationBox = { innerTextField ->
                            Box(modifier = Modifier.weight(1f).padding(vertical = 2.dp)) {
                                if (entry.text.isEmpty()) {
                                    Text(
                                        "Checkbox item",
                                        style = TextStyle(fontSize = 13.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                    )
                                }
                                innerTextField()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )

                    if (isSelected && !isReadOnly) {
                        IconButton(
                            onClick = { onCheckboxDeleted(entry.id) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Delete checkbox",
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }

            if (isSelected && !isReadOnly) {
                item(key = "add_checkbox_${item.id}") {
                    TextButton(
                        onClick = onCheckboxAdded,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "Add Checkbox",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}