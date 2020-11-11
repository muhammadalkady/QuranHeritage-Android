package com.kady.muhammad.quran.heritage.entity.media

import com.google.gson.annotations.SerializedName

data class ParentMediaLocal(
    @SerializedName("parent_id") val parentId: String,
    @SerializedName("id") val id: String
)