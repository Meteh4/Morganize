package com.metoly.morganize.core.data

import com.metoly.morganize.core.database.CategoryDao
import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ResponseState
import com.metoly.morganize.core.model.asResponseState
import com.metoly.morganize.core.model.suspendAsResponseStateFlow
import kotlinx.coroutines.flow.Flow

/**
 * Concrete implementation of [CategoryRepository] backed by a Room [CategoryDao].
 */
class CategoryRepositoryImpl(private val categoryDao: CategoryDao) : CategoryRepository {

    override fun getAllCategories(): Flow<ResponseState<List<Category>>> =
            categoryDao.getAllCategories().asResponseState()

    override fun insertCategory(category: Category): Flow<ResponseState<Long>> =
            suspendAsResponseStateFlow {
                categoryDao.insertCategory(category)
            }

    override fun deleteCategory(category: Category): Flow<ResponseState<Unit>> =
            suspendAsResponseStateFlow {
                categoryDao.deleteCategory(category)
            }
}
