package com.kady.muhammad.quran.heritage.entity.media

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity
data class Media(
    @PrimaryKey @SerializedName("id") val id: String,
    @SerializedName("parent_id") val parentId: String,
    @SerializedName("title") val title: String,
    @SerializedName("parent_title") val parentTitle: String,
    @SerializedName("is_list") val isList: Boolean
) : Parcelable

