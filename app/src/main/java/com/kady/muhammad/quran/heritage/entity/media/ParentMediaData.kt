package com.kady.muhammad.quran.heritage.entity.media

import com.kady.muhammad.quran.heritage.entity.api_response.File
import com.kady.muhammad.quran.heritage.entity.api_response.Metadata

data class ParentMediaData(val metadata: Metadata, val files: List<File>)