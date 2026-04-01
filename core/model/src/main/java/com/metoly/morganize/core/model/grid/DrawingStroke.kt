package com.metoly.morganize.core.model.grid

import kotlinx.serialization.Serializable

/**
 * Represents a single continuous pen stroke on a page's drawing canvas.
 *
 * @param colorArgb  ARGB color packed as a Long (e.g. 0xFF000000 = opaque black).
 * @param strokeWidthFraction  Stroke width as a fraction of the canvas width so it
 *                             scales properly across screen sizes (typically 0.002 – 0.05).
 * @param points  Ordered list of fractional canvas points that form this stroke.
 * @param isEraser  When true this stroke is rendered as an eraser (clears underlying pixels).
 */
@Serializable
data class DrawingStroke(
    val colorArgb: Long = 0xFF000000L,
    val strokeWidthFraction: Float = 0.008f,
    val points: List<DrawingPoint> = emptyList(),
    val isEraser: Boolean = false
)
