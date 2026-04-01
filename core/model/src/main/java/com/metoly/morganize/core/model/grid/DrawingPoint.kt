package com.metoly.morganize.core.model.grid

import kotlinx.serialization.Serializable

/**
 * A single 2D point within a drawing stroke, stored as fractional coordinates
 * (0f..1f) relative to the canvas dimensions so the drawing scales correctly
 * across different screen densities and sizes.
 */
@Serializable
data class DrawingPoint(
    val xFraction: Float,
    val yFraction: Float
)
