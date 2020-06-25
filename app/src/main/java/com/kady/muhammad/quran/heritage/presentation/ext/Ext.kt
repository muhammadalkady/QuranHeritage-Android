package com.kady.muhammad.quran.heritage.presentation.ext

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.view.View
import com.kady.muhammad.quran.heritage.presentation.font.Font

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

fun View.upAnimation(): ObjectAnimator {
    val objectAnimator: ObjectAnimator = ObjectAnimator
        .ofFloat(this, "translationY", 0F, -10F)
    objectAnimator.repeatMode = ObjectAnimator.REVERSE
    objectAnimator.repeatCount = ObjectAnimator.INFINITE
    objectAnimator.duration = 1000L
    objectAnimator.start()
    return objectAnimator
}

fun textToBitmap(messageText: String, textSize: Float, textColor: Int, isBold: Boolean = true): Bitmap {
    val paint = Paint()
    paint.textSize = textSize
    paint.typeface = Typeface.create(Font.main, if (isBold) Typeface.BOLD else Typeface.NORMAL)
    paint.color = textColor
    paint.textAlign = Paint.Align.RIGHT
    paint.isAntiAlias = true
    val baseline: Float = -paint.ascent()
    val width: Int = (paint.measureText(messageText) + 0.5f).toInt()
    val height: Int = (baseline + paint.descent() + 0.5f).toInt()
    val image: Bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(image)
    canvas.drawText(messageText, width.toFloat(), baseline, paint)
    return image
}
