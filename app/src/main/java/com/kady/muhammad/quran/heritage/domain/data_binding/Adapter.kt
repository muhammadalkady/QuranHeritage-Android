package com.kady.muhammad.quran.heritage.domain.data_binding

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.widget.ImageViewCompat
import androidx.databinding.BindingAdapter
import com.kady.muhammad.quran.heritage.domain.ext.rangesOf
import com.kady.muhammad.quran.heritage.presentation.widget.TwoStatesAVDImageView

object Adapter {

    @BindingAdapter("position", "spanCount", "drawable1", "drawable2", requireAll = true)
    @JvmStatic
    fun setMediaBackground(
        view: View, position: Int, spanCount: Int,
        drawable1: Drawable, drawable2: Drawable
    ) {
        val background = if (spanCount.rem(2) != 0) {
            if (position.rem(2) == 0) drawable1 else drawable2
        } else {
            val positionToSumWith = if (position.rem(2) == 0) position + 1 else position - 1
            if (((position + positionToSumWith) - 1).rem(8) == 0) drawable1 else drawable2
        }
        view.background = background
    }

    @BindingAdapter(
        "foregroundColorSpanText",
        "foregroundColorSpanSubText", requireAll = true
    )
    @JvmStatic
    fun setText(textView: TextView, text: String, subText: String) {
        val ranges = text.rangesOf(subText)
        val spannable = SpannableString(text)
        val spans = ranges.map {
            ForegroundColorSpan(Color.RED)
        }
        ranges.forEachIndexed { index, range ->
            spannable.setSpan(
                spans[index],
                range.first, range.last + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        textView.setText(spannable, TextView.BufferType.SPANNABLE)
    }

    @BindingAdapter("textColor")
    @JvmStatic
    fun setTextColor(textView: TextView, color: Int) {
        textView.setTextColor(color)
    }

    @BindingAdapter("backgroundColor")
    @JvmStatic
    fun setBackground(view: View, color: Int) {
        view.setBackgroundColor(color)
    }

    @BindingAdapter("backgroundColor")
    @JvmStatic
    fun setBackground(cardView: CardView, color: Int) {
        cardView.setCardBackgroundColor(color)
    }

    @BindingAdapter("tintColor")
    @JvmStatic
    fun setSeekBarTintColor(appCompatSeekBar: AppCompatSeekBar, color: Int) {
        appCompatSeekBar.progressDrawable.setTintList(ColorStateList.valueOf(color))
        appCompatSeekBar.thumb.setTintList(ColorStateList.valueOf(color))
    }

    @BindingAdapter("avd1TintColor")
    @JvmStatic
    fun tintAvd1(twoStatesAVDImageView: TwoStatesAVDImageView, color: Int) {
        twoStatesAVDImageView.tintState1Drawable(color)
    }

    @BindingAdapter("avd2TintColor")
    @JvmStatic
    fun tintAvd2(twoStatesAVDImageView: TwoStatesAVDImageView, color: Int) {
        twoStatesAVDImageView.tintState2Drawable(color)
    }

    @BindingAdapter("tintColor")
    @JvmStatic
    fun setViewBackgroundTintColor(view: View, color: Int) {
        ViewCompat.setBackgroundTintList(view, ColorStateList.valueOf(color))
    }

    @BindingAdapter("tintColor")
    @JvmStatic
    fun setImageViewTintColor(imageView: ImageView, color: Int) {
        ImageViewCompat.setImageTintList(imageView, ColorStateList.valueOf(color))
    }

}