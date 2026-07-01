package com.metoly.morganize.core.data.di

import com.metoly.morganize.core.data.CategoryRepository
import com.metoly.morganize.core.data.CategoryRepositoryImpl
import com.metoly.morganize.core.data.NoteRepository
import com.metoly.morganize.core.data.NoteRepositoryImpl
import com.metoly.morganize.core.data.TagRepository
import com.metoly.morganize.core.data.TagRepositoryImpl
import com.metoly.morganize.core.database.mapper.NoteMapper
import com.metoly.morganize.core.database.mapper.TagMapper
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import com.metoly.morganize.core.data.UserPreferencesRepository
import com.metoly.morganize.core.data.ExportImportHelper

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(name = "morganize_settings")

/** Koin module binding repository implementations as singletons. */
val dataModule = module {
    single<NoteRepository> { NoteRepositoryImpl(noteDao = get(), noteMapper = get(), context = androidContext()) }
    single<CategoryRepository> { CategoryRepositoryImpl(categoryDao = get()) }
    single<TagRepository> { TagRepositoryImpl(tagDao = get(), tagMapper = get()) }
    
    single { com.metoly.morganize.core.model.security.KeyManager() }
    single { com.metoly.morganize.core.model.security.EncryptionManager() }
    
    /** Shared Json instance configured for forward-compatible schema evolution. */
    single {
        Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }
    }
    
    /** NoteMapper converts between NoteEntity (Room) and Note (domain). */
    single { NoteMapper(json = get()) }
    
    single { TagMapper() }
    
    single<DataStore<Preferences>>(named("app_settings")) { androidContext().appDataStore }
    single { UserPreferencesRepository(dataStore = get(named("app_settings"))) }
    
    single { ExportImportHelper(context = androidContext(), json = get(), noteRepository = get()) }
}
