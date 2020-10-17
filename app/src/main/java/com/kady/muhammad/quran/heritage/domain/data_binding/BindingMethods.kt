package com.kady.muhammad.quran.heritage.domain.data_binding

import android.widget.ImageView
import androidx.databinding.BindingMethod
import androidx.databinding.BindingMethods

@BindingMethods(
    BindingMethod(
        type = ImageView::class,
        attribute = "app:srcCompat",
        method = "setImageDrawable"
    )
)
class BindingMethods