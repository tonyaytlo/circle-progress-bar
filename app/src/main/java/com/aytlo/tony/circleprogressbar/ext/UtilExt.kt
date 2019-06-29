package com.aytlo.tony.circleprogressbar.ext

import android.content.Context
import android.graphics.LinearGradient
import android.graphics.RectF
import android.graphics.Shader
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat

fun View.getColorRes(@ColorRes colorRes: Int) = context.getColorRes(colorRes)

fun Context.getColorRes(@ColorRes colorRes: Int) = ContextCompat.getColor(this, colorRes)

fun Float.half() = this / 2

fun LinearGradient.create(
    rectF: RectF,
    @ColorInt color0: Int,
    @ColorInt color1: Int,
    tile: Shader.TileMode
): LinearGradient {
    return LinearGradient(
        rectF.left,
        rectF.top,
        rectF.left,
        rectF.bottom,
        color0,
        color1,
        tile
    )
}

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)
