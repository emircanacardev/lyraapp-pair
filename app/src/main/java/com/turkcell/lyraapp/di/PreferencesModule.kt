package com.turkcell.lyraapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.turkcell.lyraapp.data.theme.ThemePreferencesRepository
import com.turkcell.lyraapp.data.theme.ThemePreferencesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Tema kalıcılığı ve DataStore bağımlılıklarını sağlayan Hilt modülü.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class PreferencesModule {

    @Binds
    @Singleton
    abstract fun bindThemePreferencesRepository(
        impl: ThemePreferencesRepositoryImpl
    ): ThemePreferencesRepository

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
            return PreferenceDataStoreFactory.create(
                produceFile = { context.preferencesDataStoreFile("lyra_preferences") }
            )
        }
    }
}
