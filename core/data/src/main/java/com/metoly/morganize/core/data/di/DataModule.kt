package com.metoly.morganize.core.data.di

import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.CategoryRepositoryImpl
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.data.NoteRepositoryImpl
import org.koin.dsl.module

/** Koin module binding repository implementations as singletons. */
val dataModule = module {
    single<NoteRepository> { NoteRepositoryImpl(noteDao = get()) }
    single<CategoryRepository> { CategoryRepositoryImpl(categoryDao = get()) }
    
    single { com.metoly.morganize.core.model.security.KeyManager() }
    single { com.metoly.morganize.core.model.security.EncryptionManager() }
}
