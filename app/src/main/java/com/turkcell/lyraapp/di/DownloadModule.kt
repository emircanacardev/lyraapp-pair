package com.turkcell.lyraapp.di

import android.content.Context
import androidx.room.Room
import com.turkcell.lyraapp.data.download.DefaultDownloadRepository
import com.turkcell.lyraapp.data.download.DownloadRepository
import com.turkcell.lyraapp.data.download.DownloadedSongDao
import com.turkcell.lyraapp.data.download.LyraDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloadModule {

    @Binds
    @Singleton
    abstract fun bindDownloadRepository(impl: DefaultDownloadRepository): DownloadRepository

    companion object {

        @Provides
        @Singleton
        fun provideLyraDatabase(@ApplicationContext context: Context): LyraDatabase =
            Room.databaseBuilder(context, LyraDatabase::class.java, "lyra_database")
                .fallbackToDestructiveMigration(true)
                .build()

        @Provides
        @Singleton
        fun provideDownloadedSongDao(db: LyraDatabase): DownloadedSongDao =
            db.downloadedSongDao()
    }
}
