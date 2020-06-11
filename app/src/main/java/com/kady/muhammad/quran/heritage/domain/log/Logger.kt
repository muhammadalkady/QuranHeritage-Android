package com.kady.muhammad.quran.heritage.domain.log

import android.util.Log
import com.kady.muhammad.quran.heritage.BuildConfig

object Logger {

    private val isLoggingEnabled: Boolean = BuildConfig.IS_LOGGING_ENABLED

    fun logE(tag: String, msg: String) {
        if (isLoggingEnabled) Log.e(tag, msg)
    }

    fun logI(tag: String, msg: String) {
        if (isLoggingEnabled) Log.i(tag, msg)
    }
}