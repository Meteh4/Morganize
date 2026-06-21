package com.metoly.morganize.core.database.mapper

import com.metoly.morganize.core.database.entity.TagEntity
import com.metoly.morganize.core.model.Tag

/**
 * Maps between TagEntity (persistence layer) and Tag (domain layer).
 */
class TagMapper {
    fun toDomain(entity: TagEntity): Tag = Tag(
        id = entity.id,
        name = entity.name,
        colorArgb = entity.colorArgb
    )

    fun toEntity(domain: Tag): TagEntity = TagEntity(
        id = domain.id,
        name = domain.name,
        colorArgb = domain.colorArgb
    )

    fun toDomainList(entities: List<TagEntity>): List<Tag> = entities.map { toDomain(it) }
}
