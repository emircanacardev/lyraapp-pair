package com.turkcell.lyraapp.di

import com.turkcell.lyraapp.data.favorites.MockFavoritesRepository
import com.turkcell.lyraapp.data.favorites.FavoritesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Favorites feature'ının repository bağlamaları.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FavoritesModule {

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: MockFavoritesRepository): FavoritesRepository
}
