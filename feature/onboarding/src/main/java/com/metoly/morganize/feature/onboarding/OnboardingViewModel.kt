package com.metoly.morganize.feature.onboarding

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "onboarding_prefs")

private val ONBOARDING_DONE_KEY = booleanPreferencesKey("onboarding_done")

/**
 * ViewModel for the Onboarding screen. Reads and writes the onboarding-complete flag via DataStore.
 * Injected by Koin; scoped via [androidx.lifecycle.lifecycle-viewmodel-navigation3].
 */
class OnboardingViewModel(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    val hasCompletedOnboarding: StateFlow<Boolean?> = dataStore.data
        .map<Preferences, Boolean?> { prefs -> prefs[ONBOARDING_DONE_KEY] ?: false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = null
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.edit { prefs -> prefs[ONBOARDING_DONE_KEY] = true }
        }
    }
}
