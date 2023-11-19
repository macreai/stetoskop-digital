package com.apicta.stetoskop_digital.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.apicta.stetoskop_digital.util.Injection

class ViewModelFactory(private val param: Any): ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)){
            return AuthViewModel(Injection.provideUserPreference(param as DataStore<Preferences>)) as T
        } else if (modelClass.isAssignableFrom(AudioViewModel::class.java)){
            return AudioViewModel(Injection.provideUserPreference(param as DataStore<Preferences>)) as T
        } else if (modelClass.isAssignableFrom(RecordViewModel::class.java)){
            return RecordViewModel(Injection.provideUserPreference(param as DataStore<Preferences>)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}