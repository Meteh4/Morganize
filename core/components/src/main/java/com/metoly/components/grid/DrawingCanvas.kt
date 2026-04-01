// DrawingCanvas.kt
package com.metoly.components.grid

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.metoly.morganize.core.model.grid.DrawingPoint
import com.metoly.morganize.core.model.grid.DrawingStroke

/**
 * Full-page freehand drawing canvas that renders persisted [strokes] and captures
 * new ones via touch/pointer gestures.
 *
 * Drawing coordinates are stored as fractions [0f..1f] relative to the canvas
 * dimensions so the drawing scales correctly across all screen sizes and densities.
 *
 * Single Responsibility: This composable is solely responsible for:
 *  1. Rendering existing drawing strokes.
 *  2. Capturing new strokes and forwarding them to the caller.
 *  3. Forwarding eraser events to the caller.
 *
 * @param strokes          The list of strokes to render (from persisted state).
 * @param isActive         Whether the drawing mode is currently active (touch input enabled).
 * @param isEraserMode     If true, the current tool erases; if false, it draws.
 * @param penColorArgb     The current pen color as an ARGB Long.
 * @param strokeWidthFraction The current stroke width as a fraction of canvas width.
 * @param eraserWidthFraction The current eraser width as a fraction of canvas width.
 * @param onStrokeFinished Called when the user lifts the finger, producing a new stroke.
 * @param onErase          Called with the pointer offset (fraction) when erasing;
 *                         the caller is responsible for removing/trimming strokes.
 * @param modifier         Standard Compose modifier.
 */
