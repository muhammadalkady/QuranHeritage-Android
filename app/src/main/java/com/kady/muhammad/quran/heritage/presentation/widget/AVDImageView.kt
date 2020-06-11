package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.kady.muhammad.quran.heritage.R

class AVDImageView : AppCompatImageView {

    private lateinit var avd: AnimatedVectorDrawable

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        resolveAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        resolveAttrs(attrs)
    }

    private fun resolveAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.AVDImageView)
            avd = typedArray.getDrawable(R.styleable.AVDImageView_avd) as AnimatedVectorDrawable
            //
            setImageDrawable(avd)
            //
            typedArray.recycle()
        }
    }

    fun startAVDAnim() {
        if (!avd.isRunning) avd.start()
    }

}