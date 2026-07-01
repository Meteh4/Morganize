package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.CategoryDao
import com.metoly.morganize.core.database.mapper.CategoryMapper
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.asResponseState
import com.metoly.morganize.core.model.suspendAsResponseStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Concrete implementation of [CategoryRepository] backed by a Room [CategoryDao].
 * Maps between [com.metoly.morganize.core.database.entity.CategoryEntity] and [Category].
 */
class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {

    override fun getAllCategories(): Flow<ResponseState<List<Category>>> =
        categoryDao.getAllCategories()
            .map { entities -> CategoryMapper.toDomainList(entities) }
            .asResponseState()

    override fun insertCategory(category: Category): Flow<ResponseState<Long>> =
        suspendAsResponseStateFlow {
            categoryDao.insertCategory(CategoryMapper.toEntity(category))
        }

    override fun deleteCategory(category: Category): Flow<ResponseState<Unit>> =
        suspendAsResponseStateFlow {
            categoryDao.deleteCategory(CategoryMapper.toEntity(category))
        }
}