@Composable
fun DrawingCanvas(
    strokes: List<DrawingStroke>,
    isActive: Boolean,
    isEraserMode: Boolean,
    penColorArgb: Long,
    strokeWidthFraction: Float,
    eraserWidthFraction: Float,
    onStrokeFinished: (DrawingStroke) -> Unit,
    onStrokesChanged: (List<DrawingStroke>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    // Points of the stroke currently being drawn (not yet committed)
    var currentPoints by remember { mutableStateOf<List<DrawingPoint>>(emptyList()) }
    
    // Strokes currently being erased (held locally during gesture)
    var localErasedStrokes by remember { mutableStateOf<List<DrawingStroke>?>(null) }
    
    val currentStrokes by rememberUpdatedState(strokes)
    
    val displayStrokes = localErasedStrokes ?: strokes

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { canvasSize = it }
            .then(
                if (isActive) {
                    Modifier.pointerInput(isEraserMode, penColorArgb, strokeWidthFraction, eraserWidthFraction) {
                        awaitEachGesture {
                            if (canvasSize == IntSize.Zero) return@awaitEachGesture
                            val down: PointerInputChange = awaitFirstDown(requireUnconsumed = false)
                            down.consume()

                            val w = canvasSize.width.toFloat()
                            val h = canvasSize.height.toFloat()

                            if (isEraserMode) {
                                val radius = eraserWidthFraction / 2f
                                var currentSegments = currentStrokes
                                currentSegments = applyEraser(currentSegments, down.position.x / w, down.position.y / h, radius)
                                localErasedStrokes = currentSegments

                                var change = down
                                while (true) {
                                    change = awaitPointerEvent().changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    change.consume()
                                    
                                    // Native fast-path eraser execution locally!
                                    currentSegments = applyEraser(currentSegments, change.position.x / w, change.position.y / h, radius)
                                    localErasedStrokes = currentSegments
                                }
                                
                                // Gesture ended, commit erasure to parent
                                onStrokesChanged(currentSegments)
                                localErasedStrokes = null
                            } else {
                                // Drawing: accumulate points into a stroke
                                val points = mutableListOf(
                                    DrawingPoint(down.position.x / w, down.position.y / h)
                                )
                                var change = down
                                while (true) {
                                    change = awaitPointerEvent().changes.firstOrNull() ?: break
                                    if (!change.pressed) break
                                    change.consume()
                                    points += DrawingPoint(change.position.x / w, change.position.y / h)
                                    currentPoints = points.toList()
                                }
                                if (points.size >= 2) {
                                    onStrokeFinished(
                                        DrawingStroke(
                                            colorArgb = penColorArgb,
                                            strokeWidthFraction = strokeWidthFraction,
                                            points = points,
                                            isEraser = false
                                        )
                                    )
                                }
                                currentPoints = emptyList()
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val w = size.width
        val h = size.height

        // Render persisted/live strokes
        for (stroke in displayStrokes) {
            if (stroke.points.size < 2) continue
            drawStrokePath(stroke, w, h)
        }

        // Render the live stroke being drawn
        if (currentPoints.size >= 2) {
            val liveStroke = DrawingStroke(
                colorArgb = penColorArgb,
                strokeWidthFraction = strokeWidthFraction,
                points = currentPoints
            )
            drawStrokePath(liveStroke, w, h)
        }
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Private drawing helpers
// ────────────────────────────────────────────────────────────────────────────

private fun DrawScope.drawStrokePath(stroke: DrawingStroke, canvasW: Float, canvasH: Float) {
    if (stroke.points.size < 2) return

    val path = Path().apply {
        val first = stroke.points.first()
        moveTo(first.xFraction * canvasW, first.yFraction * canvasH)
        for (i in 1 until stroke.points.size) {
            val prev = stroke.points[i - 1]
            val curr = stroke.points[i]
            // Smooth curve via quadratic bezier midpoints
            val midX = ((prev.xFraction + curr.xFraction) / 2f) * canvasW
            val midY = ((prev.yFraction + curr.yFraction) / 2f) * canvasH
            quadraticTo(
                prev.xFraction * canvasW, prev.yFraction * canvasH,
                midX, midY
            )
        }
        val last = stroke.points.last()
        lineTo(last.xFraction * canvasW, last.yFraction * canvasH)
    }

    val strokeWidthPx = stroke.strokeWidthFraction * canvasW

    if (stroke.isEraser) {
        drawPath(
            path = path,
            color = Color.White,
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            ),
            blendMode = BlendMode.Clear
        )
    } else {
        drawPath(
            path = path,
            color = Color(stroke.colorArgb.toULong().toLong()),
            style = Stroke(
                width = strokeWidthPx,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

// ────────────────────────────────────────────────────────────────────────────
// Utility: parse / serialize strokes from JSON
// ────────────────────────────────────────────────────────────────────────────

fun parseDrawingStrokes(json: String): List<DrawingStroke> =
    if (json.isBlank()) emptyList()
    else runCatching {
        kotlinx.serialization.json.Json.decodeFromString<List<DrawingStroke>>(json)
    }.getOrDefault(emptyList())

fun serializeDrawingStrokes(strokes: List<DrawingStroke>): String =
    if (strokes.isEmpty()) ""
    else kotlinx.serialization.json.Json.encodeToString(strokes)

// ────────────────────────────────────────────────────────────────────────────
// Eraser logic helper: remove strokes that overlap an eraser position
// Returns (clearedStrokes, updatedStroke?) where updatedStroke is a trimmed stroke
// if only part of the stroke was erased.
// ────────────────────────────────────────────────────────────────────────────

/**
 * Applies a circular eraser at position ([xFrac], [yFrac]) with radius [eraserRadiusFrac]
 * (all as canvas fractions) against [strokes].
 *
 * Strategy: any stroke that has at least one point inside the eraser circle is split
 * at that point. This produces a natural-feeling eraser without removing full strokes.
 */
fun applyEraser(
    strokes: List<DrawingStroke>,
    xFrac: Float,
    yFrac: Float,
    eraserRadiusFrac: Float
): List<DrawingStroke> {
    val result = mutableListOf<DrawingStroke>()
    for (stroke in strokes) {
        val segments = splitStrokeAtEraser(stroke, xFrac, yFrac, eraserRadiusFrac)
        result.addAll(segments)
    }
    return result
}

private fun splitStrokeAtEraser(
    stroke: DrawingStroke,
    cx: Float, cy: Float,
    radius: Float
): List<DrawingStroke> {
    if (stroke.points.isEmpty()) return emptyList()

    val segments = mutableListOf<DrawingStroke>()
    val currentSegment = mutableListOf<DrawingPoint>()
    val r2 = radius * radius

    // Estimate A4 aspect ratio (height / width) so eraser hit-box isn't a squished ellipse
    val aspect = 1.414f 

    if (stroke.points.size == 1) {
        val pt = stroke.points[0]
        val dx = pt.xFraction - cx
        val dy = (pt.yFraction - cy) * aspect
        if (dx * dx + dy * dy <= r2) return emptyList()
        return listOf(stroke)
    }

    currentSegment.add(stroke.points[0])
    for (i in 0 until stroke.points.size - 1) {
        val p1 = stroke.points[i]
        val p2 = stroke.points[i + 1]

        val d2 = distToSegmentSquared(
            p1.xFraction, p1.yFraction,
            p2.xFraction, p2.yFraction,
            cx, cy, aspect
        )

        if (d2 <= r2) {
            // Segment intersects the eraser. Finish the current segment if it has enough points.
            if (currentSegment.size >= 2) {
                segments.add(stroke.copy(points = currentSegment.toList()))
            }
            currentSegment.clear()
        } else {
            // Segment does NOT intersect.
            if (currentSegment.isEmpty()) {
                currentSegment.add(p1) // start new valid segment
            }
            currentSegment.add(p2)
        }
    }

    if (currentSegment.size >= 2) {
        segments.add(stroke.copy(points = currentSegment.toList()))
    }
    return segments
}

private fun distToSegmentSquared(
    px: Float, py: Float,
    wx: Float, wy: Float,
    cx: Float, cy: Float,
    aspect: Float
): Float {
    val dxW = wx - px
    val dyW = (wy - py) * aspect
    val l2 = dxW * dxW + dyW * dyW
    
    val dxC = cx - px
    val dyC = (cy - py) * aspect

    if (l2 == 0f) return dxC * dxC + dyC * dyC

    var t = (dxC * dxW + dyC * dyW) / l2
    t = java.lang.Float.max(0f, java.lang.Float.min(1f, t))

    val projX = px + t * (wx - px)
    val projY = py + t * (wy - py)

    val dPx = cx - projX
    val dPy = (cy - projY) * aspect

    return dPx * dPx + dPy * dPy
}
