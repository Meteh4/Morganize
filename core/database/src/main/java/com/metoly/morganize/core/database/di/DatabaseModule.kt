package com.metoly.morganize.core.database.di

import androidx.room.Room
import com.metoly.morganize.core.database.AppDatabase
import com.metoly.morganize.core.database.MIGRATION_1_2
import com.metoly.morganize.core.database.MIGRATION_2_3
import com.metoly.morganize.core.database.MIGRATION_4_5
import com.metoly.morganize.core.database.MIGRATION_5_6
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            context = androidContext(),
            klass = AppDatabase::class.java,
            name = AppDatabase.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_4_5, MIGRATION_5_6)
            .fallbackToDestructiveMigration(false)
            .build()
    }

    single { get<AppDatabase>().noteDao() }
    single { get<AppDatabase>().categoryDao() }
}
