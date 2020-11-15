package com.kady.muhammad.quran.heritage.presentation.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout

private const val ANIMATE_HEIGHT_DURATION = 1_000L

fun View.show(onEnd: () -> Unit = {}) {
    animate().alpha(1F).setDuration(300).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            visibility = View.VISIBLE
            onEnd()
        }
    }).start()
}

fun View.hide(onEnd: () -> Unit = {}) {
    animate().alpha(0F).setDuration(300).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) {
            visibility = View.INVISIBLE
            onEnd()
        }
    }).start()
}

fun View.upAnimation(): ObjectAnimator {
    val objectAnimator: ObjectAnimator = ObjectAnimator
        .ofFloat(this, "translationY", 0F, -10F)
    objectAnimator.repeatMode = ObjectAnimator.REVERSE
    objectAnimator.repeatCount = ObjectAnimator.INFINITE
    objectAnimator.duration = 1000L
    objectAnimator.start()
    return objectAnimator
}

fun View.animateHeight(
    duration: Long = ANIMATE_HEIGHT_DURATION,
    reverse: Boolean = false,
    onEnd: () -> Unit = {}
) {
    doOnLayout {
        val oldProperty = 0
        val valueAnimator =
            if (!reverse) ValueAnimator.ofInt(oldProperty, height)
            else ValueAnimator.ofInt(height, oldProperty)
        valueAnimator.addUpdateListener {
            val lp = layoutParams
            lp.height = it.animatedValue as Int
            this.layoutParams = lp
        }
        valueAnimator.doOnEnd { onEnd() }
        valueAnimator.duration = duration
        valueAnimator.start()
    }
}

val Float.px: Float
    get() {
        val res: Resources = Resources.getSystem()
        return this * res.displayMetrics.density
    }

val Float.dp: Float
    get() {
        val res: Resources = Resources.getSystem()
        return this / res.displayMetrics.density
    }

fun View.showKeyboard() {
    context.run {
        val imm: InputMethodManager? =
            ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.showSoftInput(this@showKeyboard, InputMethodManager.SHOW_IMPLICIT)
    }
}

fun View.hideKeyboard() {
    context.run {
        val imm: InputMethodManager? =
            ContextCompat.getSystemService(this, InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(
            this@hideKeyboard.windowToken, HIDE_NOT_ALWAYS
        )
    }
}