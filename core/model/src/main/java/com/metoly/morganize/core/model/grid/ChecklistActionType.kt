package com.metoly.morganize.core.model.grid

/**
 * Describes a single mutation on a [GridItem.Checklist].
 * Shared by both Create and Edit features so each only needs one event
 * (`ChecklistAction`) instead of five separate event classes.
 */
sealed interface ChecklistActionType {
    data class TitleChanged(val title: String) : ChecklistActionType
    data class EntryToggled(val entryId: String) : ChecklistActionType
    data class EntryTextChanged(val entryId: String, val text: String) : ChecklistActionType
    data object EntryAdded : ChecklistActionType
    data class EntryDeleted(val entryId: String) : ChecklistActionType
}
