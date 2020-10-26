package com.kady.muhammad.quran.heritage.presentation.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.Resources
import android.view.View
import androidx.core.view.doOnLayout

private const val HEIGHT_ANIMATION_DURATION = 1_000L

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

fun View.animateHeight(duration: Long = HEIGHT_ANIMATION_DURATION) {
    doOnLayout {
        val oldHeight = 0
        val newHeight = height
        val valueAnimator =
            ValueAnimator.ofInt(oldHeight, newHeight)
        valueAnimator.addUpdateListener {
            val lp = layoutParams
            lp.height = it.animatedValue as Int
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