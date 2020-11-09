package com.kady.muhammad.quran.heritage.presentation.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.DecelerateInterpolator
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.kady.muhammad.quran.heritage.R
import kotlin.math.abs
import kotlin.math.log

class HorizontalSwipeLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    var disableSwipe: Boolean = false

    //
    private var swipeDirection: SwipeDirection = SwipeDirection.RIGHT
    private var swipeBackOnTouchUp: Boolean = true
    private var maxSwipe: Float = 0F
    private var dismissFraction = 0F

    //
    private var swipeRecyclerView: SwipeRecyclerView? = null
    private var childrenTags: String? = null
    private var lastDownTime: Long = 0L
    private var firstDistanceXPosition: Float = Float.NaN
    private var firstDistanceYPosition: Float = Float.NaN
    private var lastHorizontalSwipeFraction: Float = Float.NaN

    //
    private val horizontalSwipeListeners: MutableList<HorizontalSwipeListener> = mutableListOf()
    private val dismissListeners: MutableList<DismissListener> = mutableListOf()
    private val onTouchUpListeners: MutableList<OnTouchUpListener> = mutableListOf()

    //
    private val gestureDetector: GestureDetectorCompat =
        GestureDetectorCompat(context, object : OnGestureListenerAdapter() {

            override fun onDown(e: MotionEvent?): Boolean {
                val isSwiped = isSwiped()
                if (isSwiped) swipeBack()
                getOthersHorizontalSwipeLayouts().forEach { it.swipeBack() }
                return isSwiped
            }

            override fun onScroll(
                e1: MotionEvent?, e2: MotionEvent?,
                distanceX: Float, distanceY: Float
            ): Boolean {
                e1 ?: return false
                e2 ?: return false
                handleOnScroll(distanceX, distanceY, e1, e2)
                return false
            }

        })

    init {
        isClickable = true
        isFocusable = true
        resolveAttrs(attrs)
    }

    override fun dispatchTouchEvent(e: MotionEvent?): Boolean {
        if (gestureDetector.onTouchEvent(e)) return false
        if (isActionUp(e)) onActionUp()
        if (isActionUp(e) && isSwiped())
            if (hasMaxSwipe() || isAtEnoughMaxSwipe()) toMaxSwipe()
            else if (isSwipedBeforeDismissFraction() || hasNoDismissFraction()) swipeBack()
        if (isActionUp(e) && swipeBackOnTouchUp && !disableSwipe && hasDismissFraction())
            if (isSwipedBeforeDismissFraction()) swipeBack()
            else if (isSwipedAtOrAfterDismissFraction()) animateDismiss()
        if (isActionDown(e)) onActionDown()
        return super.dispatchTouchEvent(e)
    }

    override fun performClick(): Boolean {
        if (isSwipeRecyclerViewScrolling() || canClick() || isSwiped()) {
            return false
        }
        return super.performClick()
    }

    fun swipeBack(animate: Boolean = true) {
        animateTranslationXToPosition(0F, if (animate) ANIMATION_DURATION else 0L)
    }

    fun toMaxSwipe(animate: Boolean = true) {
        animateTranslationXToPosition(
            if (isSwipeDirectionRight()) maxSwipe else -maxSwipe,
            if (animate) ANIMATION_DURATION else 0L
        )
    }

    fun setUpWithRecyclerView(swipeRecyclerView: SwipeRecyclerView, childrenTags: String) {
        this.swipeRecyclerView = swipeRecyclerView
        this.childrenTags = childrenTags
    }

    fun addHorizontalSwipeListener(horizontalSwipeListener: HorizontalSwipeListener) {
        horizontalSwipeListeners.add(horizontalSwipeListener)
    }

    fun removeHorizontalSwipeListener(horizontalSwipeListener: HorizontalSwipeListener) {
        horizontalSwipeListeners.remove(horizontalSwipeListener)
    }

    fun addDismissListener(dismissListener: DismissListener) {
        dismissListeners.add(dismissListener)
    }

    fun removeDismissListener(dismissListener: DismissListener) {
        dismissListeners.remove(dismissListener)
    }

    fun addOnTouchUpListener(onTouchUpListener: OnTouchUpListener) {
        onTouchUpListeners.add(onTouchUpListener)
    }

    fun removeTouchUpListener(onTouchUpListener: OnTouchUpListener) {
        onTouchUpListeners.remove(onTouchUpListener)
    }

    private fun isAtEnoughMaxSwipe(): Boolean {
        return maxSwipe.div(10F) <= abs(x)
    }

    private fun hasMaxSwipe(): Boolean {
        return maxSwipe != 0F
    }

    private fun animateDismiss() {
        animateTranslationXToPosition((if (isSwipeDirectionRight()) width else -width).f) {
            dismissListeners.forEach { it.onDismiss(this) }
        }
    }

    private fun isSwipedAtOrAfterDismissFraction(): Boolean {
        return abs(x).div(width.f) >= dismissFraction
    }

    private fun isSwipedBeforeDismissFraction(): Boolean {
        return abs(x).div(width.f) < dismissFraction
    }

    private fun isActionDown(e: MotionEvent?): Boolean {
        return e?.action == MotionEvent.ACTION_DOWN
    }

    private fun hasDismissFraction(): Boolean {
        return dismissFraction != 0F
    }

    private fun hasNoDismissFraction(): Boolean {
        return dismissFraction == 0F
    }

    private fun isActionUp(e: MotionEvent?): Boolean {
        return e?.action == MotionEvent.ACTION_UP
    }

    private fun canNotSwipe(): Boolean {
        return abs(firstDistanceYPosition) > abs(firstDistanceXPosition)
    }

    private fun isSwipeRecyclerViewScrolling(): Boolean {
        return swipeRecyclerView?.scrollState == RecyclerView.SCROLL_STATE_SETTLING ||
                swipeRecyclerView?.scrollState == RecyclerView.SCROLL_STATE_DRAGGING
    }

    private fun canClick(): Boolean {
        return System.currentTimeMillis() - lastDownTime > CLICK_DOWN_TIME
    }

    private fun onHorizontalSwipeSetTranslationX(translationX: Float) {
        val fraction = abs(translationX).div(if (hasMaxSwipe()) maxSwipe else width.f)
        if (lastHorizontalSwipeFraction != fraction) {
            lastHorizontalSwipeFraction = fraction
            horizontalSwipeListeners.forEach {
                it.onHorizontalSwipe(this, fraction)
            }
        }
    }

    private fun getOthersHorizontalSwipeLayouts(): List<HorizontalSwipeLayout> {
        return swipeRecyclerView?.children?.toList()?.mapNotNull {
            it.findViewWithTag<HorizontalSwipeLayout?>(childrenTags)
        }?.filterNot { it == this } ?: emptyList()
    }

    private fun isSwiped(): Boolean {
        return translationX != 0F
    }

    private fun resolveAttrs(attrs: AttributeSet?) {
        val typedArray: TypedArray =
            context.obtainStyledAttributes(attrs, R.styleable.HorizontalSwipeLayout)
        with(typedArray) {
            swipeDirection = getSwipeDirection()
            swipeBackOnTouchUp = getSwipeBackOnTouchUp()
            disableSwipe = getDisableSwipe()
            maxSwipe = getMaxSwipe()
            dismissFraction = getDismissFraction()
        }
        typedArray.recycle()
    }

    private fun TypedArray.getDismissFraction(): Float =
        getFloat(R.styleable.HorizontalSwipeLayout_dismissFraction, 0F)

    private fun TypedArray.getMaxSwipe(): Float {
        return getDimensionPixelSize(R.styleable.HorizontalSwipeLayout_maxSwipe, 0).f
    }

    private fun TypedArray.getDisableSwipe(): Boolean {
        return getBoolean(R.styleable.HorizontalSwipeLayout_disableSwipe, false)
    }

    private fun TypedArray.getSwipeBackOnTouchUp(): Boolean {
        return getBoolean(R.styleable.HorizontalSwipeLayout_swipeBackOnTouchUp, true)
    }

    private fun TypedArray.getSwipeDirection(): SwipeDirection {
        return if (getInt(R.styleable.HorizontalSwipeLayout_swipeDirection, 0) == 0)
            SwipeDirection.RIGHT else SwipeDirection.LEFT
    }

    private fun isSwipeDirectionRight(): Boolean {
        return swipeDirection == SwipeDirection.RIGHT
    }

    private fun onActionUp() {
        firstDistanceXPosition = Float.NaN
        firstDistanceYPosition = Float.NaN
        onTouchUpListeners.forEach { it.onTouchUp() }
    }

    private fun onActionDown() {
        lastDownTime = System.currentTimeMillis()
    }

    private fun animateTranslationXToPosition(
        to: Float,
        duration: Long = ANIMATION_DURATION,
        endAction: () -> Unit = {}
    ) {
        animate()
            .translationX(to)
            .setInterpolator(DecelerateInterpolator())
            .setDuration(duration)
            .setUpdateListener { onHorizontalSwipeSetTranslationX(translationX) }
            .withEndAction { endAction() }
            .start()
    }

    private fun handleOnScroll(
        distanceX: Float, distanceY: Float,
        e1: MotionEvent, e2: MotionEvent
    ) {
        if (firstDistanceXPosition.isNaN() && firstDistanceYPosition.isNaN()) {
            firstDistanceXPosition = distanceX
            firstDistanceYPosition = distanceY
        }
        if (canNotSwipe()) return
        val newX: Float = e2.rawX - e1.rawX
        val finalX: Float
        finalX = if (isSwipeDirectionRight()) {
            if (newX < 0F) 0F
            else newX
        } else {
            if (newX > 0F) 0F
            else newX
        }
        val finalXMaxSwipe: Float = if (maxSwipe == 0F) {
            finalX
        } else {
            if (isSwipeDirectionRight()) {
                if (finalX > maxSwipe) maxSwipe else finalX
            } else {
                if (abs(finalX) > maxSwipe) -maxSwipe else finalX
            }
        }
        val finalXLog: Float = if (disableSwipe) {
            val logX = log(abs(finalXMaxSwipe), 1.02F)
            if (finalXMaxSwipe > 0 && isSwipeDirectionRight()) logX else if (finalXMaxSwipe < 0 && !isSwipeDirectionRight()) -logX
            else 0F
        } else {
            finalXMaxSwipe
        }
        if (!finalX.isNaN()) {
            translationX = finalXLog
            onHorizontalSwipeSetTranslationX(translationX)
        }
    }

    private val Int.f: Float get() = this.toFloat()

    fun interface HorizontalSwipeListener {
        fun onHorizontalSwipe(horizontalSwipeLayout: HorizontalSwipeLayout, fraction: Float)
    }

    fun interface DismissListener {
        fun onDismiss(horizontalSwipeLayout: HorizontalSwipeLayout)
    }

    fun interface OnTouchUpListener {
        fun onTouchUp()
    }

    enum class SwipeDirection {
        RIGHT,
        LEFT
    }

    companion object {
        private const val ANIMATION_DURATION = 500L
        private const val CLICK_DOWN_TIME = 250L
    }

}