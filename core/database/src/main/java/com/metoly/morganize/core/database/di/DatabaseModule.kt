package com.metoly.morganize.core.database.di

import androidx.room.Room
import com.metoly.morganize.core.database.AppDatabase
import com.metoly.morganize.core.database.MIGRATION_1_2
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/** Koin module providing the [AppDatabase] singleton, [NoteDao], and [CategoryDao]. */
val databaseModule = module {
    single {
        Room.databaseBuilder(
                        context = androidContext(),
                        klass = AppDatabase::class.java,
                        name = AppDatabase.DATABASE_NAME
                )
                .addMigrations(MIGRATION_1_2)
                .build()
    }

    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().categoryDao() }
}
