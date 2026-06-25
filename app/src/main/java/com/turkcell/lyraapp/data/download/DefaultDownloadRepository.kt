package com.turkcell.lyraapp.data.download

import android.content.Context
import android.os.Environment
import com.turkcell.lyraapp.data.player.PlayerRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import javax.inject.Inject

class DefaultDownloadRepository @Inject constructor(
    private val playerRepository: PlayerRepository,
    private val dao: DownloadedSongDao,
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context,
) : DownloadRepository {

    override suspend fun downloadSong(
        songId: String,
        title: String,
        artist: String,
    ): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = playerRepository.getStreamUrl(songId).getOrThrow()
            val dir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) ?: context.filesDir
            val file = File(dir, "$songId.wav")

            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                check(response.isSuccessful) { "Sunucu hatasi: HTTP ${response.code}" }
                val body = checkNotNull(response.body) { "Bos yanit govdesi" }
                body.byteStream().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
            }

            dao.insertDownload(
                DownloadedSong(
                    songId = songId,
                    title = title,
                    artist = artist,
                    localFilePath = file.absolutePath,
                )
            )
        }
    }

    override suspend fun isDownloaded(songId: String): Boolean = withContext(Dispatchers.IO) {
        val record = dao.getDownload(songId) ?: return@withContext false
        File(record.localFilePath).exists()
    }

    override suspend fun getLocalFilePath(songId: String): String? = withContext(Dispatchers.IO) {
        val record = dao.getDownload(songId) ?: return@withContext null
        val file = File(record.localFilePath)
        if (file.exists()) file.absolutePath else null
    }

    override suspend fun deleteDownload(songId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                dao.getDownload(songId)?.let { File(it.localFilePath).delete() }
                dao.deleteDownload(songId)
            }
        }

    override fun observeDownloads(): Flow<List<DownloadedSong>> = dao.observeAll()
}
