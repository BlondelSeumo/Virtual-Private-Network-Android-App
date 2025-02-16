package com.wit.witvpn.di.module

import com.wit.witvpn.data.preferences.DataStoreManager
import com.wit.witvpn.data.preferences.DataStoreManagerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Furuichi on 07/09/2023
 */
@InstallIn(SingletonComponent::class)
@Module
object LocalModule {
    @Singleton
    @Provides
    fun provideDataStoreManager(dataStoreManagerImpl: DataStoreManagerImpl): DataStoreManager {
        return dataStoreManagerImpl
    }

}