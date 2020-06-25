package com.kady.muhammad.quran.heritage

import android.app.Application
import com.kady.muhammad.quran.heritage.domain.api.API
import com.kady.muhammad.quran.heritage.domain.repo.MediaRepo
import com.kady.muhammad.quran.heritage.pref.Pref
import kotlinx.coroutines.Dispatchers
import org.koin.core.module.Module
import org.koin.dsl.module

val appModules: Module = module {

    single<Application> { App.APP_INSTANCE }
    single { Pref(get(), Dispatchers.IO) }
    single { API(Dispatchers.IO, get(), get()) }
    single { MediaRepo(Dispatchers.IO, get()) }

}