package com.metoly.morganize.core.model

import kotlinx.serialization.Serializable

/**
 * A single item within a note's checklist. Serialized to/from JSON and stored in
 * [Note.checklistJson].
 */
@Serializable
data class ChecklistItem(
        val id: String = java.util.UUID.randomUUID().toString(),
        val text: String,
        val isChecked: Boolean = false
)
