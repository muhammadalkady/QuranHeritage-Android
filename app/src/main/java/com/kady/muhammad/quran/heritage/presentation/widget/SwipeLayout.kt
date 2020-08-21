package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.FrameLayout
import com.kady.muhammad.quran.heritage.domain.log.Logger
import kotlin.math.abs

class SwipeLayout : FrameLayout {

    var disableSwipe = false

    private val tag = "SwipeLayout"
    private val isLoggingEnabled = true
    private val animationDuration = 150L
    private var dismissListener: (() -> Unit)? = null
    private var swipeListener: ((fraction: Float) -> Unit)? = null
    private var firstDistanceX: Float = Float.MIN_VALUE
    private var firstDistanceY: Float = Float.MIN_VALUE
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(
            context,
            gestureDetectorListener
        )
    }
    private val gestureDetectorListener: GestureDetector.OnGestureListener =
        object : GestureDetector.OnGestureListener {
            override fun onShowPress(e: MotionEvent?) {
                Logger.logI(tag, "onShowPress")
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                Logger.logI(tag, "onSingleTapUp", isLoggingEnabled)
                return false
            }

            override fun onDown(e: MotionEvent?): Boolean {
                Logger.logI(tag, "onDown", isLoggingEnabled)
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                Logger.logI(tag, "onFling", isLoggingEnabled)
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                Logger.logI(
                    tag,
                    "onScroll e2.rawX = ${e2?.x} | distanceX = $firstDistanceX |  distanceY = $firstDistanceY",
                    true
                )
                e1 ?: return false
                e2 ?: return false
                if (firstDistanceX == Float.MIN_VALUE && firstDistanceY == Float.MIN_VALUE) {
                    firstDistanceX = distanceX
                    firstDistanceY = distanceY
                }
                if (abs(firstDistanceY) > abs(firstDistanceX)) return false
                val newX: Float = e2.rawX - e1.rawX
                x = if (newX < 0F) 0F else newX
                Logger.logI(
                    tag,
                    "onScroll x = $x , e1.rawX = ${e1.rawX} , e2.rawX = ${e2.rawX}",
                    false
                )
                return false
            }

            override fun onLongPress(e: MotionEvent?) {
                Logger.logI(tag, "onLongPress", isLoggingEnabled)
            }

        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        Logger.logI(tag, "dispatchTouchEvent", isLoggingEnabled)
        if (!disableSwipe) {
            gestureDetector.onTouchEvent(ev)
            if (ev != null && ev.action == MotionEvent.ACTION_UP) onUpTouch()
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun setX(x: Float) {
        super.setX(x)
        swipeListener?.invoke(x.div(width.toFloat()))
    }

    fun setDismissListener(dismissListener: (() -> Unit)?) {
        this.dismissListener = dismissListener
    }

    fun setSwipeListener(swipeListener: ((Float) -> Unit)?) {
        this.swipeListener = swipeListener
    }

    private fun onUpTouch() {
        firstDistanceX = Float.MIN_VALUE
        firstDistanceY = Float.MIN_VALUE
        val dismissPoint: Float = width.div(other = 2F)
        if (x >= dismissPoint) {
            animate()
                .x(width.toFloat())
                .setDuration(animationDuration)
                .withEndAction { dismissListener?.invoke() }
                .setUpdateListener { swipeListener?.invoke(x.div(width.toFloat())) }
                .start()
        } else {
            animate()
                .x(0F)
                .setDuration(animationDuration)
                .setUpdateListener { swipeListener?.invoke(x.div(width.toFloat())) }
                .start()
        }
    }

}