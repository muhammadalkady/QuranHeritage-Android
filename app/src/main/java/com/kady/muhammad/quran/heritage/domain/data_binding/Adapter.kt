package com.kady.muhammad.quran.heritage.domain.data_binding

import android.graphics.drawable.Drawable
import android.view.View
import androidx.databinding.BindingAdapter

object Adapter {

    @BindingAdapter("position", "spanCount", "drawable1", "drawable2", requireAll = true)
    @JvmStatic
    fun setMediaBackground(
        view: View,
        position: Int,
        spanCount: Int,
        drawable1: Drawable,
        drawable2: Drawable
    ) {
        val background = if (spanCount.rem(2) != 0) {
            if (position.rem(2) == 0) drawable1 else drawable2
        } else {
            val positionToSumWith = if (position.rem(2) == 0) position + 1 else position - 1
            if (((position + positionToSumWith) - 1).rem(8) == 0) drawable1 else drawable2
        }
        view.background = background
    }

}