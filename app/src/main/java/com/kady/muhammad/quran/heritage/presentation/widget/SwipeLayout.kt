package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import kotlin.math.abs
import kotlin.math.log

class SwipeLayout : ConstraintLayout {

    var disableSwipe = false
    var swipeDirection: SwipeDirection = SwipeDirection.Right
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
                val finalX =
                    if (!disableSwipe) {
                        if (newX < 0F && swipeDirection == SwipeDirection.Left) {
                            newX
                        } else if (newX > 0F && swipeDirection == SwipeDirection.Right) {
                            newX
                        } else {
                            0F
                        }
                    } else {
                        Logger.logI("SwipeLayout", "newX = $newX")
                        val logX = log(abs(newX), base = 1.05F)
                        if (swipeDirection == SwipeDirection.Right && newX > 0) {
                            logX
                        } else if (swipeDirection == SwipeDirection.Left && newX < 0) {
                            -logX
                        } else {
                            0F
                        }
                    }
                Logger.logI("SwipeLayout", "finalX = $finalX")
                if (!finalX.isNaN()) x = finalX
                return false
            }

            override fun onLongPress(e: MotionEvent?) {
            }

        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        resolveAttrs(attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet?,
        @AttrRes defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
        resolveAttrs(attrs)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        gestureDetector.onTouchEvent(ev)
        if (ev != null && ev.action == MotionEvent.ACTION_UP) onUpTouch()
        return if (ev?.action == MotionEvent.ACTION_UP && x != 0F) false
        else super.dispatchTouchEvent(ev)
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

    private fun resolveAttrs(attrs: AttributeSet?) {
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, R.styleable.SwipeLayout)
            swipeDirection = if (typedArray.getInt(
                    R.styleable.SwipeLayout_swipeDirection,
                    0
                ) == 0
            ) SwipeDirection.Right else SwipeDirection.Left
            disableSwipe = typedArray.getBoolean(R.styleable.SwipeLayout_disableSwipe, false)
            //
            typedArray.recycle()
        }
    }

    private fun onUpTouch() {
        firstDistanceX = Float.MIN_VALUE
        firstDistanceY = Float.MIN_VALUE
        val dismissPoint: Float = width.div(other = 3F)
        Logger.logI("SwipeLayout", "onUpTouch x = $x")
        if (abs(x) >= dismissPoint) {
            animate()
                .x(if (swipeDirection == SwipeDirection.Right) width.toFloat() else -width.toFloat())
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

    sealed class SwipeDirection {
        object Right : SwipeDirection()
        object Left : SwipeDirection()
    }

}