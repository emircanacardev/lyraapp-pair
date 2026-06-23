package com.turkcell.lyraapp.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TokenStorageImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    companion object {
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    override suspend fun getAccessToken(): String? =
        dataStore.data.map { it[KEY_ACCESS_TOKEN] }.firstOrNull()

    override suspend fun saveAccessToken(token: String) {
        dataStore.edit { it[KEY_ACCESS_TOKEN] = token }
    }

    override suspend fun clearTokens() {
        dataStore.edit { it.remove(KEY_ACCESS_TOKEN) }
    }
}
