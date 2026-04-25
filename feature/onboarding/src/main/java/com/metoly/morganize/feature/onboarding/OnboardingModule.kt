package com.metoly.morganize.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

private val Context.dataStore: DataStore<Preferences> by
preferencesDataStore(name = "onboarding_prefs")

private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")

val onboardingModule = module {
    single<DataStore<Preferences>> { androidContext().dataStore }
    viewModel { OnboardingViewModel(dataStore = get()) }
}
