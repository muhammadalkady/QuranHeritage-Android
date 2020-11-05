package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.AttrRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.log.Logger
import kotlin.math.abs
import kotlin.math.log

class SwipeLayout : ConstraintLayout {

    var disableSwipe = false
    private var swipeDirection: SwipeDirection = SwipeDirection.Right
    private var swipeLimitInPx: Float = 0F
    private var revertSwipeOnTouchUp: Boolean = true
    private var lastDownTime: Long = 0L
    private var dismissListener: (() -> Unit)? = null
    private var swipeListener: ((fraction: Float) -> Unit)? = null
    private var touchUpListener: (() -> Unit)? = null
    private var firstDistanceX: Float = Float.MIN_VALUE
    private var firstDistanceY: Float = Float.MIN_VALUE
    private val animationDuration = 150L
    private val clickDownTime = 250L
    private val gestureDetector: GestureDetector by lazy {
        GestureDetector(context, gestureDetectorListenerAdapter)
    }
    private val gestureDetectorListenerAdapter = object : OnGestureListenerAdapter() {

        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            Logger.logI(LOG_TAG + "_$tag", "onSingleTapUp")
            return false
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent?,
            distanceX: Float, distanceY: Float
        ): Boolean {
            Logger.logI(LOG_TAG + "_$tag", "onScroll")
            e1 ?: return false
            e2 ?: return false
            if (firstDistanceX == Float.MIN_VALUE && firstDistanceY == Float.MIN_VALUE) {
                firstDistanceX = distanceX
                firstDistanceY = distanceY
            }
            if (abs(firstDistanceY) > abs(firstDistanceX)) return false
            val newX: Float = e2.rawX - e1.rawX
            val finalX: Float = getFinalXPosition(newX)
            if (!finalX.isNaN()) x = finalX
            Logger.logI(LOG_TAG + "_$tag", "finalX = $finalX limit = $swipeLimitInPx")
            return false
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
        Logger.logI(LOG_TAG + "_$tag", "dispatchTouchEvent action = $ev")
        if (ev?.action == MotionEvent.ACTION_DOWN) lastDownTime = System.currentTimeMillis()
        gestureDetector.onTouchEvent(ev)
        if (ev != null && ev.action == MotionEvent.ACTION_UP && revertSwipeOnTouchUp) onUpTouch()
        return /*if (ev?.action == MotionEvent.ACTION_UP && x != 0F) false
        else*/ super.dispatchTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        Logger.logI(
            LOG_TAG + "_$tag",
            "performClick -> ${System.currentTimeMillis() - lastDownTime}"
        )
        if (parent is SwipeRecyclerView) {
            val parent = parent as RecyclerView
            if ((parent.scrollState ==
                        RecyclerView.SCROLL_STATE_DRAGGING ||
                        parent.scrollState == RecyclerView.SCROLL_STATE_SETTLING) ||
                (System.currentTimeMillis() - lastDownTime) > clickDownTime
            ) {
                Logger.logI(LOG_TAG + "_$tag", " performClick no click")
                return false
            }
        }
        return super.performClick()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        Logger.logI(LOG_TAG + "_$tag", "onInterceptTouchEvent")
        return super.onInterceptTouchEvent(ev)
    }

    override fun setX(x: Float) {
        super.setX(x)
        swipeListener?.invoke(x.div(width.toFloat()))
    }

    fun revertSwipe() {
        onUpTouch()
    }

    fun setDismissListener(dismissListener: (() -> Unit)?) {
        this.dismissListener = dismissListener
    }

    fun setSwipeListener(swipeListener: ((Float) -> Unit)?) {
        this.swipeListener = swipeListener
    }

    fun setTouchUpListener(touchUpListener: () -> Unit) {
        this.touchUpListener = touchUpListener
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
            swipeLimitInPx = typedArray
                .getDimensionPixelSize(R.styleable.SwipeLayout_swipeLimit, 0).toFloat()
            revertSwipeOnTouchUp =
                typedArray.getBoolean(R.styleable.SwipeLayout_revertSwipeOnTouchUp, true)
            //
            typedArray.recycle()
        }
    }

    private fun getFinalXPosition(newX: Float): Float {
        return if (!disableSwipe) {
            if (newX < 0F && swipeDirection == SwipeDirection.Left) {
                if (abs(newX) > swipeLimitInPx && swipeLimitInPx > 0) -swipeLimitInPx else newX
            } else if (newX > 0F && swipeDirection == SwipeDirection.Right) {
                if (newX > swipeLimitInPx && swipeLimitInPx > 0) swipeLimitInPx else newX
            } else {
                0F
            }
        } else {
            val logX = log(abs(newX), base = 1.03F)
            if (swipeDirection == SwipeDirection.Right && newX > 0) {
                logX
            } else if (swipeDirection == SwipeDirection.Left && newX < 0) {
                -logX
            } else {
                0F
            }
        }
    }

    private fun onUpTouch() {
        touchUpListener?.invoke()
        firstDistanceX = Float.MIN_VALUE
        firstDistanceY = Float.MIN_VALUE
        val dismissPoint: Float = getDismissPoint()
        Logger.logI(LOG_TAG + "_$tag", "onUpTouch x = $x")
        if (x == 0F) return
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

    private fun getDismissPoint() = width.div(other = 3F)

    sealed class SwipeDirection {
        object Right : SwipeDirection()
        object Left : SwipeDirection()
    }

    companion object {
        private const val LOG_TAG = "SwipeLayout"
    }

}