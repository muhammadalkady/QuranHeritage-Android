package com.kady.muhammad.quran.heritage.presentation.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.core.content.ContextCompat
import androidx.core.view.doOnLayout

private const val PROPERTY_ANIMATION_DURATION = 1_000L

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

enum class ViewProperty {
    WIDTH, HEIGHT
}

fun View.animateProperty(viewProperty: ViewProperty, duration: Long = PROPERTY_ANIMATION_DURATION) {
    doOnLayout {
        val oldProperty = 0
        val newPropertyValue = when (viewProperty) {
            ViewProperty.HEIGHT -> height
            ViewProperty.WIDTH -> width
        }
        val valueAnimator =
            ValueAnimator.ofInt(oldProperty, newPropertyValue)
        valueAnimator.addUpdateListener {
            val lp = layoutParams
            when (viewProperty) {
                ViewProperty.HEIGHT -> lp.height = it.animatedValue as Int
                ViewProperty.WIDTH -> lp.width = it.animatedValue as Int
            }
            this.layoutParams = lp
        }
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

fun Context.showKeyboard() {
    val imm: InputMethodManager? =
        ContextCompat.getSystemService(this, InputMethodManager::class.java)
    imm?.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, HIDE_NOT_ALWAYS)
}


fun Context.hideKeyboard() {
    val imm: InputMethodManager? =
        ContextCompat.getSystemService(this, InputMethodManager::class.java)
    imm?.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
}