package com.wit.witvpn.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private val Context.dataStore by preferencesDataStore("settings")

class DataStoreManagerImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : DataStoreManager {
    private val dataStore = appContext.dataStore

    override fun <T> getFlow(key: Preferences.Key<T>, default: T): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: default
        }
    }

    override suspend fun <T> get(key: Preferences.Key<T>, default: T): T {
        return suspendCoroutine {
            val result = runBlocking {
                dataStore.data
                    .map { preferences ->
                        preferences[key] ?: default
                    }
                    .firstOrNull() ?: default
            }
            it.resume(result)
        }
    }

    override suspend fun <T> set(key: Preferences.Key<T>, value: T) {
        dataStore.edit { settings ->
            settings[key] = value
        }
    }

    override suspend fun <T> remove(key: Preferences.Key<T>) {
        dataStore.edit { settings ->
            if (settings.contains(key)) {
                settings.remove(key)
            }
        }
    }

}