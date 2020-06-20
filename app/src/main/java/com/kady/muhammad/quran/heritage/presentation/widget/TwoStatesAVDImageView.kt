package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import com.kady.muhammad.quran.heritage.R

class TwoStatesAVDImageView : AppCompatImageView {

    private lateinit var state1Avd: AnimatedVectorDrawable

    private lateinit var state2Avd: AnimatedVectorDrawable

    private var state = 2

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
            state1Avd = typedArray.getDrawable(R.styleable.TwoStatesAVDImageView_avd1) as AnimatedVectorDrawable
            state2Avd = typedArray.getDrawable(R.styleable.TwoStatesAVDImageView_avd2) as AnimatedVectorDrawable
            //
            setImageDrawable(state1Avd)
            //
            typedArray.recycle()
        }
    }

    private fun restorePlayPauseState(state: Int) {
        if (state == 1) toState1() else toState2()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val bundle = Bundle()
        val superState = super.onSaveInstanceState()
        bundle.putParcelable("super_state", superState)
        bundle.putInt("state", state)
        return bundle
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        if (state is Bundle) {
            val superState: Parcelable? = state.getParcelable("super_state")
            this.state = state.getInt("state")
            restorePlayPauseState(this.state)
            super.onRestoreInstanceState(superState)
        }
    }

    fun toState1() {
        post {
            if (!state2Avd.isRunning && state != 1) {
                setImageDrawable(state1Avd)
                state1Avd.start()
                state = 1
            }
        }
    }

    fun toState2() {
        post {
            if (!state1Avd.isRunning && state != 2) {
                setImageDrawable(state2Avd)
                state2Avd.start()
                state = 2
            }
        }
    }

}