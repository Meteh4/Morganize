// GridCanvas.kt
package com.metoly.components.grid

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.metoly.components.RichTextEditor
import com.metoly.components.RichTextEditorState
import com.metoly.components.continueList
import com.metoly.components.richTextStateFromPersisted
import com.metoly.morganize.core.model.RichSpan
import com.metoly.morganize.core.model.grid.GridItem
import com.metoly.morganize.core.model.grid.NotePage
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

private val json = Json { ignoreUnknownKeys = true }

private fun parseRichSpans(richSpansJson: String): List<RichSpan> =
    if (richSpansJson.isBlank()) emptyList()
    else runCatching { json.decodeFromString<List<RichSpan>>(richSpansJson) }.getOrDefault(emptyList())

private fun serializeRichSpans(spans: List<RichSpan>): String =
    if (spans.isEmpty()) "" else json.encodeToString(spans)

@Composable
fun GridCanvas(
    page: NotePage,
    selectedItemId: String?,
    onItemSelected: (String?) -> Unit,
    onItemMoved: (itemId: String, newX: Int, newY: Int) -> Unit,
    onItemResized: (itemId: String, newWidth: Int, newHeight: Int, newX: Int, newY: Int) -> Unit,
    onItemTextChanged: (itemId: String, newText: String) -> Unit,
    onItemRichSpansChanged: (itemId: String, richSpansJson: String) -> Unit,
    onItemTypographyChanged: (itemId: String, fontSize: Float, textAlign: String, lineHeight: Float) -> Unit,
    onItemDeleted: (itemId: String) -> Unit,
    onEditingTextItemChanged: (itemId: String?, richState: RichTextEditorState?) -> Unit,
    editingTextItemId: String? = null,
    activeRichState: RichTextEditorState? = null,
    onActiveRichStateChange: (RichTextEditorState) -> Unit = {},
    modifier: Modifier = Modifier,
    columns: Int = 10,
    rows: Int = 20,
    isReadOnly: Boolean = false
) {
    var canvasWidth by remember { mutableFloatStateOf(0f) }
    val cellSize = if (columns > 0) canvasWidth / columns else 0f
    val density = LocalDensity.current

    Box(
        modifier = modifier
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
                    onRichSpansChanged = { spansJson -> onItemRichSpansChanged(item.id, spansJson) },
                    onTypographyChanged = { fs, ta, lh -> onItemTypographyChanged(item.id, fs, ta, lh) },
                    onDelete = { onItemDeleted(item.id) },
                    onEditingChanged = { isEditing, richState ->
                        onEditingTextItemChanged(
                            if (isEditing) item.id else null,
                            if (isEditing) richState else null
                        )
                    },
                    editingTextItemId = editingTextItemId,
                    activeRichState = activeRichState,
                    onActiveRichStateChange = onActiveRichStateChange
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
    onRichSpansChanged: (String) -> Unit,
    onTypographyChanged: (Float, String, Float) -> Unit,
    onDelete: () -> Unit,
    onEditingChanged: (Boolean, RichTextEditorState) -> Unit,
    editingTextItemId: String?,
    activeRichState: RichTextEditorState?,
    onActiveRichStateChange: (RichTextEditorState) -> Unit,
) {
    val density = LocalDensity.current

    var dragOffsetX by remember(item.x) { mutableFloatStateOf(0f) }
    var dragOffsetY by remember(item.y) { mutableFloatStateOf(0f) }

    var resizeW by remember(item.width) { mutableFloatStateOf(0f) }
    var resizeH by remember(item.height) { mutableFloatStateOf(0f) }
    var resizeX by remember(item.x) { mutableFloatStateOf(0f) }
    var resizeY by remember(item.y) { mutableFloatStateOf(0f) }

    var showDropdown by remember { mutableStateOf(false) }
    var isEditingText by remember { mutableStateOf(false) }

    // Rich text state for text items
    var richState by remember(item.id) {
        mutableStateOf(
            if (item is GridItem.Text) {
                richTextStateFromPersisted(
                    text = item.textContent,
                    spans = parseRichSpans(item.richSpansJson),
                    fontSize = item.fontSize,
                    textAlign = item.textAlign,
                    lineHeight = item.lineHeight
                )
            } else {
                RichTextEditorState()
            }
        )
    }

    // Sync rich state when item text changes externally (e.g., undo/redo)
    LaunchedEffect(item) {
        if (item is GridItem.Text && item.textContent != richState.text) {
            richState = richTextStateFromPersisted(
                text = item.textContent,
                spans = parseRichSpans(item.richSpansJson),
                fontSize = item.fontSize,
                textAlign = item.textAlign,
                lineHeight = item.lineHeight
            )
        }
    }

    LaunchedEffect(isSelected) {
        if (!isSelected) {
            isEditingText = false
            showDropdown = false
        }
    }

    // Report editing state to parent whenever it changes
    LaunchedEffect(isEditingText, richState) {
        if (item is GridItem.Text) {
            onEditingChanged(isEditingText && !isReadOnly, richState)
        }
    }

    val isEditorActive = (editingTextItemId == item.id)
    val currentRichState = if (isEditorActive && activeRichState != null) activeRichState else richState

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
        if (isSelected) 10.dp else 4.dp,
        spring(stiffness = Spring.StiffnessMediumLow)
    )
    val animBorderWidth by animateDpAsState(
        if (isSelected) 2.dp else 0.dp,
        spring(stiffness = Spring.StiffnessMediumLow)
    )
    val animBorderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        spring(stiffness = Spring.StiffnessMediumLow)
    )

    Box(
        modifier = Modifier
            .offset { IntOffset(animX.roundToInt(), animY.roundToInt()) }
            .size(
                width = with(density) { animW.toDp() },
                height = with(density) { animH.toDp() }
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(animPadding)
                .clip(RoundedCornerShape(8.dp))
                .border(animBorderWidth, animBorderColor, RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .then(
                    if (!isReadOnly) {
                        Modifier.combinedClickable(
                            onClick = onClick,
                            onLongClick = { onClick(); showDropdown = true }
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
            when (item) {
                is GridItem.Text -> {
                    RichTextEditor(
                        state = currentRichState,
                        onStateChange = { newState ->
                            if (isEditorActive) {
                                onActiveRichStateChange(newState)
                            } else {
                                richState = newState
                            }
                            // Persist changes implicitly on state change (throttled by user typing)
                            onTextChanged(newState.text)
                            onRichSpansChanged(serializeRichSpans(newState.spans))
                            onTypographyChanged(newState.fontSize, newState.textAlign, newState.lineHeight)
                        },
                        onEnterPressed = {
                            val next = currentRichState.continueList()
                            if (isEditorActive) {
                                onActiveRichStateChange(next)
                            } else {
                                richState = next
                            }
                            onTextChanged(next.text)
                            onRichSpansChanged(serializeRichSpans(next.spans))
                        },
                        enabled = isEditingText && !isReadOnly,
                        modifier = Modifier.fillMaxSize().padding(8.dp)
                    )
                }
                is GridItem.Image -> {
                    AsyncImage(
                        model = item.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            // Context menu
            DropdownMenu(
                expanded = showDropdown,
                onDismissRequest = { showDropdown = false },
                shape = RoundedCornerShape(24.dp)
            ) {
                if (item is GridItem.Text) {
                    DropdownMenuItem(
                        text = { Text(if (isEditingText) "Done Editing" else "Edit Text") },
                        onClick = { isEditingText = !isEditingText; showDropdown = false },
                        leadingIcon = {
                            Icon(
                                if (isEditingText) Icons.Default.Check else Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    )
                }
                DropdownMenuItem(
                    text = { Text("Delete") },
                    onClick = { showDropdown = false; onDelete() },
                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) }
                )
            }
        }

        // Resize handles (only when selected, not editing)
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

    // Right
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

    // Top
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

    // Bottom
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