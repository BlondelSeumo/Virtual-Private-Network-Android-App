package com.wit.witvpn.di.module

import com.wit.witvpn.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by Furuichi on 25/09/2023
 */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun provideUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds
    @Singleton
    abstract fun provideCommonRepository(impl: CommonRepositoryImpl): CommonRepository

    @Binds
    @Singleton
    abstract fun provideServerRepository(impl: ServerRepositoryImpl): ServerRepository
}