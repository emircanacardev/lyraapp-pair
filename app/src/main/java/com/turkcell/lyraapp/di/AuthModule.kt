package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.auth.AuthRepository
import com.turkcell.lyraapp.data.auth.DefaultAuthRepository
import com.turkcell.lyraapp.data.auth.TokenStorage
import com.turkcell.lyraapp.data.auth.TokenStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: DefaultAuthRepository): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenStorageImpl): TokenStorage
}
