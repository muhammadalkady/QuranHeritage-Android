package com.kady.muhammad.quran.heritage.presentation.font

import android.app.Application
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat
import com.kady.muhammad.quran.heritage.R
import org.koin.core.KoinComponent
import org.koin.core.inject

object Font : KoinComponent {
    private val app: Application by inject()
    val main: Typeface = ResourcesCompat.getFont(app, R.font.main)!!
}