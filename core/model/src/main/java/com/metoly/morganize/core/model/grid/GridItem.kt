package com.metoly.morganize.core.model.grid

import kotlinx.serialization.Serializable

/**
 * Represents a draggable, resizable wrapper over standard note components.
 */
@Serializable
sealed class GridItem {
    abstract val id: String
    abstract val x: Int
    abstract val y: Int
    abstract val width: Int
    abstract val height: Int

    @Serializable
    data class Text(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val textContent: String,
        val richSpansJson: String = "",
        val fontSize: Float = 14f,        // sp
        val textAlign: String = "Start",  // "Start" | "Center" | "End"
        val lineHeight: Float = 1.4f      // multiplier
    ) : GridItem()

    @Serializable
    data class Image(
        override val id: String,
        override val x: Int,
        override val y: Int,
        override val width: Int,
        override val height: Int,
        val imageUri: String
    ) : GridItem()
}
