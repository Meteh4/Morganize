package com.metoly.morganize

import android.app.Application
import com.metoly.morganize.core.data.di.dataModule
import com.metoly.morganize.core.database.di.databaseModule
import com.metoly.morganize.feature.create.createModule
import com.metoly.morganize.feature.edit.editModule
import com.metoly.morganize.feature.list.listModule
import com.metoly.morganize.feature.onboarding.onboardingModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

/**
 * Application class that initializes Koin with all modules from all features.
 *
 * Module loading order:
 * 1. [databaseModule] — Room database & DAO
 * 2. [dataModule] — Repository binding
 * 3. Feature view models
 */
class MorganizeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@MorganizeApplication)
            modules(
                    databaseModule,
                    dataModule,
                    onboardingModule,
                    listModule,
                    createModule,
                    editModule
            )
        }
    }
}
