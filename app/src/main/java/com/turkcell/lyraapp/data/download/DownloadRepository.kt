package com.turkcell.lyraapp.data.download

import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    suspend fun downloadSong(songId: String, title: String, artist: String): Result<Unit>
    suspend fun isDownloaded(songId: String): Boolean
    suspend fun getLocalFilePath(songId: String): String?
    suspend fun deleteDownload(songId: String): Result<Unit>
    fun observeDownloads(): Flow<List<DownloadedSong>>
}
