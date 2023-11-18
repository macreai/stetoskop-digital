package com.apicta.stetoskop_digital.model.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserPreference(private val dataStore: DataStore<Preferences>){

    fun getUserId(): Flow<Int> {
        return dataStore.data.map { preference ->
            preference[ID_KEY] ?: 0
        }
    }

    fun getUserToken(): Flow<String> {
        return dataStore.data.map { preference ->
            preference[TOKEN_KEY] ?: DEFAULT_ID
        }
    }

    suspend fun saveUserId(id: Int){
        dataStore.edit {
            it[ID_KEY] = id
        }
    }

    suspend fun saveUserToken(token: String){
        dataStore.edit {
            it[TOKEN_KEY] = token
        }
    }

    suspend fun deleteId(){
        dataStore.edit {
            it.remove(ID_KEY)
        }
    }

    suspend fun deleteToken(){
        dataStore.edit {
            it.remove(TOKEN_KEY)
        }
    }

    companion object {
        private const val DEFAULT_ID = ""
        @Volatile
        private var INSTANCE: UserPreference? = null
        private val ID_KEY = intPreferencesKey("id")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<Preferences>): UserPreference{
            return INSTANCE ?: synchronized(this){
                val instance = UserPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }
    }
}