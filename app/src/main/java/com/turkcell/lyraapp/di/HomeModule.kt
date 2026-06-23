package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.home.DefaultHomeRepository
import com.turkcell.lyraapp.data.home.HomeRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Home feature'ının repository bağlamaları. */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeModule {

    @Binds
    @Singleton
    abstract fun bindHomeRepository(impl: DefaultHomeRepository): HomeRepository
}
