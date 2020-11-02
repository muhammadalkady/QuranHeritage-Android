package com.kady.muhammad.quran.heritage.entity.ext

import com.kady.muhammad.quran.heritage.entity.api_response.File
import com.kady.muhammad.quran.heritage.entity.media.Media

fun File.toMedia(parentId: String, parentTitle: String): Media {
    return Media(
        id = "${parentId}_$name", parentId = parentId,
        title = name.substring(0, name.lastIndexOf(".")),
        parentTitle = parentTitle,
        isList = false
    )
}
