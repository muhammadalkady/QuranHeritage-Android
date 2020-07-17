package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import com.kady.muhammad.quran.heritage.domain.log.Logger

class SwipeLayout : FrameLayout {

    var disableSwipe = false

    private val tag = "SwipeLayout"
    private val animationDuration = 150L
    private var listener: (() -> Unit)? = null
    private val gestureDetector: GestureDetector by lazy { GestureDetector(context, gestureDetectorListener) }
    private val gestureDetectorListener: GestureDetector.OnGestureListener = object : GestureDetector.OnGestureListener {
        override fun onShowPress(e: MotionEvent?) {
            Logger.logI(tag, "onShowPress")
        }

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Logger.logI(tag, "onSingleTapUp")
            return false
        }

        override fun onDown(e: MotionEvent?): Boolean {
            Logger.logI(tag, "onDown")
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            Logger.logI(tag, "onFling")
            return false
        }

        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            Logger.logI(tag, "onScroll e2.rawX = ${e2?.x} | distanceX = $distanceX")
            e1 ?: return false
            e2 ?: return false
            if (e2.rawX > e1.rawX) x = e2.rawX - e1.rawX
            return false
        }

        override fun onLongPress(e: MotionEvent?) {
            Logger.logI(tag, "onLongPress")
        }

    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Logger.logI(tag, "dispatchTouchEvent")
        if (!disableSwipe) {
            gestureDetector.onTouchEvent(ev)
            if (ev != null && ev.action == MotionEvent.ACTION_UP) onUpTouch()
        }
        return super.dispatchTouchEvent(ev)
    }

    fun setDismissListener(listener: () -> Unit) {
        this.listener = listener
    }

    private fun onUpTouch() {
        val viewCenter: Float = width.div(other = 2F)
        if (x >= viewCenter) {
            animate()
                .x(width.toFloat())
                .setDuration(animationDuration)
                .withEndAction { listener?.invoke() }
                .start()
        } else {
            animate()
                .x(0F)
                .setDuration(animationDuration)
                .start()
        }
    }

}