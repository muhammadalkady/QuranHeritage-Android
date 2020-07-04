package com.kady.muhammad.quran.heritage.entity.api_response

import com.google.gson.annotations.SerializedName
import com.kady.muhammad.quran.heritage.entity.media.Media

data class GetMediaResponse(@SerializedName("data") val media: List<Media>) : Response