package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.math.abs

class SwipeLayout : ConstraintLayout {

    var disableSwipe = false
    private var dismissListener: (() -> Unit)? = null
    private var swipeListener: ((fraction: Float) -> Unit)? = null
    private var firstDistanceX: Float = Float.MIN_VALUE
    private var firstDistanceY: Float = Float.MIN_VALUE
    private val animationDuration = 150L
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(
            context,
            gestureDetectorListener
        )
    }
    private val gestureDetectorListener: GestureDetector.OnGestureListener =
        object : GestureDetector.OnGestureListener {

            override fun onShowPress(e: MotionEvent?) {}

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                return false
            }

            override fun onDown(e: MotionEvent?): Boolean {
                return false
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                return false
            }

            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                e1 ?: return false
                e2 ?: return false
                if (firstDistanceX == Float.MIN_VALUE && firstDistanceY == Float.MIN_VALUE) {
                    firstDistanceX = distanceX
                    firstDistanceY = distanceY
                }
                if (abs(firstDistanceY) > abs(firstDistanceX)) return false
                val newX: Float = e2.rawX - e1.rawX
                x = if (newX < 0F) 0F else newX
                return false
            }

            override fun onLongPress(e: MotionEvent?) {
            }

        }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
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

    override fun performClick(): Boolean {
         super.performClick()
        return true
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