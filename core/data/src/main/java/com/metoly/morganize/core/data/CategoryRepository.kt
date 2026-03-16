package com.metoly.morganize.core.data

import com.metoly.morganize.core.model.Category
import com.metoly.morganize.core.model.ResponseState
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {

    /** Emit all categories ordered by name, reactively. */
    fun getAllCategories(): Flow<ResponseState<List<Category>>>

    /** Insert a new category. Returns the generated row ID. */
    fun insertCategory(category: Category): Flow<ResponseState<Long>>

    /** Delete a category by its entity. */
    fun deleteCategory(category: Category): Flow<ResponseState<Unit>>
}
