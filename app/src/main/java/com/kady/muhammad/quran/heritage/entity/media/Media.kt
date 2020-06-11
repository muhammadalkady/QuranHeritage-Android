package com.kady.muhammad.quran.heritage.entity.media

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Media(
    @SerializedName("id") val id: String,
    @SerializedName("parent_id") val parentId: String,
    @SerializedName("title") val title: String,
    @SerializedName("is_list") val isList: Boolean
) : Parcelable

