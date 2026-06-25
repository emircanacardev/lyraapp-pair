package com.turkcell.lyraapp.data.download

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "downloaded_songs")
data class DownloadedSong(
    @PrimaryKey val songId: String,
    val title: String,
    val artist: String,
    val localFilePath: String,
    val downloadedAt: Long = System.currentTimeMillis(),
)

@Dao
interface DownloadedSongDao {

    @Query("SELECT * FROM downloaded_songs WHERE songId = :songId")
    suspend fun getDownload(songId: String): DownloadedSong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(song: DownloadedSong)

    @Query("DELETE FROM downloaded_songs WHERE songId = :songId")
    suspend fun deleteDownload(songId: String)

    @Query("SELECT * FROM downloaded_songs")
    fun observeAll(): Flow<List<DownloadedSong>>
}

@Database(entities = [DownloadedSong::class], version = 1, exportSchema = false)
abstract class LyraDatabase : RoomDatabase() {
    abstract fun downloadedSongDao(): DownloadedSongDao
}
