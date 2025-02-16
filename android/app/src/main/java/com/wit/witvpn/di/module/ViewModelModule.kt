package com.wit.witvpn.di.module

import com.limerse.iap.IapConnector
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent
import javax.inject.Singleton

/**
 * Created by Furuichi on 26/09/2023
 */
@Module
@InstallIn(ViewModelComponent::class)
class ViewModelModule {

    @Provides
    fun provideIapConnector(): IapConnector {
        return IapConnector()
    }
}