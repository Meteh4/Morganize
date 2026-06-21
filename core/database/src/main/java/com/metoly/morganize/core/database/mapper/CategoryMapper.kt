package com.metoly.morganize.core.database.mapper

import com.metoly.morganize.core.database.entity.CategoryEntity
import com.metoly.morganize.core.model.Category

/**
 * Bidirectional mapper between [CategoryEntity] (persistence) and [Category] (domain).
 */
object CategoryMapper {

    fun toDomain(entity: CategoryEntity): Category = Category(
        id = entity.id,
        name = entity.name,
        colorArgb = entity.colorArgb
    )

    fun toEntity(domain: Category): CategoryEntity = CategoryEntity(
        id = domain.id,
        name = domain.name,
        colorArgb = domain.colorArgb
    )

    fun toDomainList(entities: List<CategoryEntity>): List<Category> = entities.map { toDomain(it) }
}
