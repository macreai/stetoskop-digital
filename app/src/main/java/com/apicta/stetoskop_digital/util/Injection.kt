package com.apicta.stetoskop_digital.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.apicta.stetoskop_digital.model.local.UserPreference

object Injection {
    fun provideUserPreference(dataStore: DataStore<Preferences>): UserPreference{
        return UserPreference.getInstance(
            dataStore
        )
    }
}