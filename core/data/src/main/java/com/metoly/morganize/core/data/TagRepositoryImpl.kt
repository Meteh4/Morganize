package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.TagDao
import com.metoly.morganize.core.database.entity.NoteTagCrossRef
import com.metoly.morganize.core.database.mapper.TagMapper
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.Tag
import com.metoly.morganize.core.model.asResponseState
import com.metoly.morganize.core.model.suspendAsResponseStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TagRepositoryImpl(
    private val tagDao: TagDao,
    private val tagMapper: TagMapper
) : TagRepository {

    override fun getAllTags(): Flow<ResponseState<List<Tag>>> =
        tagDao.getAllTags()
            .map { entities -> tagMapper.toDomainList(entities) }
            .asResponseState()

    override fun getTagById(id: Long): Flow<ResponseState<Tag?>> =
        tagDao.getTagById(id)
            .map { entity -> entity?.let { tagMapper.toDomain(it) } }
            .asResponseState()

    override fun insertTag(tag: Tag): Flow<ResponseState<Long>> = suspendAsResponseStateFlow {
        tagDao.insertTag(tagMapper.toEntity(tag))
    }

    override fun updateTag(tag: Tag): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        tagDao.updateTag(tagMapper.toEntity(tag))
    }

    override fun deleteTag(tag: Tag): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        tagDao.deleteTag(tagMapper.toEntity(tag))
    }

    override fun deleteTagById(id: Long): Flow<ResponseState<Unit>> = suspendAsResponseStateFlow {
        tagDao.deleteTagById(id)
    }

    override fun getTagIdsForNote(noteId: Long): Flow<ResponseState<List<Long>>> =
        tagDao.getTagIdsForNote(noteId).asResponseState()

    override fun getTagsForNote(noteId: Long): Flow<ResponseState<List<Tag>>> =
        tagDao.getTagsForNote(noteId)
            .map { entities -> tagMapper.toDomainList(entities) }
            .asResponseState()

    override fun addTagToNote(noteId: Long, tagId: Long): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { tagDao.addTagToNote(NoteTagCrossRef(noteId, tagId)) }

    override fun removeTagFromNote(noteId: Long, tagId: Long): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { tagDao.removeTagFromNote(noteId, tagId) }

    override fun removeAllTagsFromNote(noteId: Long): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow { tagDao.removeAllTagsFromNote(noteId) }
}
