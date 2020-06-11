package com.kady.muhammad.quran.heritage

import android.app.Application
import com.kady.muhammad.quran.heritage.domain.lang.Lang.LANGUAGE_ARABIC
import com.yariksoffice.lingver.Lingver
import org.koin.core.context.startKoin
import java.util.*

class App : Application() {

    companion object {
        private lateinit var mutableAppInstance: App
        val APP_INSTANCE: App by lazy { mutableAppInstance }
    }

    override fun onCreate() {
        super.onCreate()
        Lingver.init(this, Locale(LANGUAGE_ARABIC))
        mutableAppInstance = this
        startKoin { modules(appModules) }
    }

}