package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.kady.muhammad.quran.heritage.R

class TwoStatesAVDImageView : AppCompatImageView {

    private lateinit var state1Avd: AnimatedVectorDrawable
    private lateinit var state2Avd: AnimatedVectorDrawable
    private var state = -1

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        resolveAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        resolveAttrs(attrs)
    }

    private fun resolveAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.TwoStatesAVDImageView)
            state1Avd =
                typedArray.getDrawable(R.styleable.TwoStatesAVDImageView_avd1) as AnimatedVectorDrawable
            state2Avd =
                typedArray.getDrawable(R.styleable.TwoStatesAVDImageView_avd2) as AnimatedVectorDrawable
            //
            toState2()
            //
            typedArray.recycle()
        }
    }

    fun tintState1Drawable(color: Int) {
        state1Avd.setTint(color)
    }

    fun tintState2Drawable(color: Int) {
        state2Avd.setTint(color)
    }

    fun toState1() {
        post {
            if (state != 1 || state == -1) {
                state = 1
                setImageDrawable(state1Avd)
                state1Avd.start()
            }
        }
    }

    fun toState2() {
        post {
            if (state != 2 || state == -1) {
                state = 2
                setImageDrawable(state2Avd)
                state2Avd.start()
            }
        }
    }

}