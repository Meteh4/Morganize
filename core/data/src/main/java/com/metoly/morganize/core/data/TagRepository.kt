package com.metoly.morganize.core.data

import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for abstracting Tag persistent storage layer.
 */
interface TagRepository {
    fun getAllTags(): Flow<ResponseState<List<Tag>>>
    fun getTagById(id: Long): Flow<ResponseState<Tag?>>
    fun insertTag(tag: Tag): Flow<ResponseState<Long>>
    fun updateTag(tag: Tag): Flow<ResponseState<Unit>>
    fun deleteTag(tag: Tag): Flow<ResponseState<Unit>>
    fun deleteTagById(id: Long): Flow<ResponseState<Unit>>

    // Cross-ref ops
    fun getTagIdsForNote(noteId: Long): Flow<ResponseState<List<Long>>>
    fun getTagsForNote(noteId: Long): Flow<ResponseState<List<Tag>>>
    fun addTagToNote(noteId: Long, tagId: Long): Flow<ResponseState<Unit>>
    fun removeTagFromNote(noteId: Long, tagId: Long): Flow<ResponseState<Unit>>
    fun removeAllTagsFromNote(noteId: Long): Flow<ResponseState<Unit>>
}
