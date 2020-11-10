package com.kady.muhammad.quran.heritage.presentation.color

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ColorViewModel : ViewModel() {

    //
    val primaryColor: MutableLiveData<Int> = MutableLiveData(Color.colorPrimary)
    val primaryDarkColor: MutableLiveData<Int> = MutableLiveData(Color.colorPrimaryDark)

    //
    val color1: MutableLiveData<Int> = MutableLiveData(Color.colorPrimary)
    val color2: MutableLiveData<Int> = MutableLiveData(Color.lightenColor(Color.colorPrimary, 0.1F))

    //
    //enabled
    val avdColor1: MutableLiveData<Int> = MutableLiveData(Color.colorBlack)

    //disabled
    val avdColor2: MutableLiveData<Int> =
        MutableLiveData((Color.lightenColor(Color.colorBlack, 0.8F)))

    //
    val textPrimaryColor: MutableLiveData<Int> = MutableLiveData(Color.colorBlack)
    val textSecondaryColor: MutableLiveData<Int> =
        MutableLiveData((Color.lightenColor(Color.colorBlack, 0.2F)))
}