package com.kady.muhammad.quran.heritage.domain.db

import android.app.Application
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.kady.muhammad.quran.heritage.entity.media.FavoriteMedia
import com.kady.muhammad.quran.heritage.entity.media.Media
import kotlinx.coroutines.flow.Flow
import org.koin.core.KoinComponent
import org.koin.core.inject

object DB : KoinComponent {

    private const val DB_VERSION = 1
    private const val DB_NAME = "quran_heritage_db"
    private val app: Application by inject()
    private val roomDb: QuranHeritageDb =
        Room.databaseBuilder(app, QuranHeritageDb::class.java, DB_NAME).build()

    @Database(
        entities = [Media::class, FavoriteMedia::class],
        version = DB_VERSION,
        exportSchema = true
    )
    abstract class QuranHeritageDb : RoomDatabase() {
        abstract fun dao(): DAO
    }

    suspend fun insertAllMedia(allMedia: List<Media>): List<Long> {
        return roomDb.dao().insertAllMedia(allMedia = allMedia)
    }

    fun getAllMedia(): Flow<List<Media>> {
        return roomDb.dao().getAllMedia()
    }

    fun getMedia(ids: List<String>): Flow<List<Media>> {
        return roomDb.dao().getMedia(ids)
    }

    suspend fun deleteMedia(ids: List<String>): Int {
        return roomDb.dao().deleteMedia(ids)
    }

    fun getAllFavorite(): Flow<List<FavoriteMedia>> {
        return roomDb.dao().getAllFavorite()
    }

    suspend fun isFavorite(id: String): Boolean {
        return roomDb.dao().isFavorite(id)
    }

    suspend fun insertFavorite(favoriteMedia: FavoriteMedia): Long {
        return roomDb.dao().insertFavorite(favoriteMedia)
    }

    suspend fun deleteFavorite(ids: List<String>): Int {
        return roomDb.dao().deleteFavorite(ids)
    }

}