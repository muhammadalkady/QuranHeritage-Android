package com.kady.muhammad.quran.heritage.presentation.widget

import android.view.GestureDetector
import android.view.MotionEvent

abstract class OnGestureListenerAdapter : GestureDetector.OnGestureListener {

    override fun onDown(e: MotionEvent?): Boolean = false

    override fun onShowPress(e: MotionEvent?) = Unit

    override fun onSingleTapUp(e: MotionEvent?): Boolean = false

    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent?,
        distanceX: Float, distanceY: Float
    ): Boolean = false

    override fun onLongPress(e: MotionEvent?) = Unit

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent?,
        velocityX: Float, velocityY: Float
    ): Boolean = false
}