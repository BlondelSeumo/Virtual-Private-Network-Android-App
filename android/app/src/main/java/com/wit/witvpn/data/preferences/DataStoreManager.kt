package com.wit.witvpn.data.preferences

import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow

interface DataStoreManager {
    fun <T> getFlow(key: Preferences.Key<T>, default: T): Flow<T>
    suspend fun <T> get(key: Preferences.Key<T>, default: T): T
    suspend fun <T> set(key: Preferences.Key<T>, value: T)
    suspend fun <T> remove(key: Preferences.Key<T>)
}