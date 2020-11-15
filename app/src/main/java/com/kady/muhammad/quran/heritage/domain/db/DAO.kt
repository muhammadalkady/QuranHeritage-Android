package com.kady.muhammad.quran.heritage.domain.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kady.muhammad.quran.heritage.entity.media.FavoriteMedia
import com.kady.muhammad.quran.heritage.entity.media.Media
import kotlinx.coroutines.flow.Flow

@Dao
interface DAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMedia(allMedia: List<Media>): List<Long>

    @Query("SELECT * FROM Media")
    fun getAllMedia(): Flow<List<Media>>

    @Query("SELECT * FROM Media WHERE id IN (:ids)")
    fun getMedia(ids: List<String>): Flow<List<Media>>

    @Query("DELETE FROM Media WHERE id IN (:ids)")
    suspend fun deleteMedia(ids: List<String>): Int

    @Insert
    suspend fun insertFavorite(favoriteMedia: FavoriteMedia): Long

    @Query("DELETE FROM FavoriteMedia WHERE id IN (:ids)")
    suspend fun deleteFavorite(ids: List<String>): Int

    @Query("SELECT * FROM FavoriteMedia")
    fun getAllFavorite(): Flow<List<FavoriteMedia>>

    @Query("SELECT EXISTS (SELECT * FROM FavoriteMedia WHERE id = :id)")
    suspend fun isFavorite(id: String): Boolean

}