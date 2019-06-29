package com.aytlo.tony.circleprogressbar

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import com.aytlo.tony.circleprogressbar.CapType.Companion.BUT
import com.aytlo.tony.circleprogressbar.CapType.Companion.ROUND
import com.aytlo.tony.circleprogressbar.CapType.Companion.SQUARE
import com.aytlo.tony.circleprogressbar.GradientType.Companion.LINEAR_GRADIENT
import com.aytlo.tony.circleprogressbar.GradientType.Companion.NO_GRADIENT
import com.aytlo.tony.circleprogressbar.GradientType.Companion.RADIAL_GRADIENT
import com.aytlo.tony.circleprogressbar.GradientType.Companion.SWEEP_GRADIENT
import com.aytlo.tony.circleprogressbar.ext.getColorRes
import com.aytlo.tony.circleprogressbar.ext.half
import com.aytlo.tony.circleprogressbar.ext.unsafeLazy

class CircleProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val maxSweepAngle = THREE_HUNDRED_SIXTY_DEGREES

    private var sweepAngle = ZERO

    private val animator by unsafeLazy {
        ValueAnimator.ofInt().apply {
            duration = progressAnimationDuration
            interpolator = DecelerateInterpolator()
        }
    }

    private var currentProgress = 0 // animated progress value

    private var finalProgress = currentProgress // target progress value

    private val paintProgressBackground by unsafeLazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
    }

    private val paintProgressForeground by unsafeLazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.STROKE
        }
    }

    private val paintText by unsafeLazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    private val progressRect by unsafeLazy { RectF() }

    // attrs

    private var progressStrokeWidth = resources.getDimension(R.dimen.progress_stroke_width)

    private var progressBackgroundColor = getColorRes(R.color.colorPrimaryDark)

    private var progressForegroundColor = getColorRes(R.color.colorPrimaryDark)

    private var progressStartColor = getColorRes(R.color.colorPrimaryDark)

    private var progressEndColor = getColorRes(R.color.colorAccent)

    private var progressTextSize = context.resources.getDimension(R.dimen.progress_text_size)

    private var progressTextColor = getColorRes(R.color.colorPrimaryDark)

    private var progressStartAngle = -90f

    private var progressAnimationDuration = DEFAULT_ANIMATION_DURATION

    var textProgressDecorator: ((progress: Int) -> String)? = { "$currentProgress%" }

    init {
        initFromAttributes(attrs, context)
    }

    private fun initFromAttributes(attrs: AttributeSet?, context: Context) {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressBar)

        progressStrokeWidth = a.getDimension(R.styleable.CircleProgressBar_progress_stroke_width, progressStrokeWidth)
        progressBackgroundColor =
            a.getColor(R.styleable.CircleProgressBar_progress_background_color, progressBackgroundColor)
        progressForegroundColor =
            a.getColor(R.styleable.CircleProgressBar_progress_foreground_color, progressForegroundColor)
        progressStartColor =
            a.getColor(R.styleable.CircleProgressBar_progress_start_color, progressStartColor)
        progressEndColor =
            a.getColor(R.styleable.CircleProgressBar_progress_end_color, progressEndColor)
        progressTextSize = a.getDimension(R.styleable.CircleProgressBar_progress_text_size, progressTextSize)
        progressTextColor = a.getColor(R.styleable.CircleProgressBar_progress_text_color, progressTextColor)
        progressStartAngle = a.getFloat(R.styleable.CircleProgressBar_progress_start_angle, progressStartAngle)
        progressAnimationDuration = a.getInt(
            R.styleable.CircleProgressBar_progress_animation_duration,
            DEFAULT_ANIMATION_DURATION.toInt()
        ).toLong()

        val shaderType = a.getColor(R.styleable.CircleProgressBar_progress_shader, NO_GRADIENT)

        postSetGradient(shaderType, progressStartColor, progressEndColor)

        a.recycle()

        initPaints()
    }

    fun setProgressStrokeWidth(strokeWidth: Float) {
        progressStrokeWidth = strokeWidth
        initPaintsWithInvalidate()
    }

    fun setProgressBackgroundColor(@ColorInt color: Int) {
        progressBackgroundColor = color
        initPaintsWithInvalidate()
    }

    fun setProgressForegoundColor(@ColorInt color: Int) {
        progressForegroundColor = color
        initPaintsWithInvalidate()
    }

    fun setProgressTextSize(size: Float) {
        progressTextSize = size
        initPaintsWithInvalidate()
    }

    fun setProgressTextColor(@ColorInt color: Int) {
        progressTextColor = color
        initPaintsWithInvalidate()
    }

    fun setProgressAnimationDuration(duration: Long) {
        progressAnimationDuration = duration
        animator.duration = progressAnimationDuration
    }

    fun postSetGradient(@GradientType type: Int, startColor: Int, endColor: Int) {
        post {
            setGradient(type, startColor, endColor)
        }
    }

    private fun setGradient(@GradientType type: Int, startColor: Int, endColor: Int) {
        val shader = when (type) {
            LINEAR_GRADIENT -> LinearGradient(
                progressRect.left, progressRect.top,
                progressRect.left, progressRect.bottom,
                startColor, endColor, Shader.TileMode.CLAMP
            ).apply {
                setLocalMatrix(Matrix().apply {
                    setRotate(
                        progressStartAngle,
                        progressRect.centerX(),
                        progressRect.centerY()
                    )
                })
            }
            else -> null
        }
        paintProgressForeground.shader = shader
    }

    private fun initPaints() {
        paintProgressBackground.run {
            strokeWidth = progressStrokeWidth
            color = progressBackgroundColor
        }
        paintProgressForeground.run {
            strokeWidth = progressStrokeWidth
            color = progressForegroundColor
        }
        paintText.run {
            textSize = progressTextSize
            color = progressTextColor
        }
    }

    private fun initPaintsWithInvalidate() {
        initPaints()
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w == 0 || h == 0) {
            return
        }

        val xPadding = paddingStart + paddingEnd
        val yPadding = paddingTop + paddingBottom

        val freeW = (w - xPadding).toFloat()
        val freeH = (h - yPadding).toFloat()

        val halfStrokeWidth = progressStrokeWidth.half()

        progressRect.run {
            set(paddingStart.toFloat(), paddingTop.toFloat(), paddingStart + freeW, paddingTop + freeH)
            inset(halfStrokeWidth, halfStrokeWidth)
        }
    }

    override fun onDraw(canvas: Canvas) {
        drawProgress(canvas)
        drawTextIfNeed(canvas)
    }

    private fun drawProgress(canvas: Canvas) {
        canvas.run {
            drawArc(progressRect, ZERO, THREE_HUNDRED_SIXTY_DEGREES, false, paintProgressBackground)
            drawArc(progressRect, progressStartAngle, sweepAngle, false, paintProgressForeground)
        }
    }

    private fun drawTextIfNeed(canvas: Canvas) {
        textProgressDecorator?.let { transform ->
            val text = transform(currentProgress)
            canvas.run {
                translate(0F, -(paintText.descent() + paintText.ascent()) / 2)
                drawText(text, progressRect.centerX(), progressRect.centerY(), paintText)
            }
        }
    }

    private fun calcSweepAngleFromProgress(progress: Int) = maxSweepAngle / ONE_HUNDRED_PERCENTS * progress

    fun setProgress(progress: Int) {
        finalProgress = progress
        animator.run {
            setIntValues(currentProgress, progress)
            addUpdateListener { valueAnimator ->
                val newProgress = valueAnimator.animatedValue as Int
                sweepAngle = calcSweepAngleFromProgress(newProgress)
                currentProgress = newProgress
                invalidate()
            }
            start()
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()!!
        return SavedState(superState).apply {
            progress = finalProgress
        }
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is SavedState) {
            super.onRestoreInstanceState(state.superState)
            setProgress(state.progress)
            return
        }
        super.onRestoreInstanceState(state)
    }

    private class SavedState(source: Parcelable) : BaseSavedState(source) {
        var progress: Int = 0
    }

    companion object {
        private const val THREE_HUNDRED_SIXTY_DEGREES = 360F
        private const val DEFAULT_ANIMATION_DURATION = 100L
        private const val ONE_HUNDRED_PERCENTS = 100
        private const val ZERO = 0F
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(NO_GRADIENT, LINEAR_GRADIENT, RADIAL_GRADIENT, SWEEP_GRADIENT)
annotation class GradientType {
    companion object {
        const val NO_GRADIENT = 0
        const val LINEAR_GRADIENT = 1
        const val RADIAL_GRADIENT = 2
        const val SWEEP_GRADIENT = 3
    }
}

@Retention(AnnotationRetention.SOURCE)
@IntDef(BUT, ROUND, SQUARE)
annotation class CapType {
    companion object {
        const val BUT = 0
        const val ROUND = 1
        const val SQUARE = 2
    }
}