package com.metoly.morganize.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.metoly.morganize.core.database.entity.NoteTagCrossRef
import com.metoly.morganize.core.database.entity.TagEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for [TagEntity] and [NoteTagCrossRef] operations.
 * Supports CRUD on tags and managing the many-to-many note↔tag relationship.
 */
@Dao
interface TagDao {

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getTagById(id: Long): Flow<TagEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTagById(id: Long)

    // ── Cross-ref operations ────────────────────────────────────────────

    /** Get all tag IDs associated with a note. */
    @Query("SELECT tagId FROM note_tag_cross_ref WHERE noteId = :noteId")
    fun getTagIdsForNote(noteId: Long): Flow<List<Long>>

    /** Get all tags associated with a note. */
    @Query("SELECT t.* FROM tags t INNER JOIN note_tag_cross_ref ref ON t.id = ref.tagId WHERE ref.noteId = :noteId ORDER BY t.name ASC")
    fun getTagsForNote(noteId: Long): Flow<List<TagEntity>>

    /** Add a tag to a note. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addTagToNote(crossRef: NoteTagCrossRef)

    /** Remove a tag from a note. */
    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId AND tagId = :tagId")
    suspend fun removeTagFromNote(noteId: Long, tagId: Long)

    /** Remove all tags from a note. */
    @Query("DELETE FROM note_tag_cross_ref WHERE noteId = :noteId")
    suspend fun removeAllTagsFromNote(noteId: Long)
}
