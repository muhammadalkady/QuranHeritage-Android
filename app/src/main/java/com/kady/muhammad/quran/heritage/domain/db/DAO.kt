package com.kady.muhammad.quran.heritage.domain.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.kady.muhammad.quran.heritage.entity.media.Media

@Dao
interface DAO {
    @Insert
    suspend fun insertAllMedia(allMedia: List<Media>): List<Long>

    @Query("DELETE FROM Media")
    suspend fun deleteAllMedia()

    @Query("SELECT * FROM Media")
    suspend fun getAllMedia(): List<Media>
}