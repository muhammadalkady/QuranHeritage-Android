package com.kady.muhammad.quran.heritage.presentation.color

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kady.muhammad.quran.heritage.domain.log.Logger

class ColorViewModel : ViewModel() {

    //
    val primaryColor: MutableLiveData<Int> =
        MutableLiveData(Color.getPrimaryColor(Color.colorPrimary))
    val primaryDarkColor: MutableLiveData<Int> =
        MutableLiveData(Color.getPrimaryDarkColor(Color.colorPrimaryDark))

    //
    val color1: MutableLiveData<Int> = MutableLiveData(Color.getColor1(Color.colorPrimary))
    val color2: MutableLiveData<Int> =
        MutableLiveData(Color.getColor2(Color.lightenColor(Color.colorPrimary, 0.1F)))

    //
    //enabled
    val avdColor1: MutableLiveData<Int> = MutableLiveData(Color.getAvdColor1(Color.colorBlack))

    //disabled
    val avdColor2: MutableLiveData<Int> =
        MutableLiveData(Color.getAvdColor2(Color.lightenColor(Color.colorBlack, 0.8F)))

    //
    val textPrimaryColor: MutableLiveData<Int> =
        MutableLiveData(Color.getTextPrimaryColor(Color.colorBlack))
    val textSecondaryColor: MutableLiveData<Int> =
        MutableLiveData(Color.getTextSecondaryColor(Color.lightenColor(Color.colorBlack, 0.2F)))

    override fun onCleared() {
        Logger.logI("Color", "colorViewModel cleared")
        Color.savePrimaryColor(primaryColor.value!!)
        Color.savePrimaryDarkColor(primaryDarkColor.value!!)
        Color.saveColor1(color1.value!!)
        Color.saveColor2(color2.value!!)
        Color.saveAvdColor1(avdColor1.value!!)
        Color.saveAvdColor2(avdColor2.value!!)
        Color.saveTextPrimaryColor(textPrimaryColor.value!!)
        Color.saveTextSecondaryColor(textSecondaryColor.value!!)
    }
}