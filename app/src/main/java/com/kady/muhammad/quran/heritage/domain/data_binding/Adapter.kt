package com.kady.muhammad.quran.heritage.domain.data_binding

import android.view.View
import androidx.databinding.BindingAdapter

object Adapter {

    @BindingAdapter("position", "spanCount", "color1", "color2", requireAll = true)
    @JvmStatic
    fun setMediaBackground(view: View, position: Int, spanCount: Int, color1: Int, color2: Int) {
        val background = if (spanCount.rem(2) != 0) {
            if (position.rem(2) == 0) color1 else color2
        } else {
            val positionToSumWith = if (position.rem(2) == 0) position + 1 else position - 1
            if (((position + positionToSumWith) - 1).rem(8) == 0) color1 else color2
        }
        view.setBackgroundColor(background)
    }

}