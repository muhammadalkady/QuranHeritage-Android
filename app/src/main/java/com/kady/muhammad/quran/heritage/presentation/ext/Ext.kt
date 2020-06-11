package com.kady.muhammad.quran.heritage.presentation.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View

fun View.show(onEnd: () -> Unit = {}) {
    animate().alpha(1F).setDuration(300).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = onEnd()
    }).start()
}

fun View.hide(onEnd: () -> Unit = {}) {
    animate().alpha(0F).setDuration(300).setListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator) = onEnd()
    }).start()
}
