package com.metoly.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

private data class DrawStroke(val points: List<Offset>, val color: Color, val width: Float)

/**
 * An interactive drawing canvas using Compose's [Canvas] and touch tracking.
 *
 * The caller is responsible for persisting the bitmap via [onSave].
 *
 * @param modifier layout modifier
 * @param onSave called with the resulting [Bitmap] when the user is ready to save
 */
@Composable
fun DrawingCanvas(modifier: Modifier = Modifier, onSave: (Bitmap) -> Unit = {}) {
    val strokes = remember { mutableListOf<DrawStroke>() }
    var currentPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var strokesVersion by remember { mutableStateOf(0) } // triggers recomposition
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    val strokeColor = MaterialTheme.colorScheme.onSurface
    val strokeWidth = 6f

    Column(modifier = modifier) {
        // Toolbar
        Surface(color = MaterialTheme.colorScheme.surfaceContainerHigh, tonalElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Çizim",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Row {
                    IconButton(
                        onClick = {
                            if (strokes.isNotEmpty()) {
                                strokes.removeAt(strokes.lastIndex)
                                strokesVersion++
                            }
                        }
                    ) { Icon(Icons.Default.Undo, contentDescription = "Geri al") }
                    IconButton(
                        onClick = {
                            strokes.clear()
                            strokesVersion++
                        }
                    ) { Icon(Icons.Default.Delete, contentDescription = "Temizle") }
                    IconButton(
                        onClick = {
                            if (canvasSize.width > 0 && canvasSize.height > 0) {
                                val bitmap =
                                    renderStrokesToBitmap(
                                        widthPx = canvasSize.width,
                                        heightPx = canvasSize.height,
                                        strokes =
                                            strokes.map {
                                                it.points to it.color.toArgb()
                                            },
                                        strokeWidthPx = strokeWidth
                                    )
                                onSave(bitmap)
                            }
                        }
                    ) { Icon(Icons.Default.Check, contentDescription = "Kaydet") }
                }
            }
        }

        // Drawing area
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(MaterialTheme.colorScheme.surface)
                    .clipToBounds()
                    .onSizeChanged { canvasSize = it }
        ) {
            Canvas(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { offset -> currentPoints = listOf(offset) },
                                onDrag = { change, _ ->
                                    currentPoints = currentPoints + change.position
                                },
                                onDragEnd = {
                                    if (currentPoints.size > 1) {
                                        strokes.add(
                                            DrawStroke(
                                                points = currentPoints,
                                                color = strokeColor,
                                                width = strokeWidth
                                            )
                                        )
                                        strokesVersion++
                                    }
                                    currentPoints = emptyList()
                                }
                            )
                        }
            ) {
                // Suppress unused variable warning — strokesVersion read to trigger recomposition
                @Suppress("UNUSED_EXPRESSION") strokesVersion

                fun drawStroke(stroke: DrawStroke) {
                    if (stroke.points.size < 2) return
                    val path =
                        Path().apply {
                            moveTo(stroke.points[0].x, stroke.points[0].y)
                            stroke.points.drop(1).forEach { lineTo(it.x, it.y) }
                        }
                    drawPath(
                        path = path,
                        color = stroke.color,
                        style =
                            Stroke(
                                width = stroke.width,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                    )
                }

                strokes.forEach { drawStroke(it) }

                // Live stroke
                if (currentPoints.size >= 2) {
                    drawStroke(DrawStroke(currentPoints, strokeColor, strokeWidth))
                }
            }
        }
    }
}

/** Render all strokes into a [Bitmap]. Call on a background coroutine if the canvas is large. */
fun renderStrokesToBitmap(
    widthPx: Int,
    heightPx: Int,
    strokes: List<Pair<List<Offset>, Int>>, // points + ARGB color
    strokeWidthPx: Float = 6f
): Bitmap {
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = strokeWidthPx
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }
    for ((points, color) in strokes) {
        paint.color = color
        if (points.size < 2) continue
        val path =
            android.graphics.Path().apply {
                moveTo(points[0].x, points[0].y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
        canvas.drawPath(path, paint)
    }
    return bitmap
}
