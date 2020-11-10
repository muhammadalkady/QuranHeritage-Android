package com.kady.muhammad.quran.heritage.presentation.color

import android.app.Application
import android.graphics.Color
import androidx.annotation.ColorInt
import androidx.annotation.Size
import androidx.core.content.ContextCompat
import com.kady.muhammad.quran.heritage.R
import com.kady.muhammad.quran.heritage.domain.pref.Pref
import kotlinx.coroutines.runBlocking
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.math.max
import kotlin.math.min

object Color : KoinComponent {

    const val colorWhite = Color.WHITE
    const val colorBlack = Color.BLACK
    //

    private const val PRIMARY_COLOR_KEY = "primary_color"
    private const val PRIMARY_DARK_COLOR_KEY = "primary_dark_color"
    private const val COLOR_1 = "color1"
    private const val COLOR_2 = "color2"
    private const val AVD_COLOR_1 = "avd_color1"
    private const val AVD_COLOR_2 = "avd_color2"
    private const val TEXT_PRIMARY_COLOR_KEY = "text_primary_color"
    private const val TEXT_SECONDARY_COLOR_KEY = "text_secondary_color"

    //
    private val app: Application by inject()
    private val pref: Pref by inject()
    val colorPrimary = ContextCompat.getColor(app, R.color.colorPrimary)
    val colorPrimaryDark = ContextCompat.getColor(app, R.color.colorPrimaryDark)


    fun savePrimaryColor(color: Int) {
        saveColor(PRIMARY_COLOR_KEY, color)
    }

    fun getPrimaryColor(color: Int): Int {
        return getColor(PRIMARY_COLOR_KEY, color)
    }

    fun savePrimaryDarkColor(color: Int) {
        saveColor(PRIMARY_DARK_COLOR_KEY, color)
    }

    fun getPrimaryDarkColor(color: Int): Int {
        return getColor(PRIMARY_DARK_COLOR_KEY, color)
    }

    fun saveColor1(color: Int) {
        saveColor(COLOR_1, color)
    }

    fun getColor1(color: Int): Int {
        return getColor(COLOR_1, color)
    }

    fun saveColor2(color: Int) {
        saveColor(COLOR_2, color)
    }

    fun getColor2(color: Int): Int {
        return getColor(COLOR_2, color)
    }

    fun saveAvdColor1(color: Int) {
        saveColor(AVD_COLOR_1, color)
    }

    fun getAvdColor1(color: Int): Int {
        return getColor(AVD_COLOR_1, color)
    }

    fun saveAvdColor2(color: Int) {
        saveColor(AVD_COLOR_2, color)
    }

    fun getAvdColor2(color: Int): Int {
        return getColor(AVD_COLOR_2, color)
    }

    fun saveTextPrimaryColor(color: Int) {
        saveColor(TEXT_PRIMARY_COLOR_KEY, color)
    }

    fun getTextPrimaryColor(color: Int): Int {
        return getColor(TEXT_PRIMARY_COLOR_KEY, color)
    }

    fun saveTextSecondaryColor(color: Int) {
        saveColor(TEXT_SECONDARY_COLOR_KEY, color)
    }

    fun getTextSecondaryColor(color: Int): Int {
        return getColor(TEXT_SECONDARY_COLOR_KEY, color)
    }

    private fun saveColor(key: String, value: Int) = runBlocking {
        pref.saveInt(key, value)
    }

    private fun getColor(key: String, defaultValue: Int): Int = runBlocking {
        pref.getInt(key, defaultValue)
    }

    fun colorIndexFromPosition(spanCount: Int, position: Int): Int {
        return if (spanCount.rem(2) != 0) {
            if (position.rem(2) == 0) 0 else 1
        } else {
            val positionToSumWith = if (position.rem(2) == 0) position + 1 else position - 1
            if (((position + positionToSumWith) - 1).rem(8) == 0) 0 else 1
        }
    }

    @ColorInt
    fun lightenColor(
        @ColorInt color: Int,
        value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] += value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }

    @ColorInt
    fun darkenColor(
        @ColorInt color: Int,
        value: Float
    ): Int {
        val hsl = colorToHSL(color)
        hsl[2] -= value
        hsl[2] = max(0f, min(hsl[2], 1f))
        return hslToColor(hsl)
    }

    @Size(3)
    private fun colorToHSL(
        @ColorInt color: Int,
        @Size(3) hsl: FloatArray = FloatArray(3)
    ): FloatArray {
        val r = Color.red(color) / 255f
        val g = Color.green(color) / 255f
        val b = Color.blue(color) / 255f
        val max = max(r, max(g, b))
        val min = min(r, min(g, b))
        hsl[2] = (max + min) / 2
        if (max == min) {
            hsl[1] = 0f
            hsl[0] = hsl[1]
        } else {
            val d = max - min
            hsl[1] = if (hsl[2] > 0.5f) d / (2f - max - min) else d / (max + min)
            when (max) {
                r -> hsl[0] = (g - b) / d + (if (g < b) 6 else 0)
                g -> hsl[0] = (b - r) / d + 2
                b -> hsl[0] = (r - g) / d + 4
            }
            hsl[0] /= 6f
        }
        return hsl
    }

    @ColorInt
    private fun hslToColor(@Size(3) hsl: FloatArray): Int {
        val r: Float
        val g: Float
        val b: Float
        val h = hsl[0]
        val s = hsl[1]
        val l = hsl[2]
        if (s == 0f) {
            b = l
            g = b
            r = g
        } else {
            val q = if (l < 0.5f) l * (1 + s) else l + s - l * s
            val p = 2 * l - q
            r = hue2rgb(p, q, h + 1f / 3)
            g = hue2rgb(p, q, h)
            b = hue2rgb(p, q, h - 1f / 3)
        }
        return Color.rgb((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    private fun hue2rgb(p: Float, q: Float, t: Float): Float {
        var valueT = t
        if (valueT < 0) valueT += 1f
        if (valueT > 1) valueT -= 1f
        if (valueT < 1f / 6) return p + (q - p) * 6f * valueT
        if (valueT < 1f / 2) return q
        return if (valueT < 2f / 3) p + (q - p) * (2f / 3 - valueT) * 6f else p
    }

}