package com.example.meteomind

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.VectorDrawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.cos

class SunPositionView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val pathPaint = Paint()
    private val path = Path()

    init {
        paint.isAntiAlias = true
        paint.color = 0xFFFFC208.toInt()
        paint.style = Paint.Style.FILL

        pathPaint.isAntiAlias = true
        pathPaint.color = 0xFF84888C.toInt()
        pathPaint.style = Paint.Style.STROKE
        pathPaint.strokeWidth = 3f
    }

    private var sunPositionRatio: Float = 0f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width
        val height = height

        val sunRadius = width / 25f
        val sunX = map(
            sunPositionRatio,
            (-Math.PI).toFloat(),
            Math.PI.toFloat(),
            0.0,
            width.toDouble()
        ).toFloat()
        val sunY =
            height - (cos(sunPositionRatio.toDouble()) * height / 3 + height / 2).toFloat()

        // Draw the path of the sun
        path.reset()
        for (i in 0 until width) {
            val x = i.toFloat()
            val y = height - (cos(
                map(
                    i.toFloat() / width,
                    0f,
                    1f,
                    -Math.PI,
                    Math.PI
                )
            ) * height / 3 + height / 2).toFloat()
            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        canvas.drawPath(path, pathPaint)

        // Draw the horizon line
        canvas.drawLine(0f, height / 2f, width.toFloat(), height / 2f, pathPaint)

        // Draw the sun
        val sunBitmap = getBitmapFromVectorDrawable(context, R.drawable.sun)
        val scaledSunBitmap = Bitmap.createScaledBitmap(
            sunBitmap,
            2 * sunRadius.toInt(),
            2 * sunRadius.toInt(),
            false
        )
        canvas.drawBitmap(scaledSunBitmap, sunX - sunRadius, sunY - sunRadius, null)
    }

    private fun map(
        value: Float,
        start1: Float,
        stop1: Float,
        start2: Double,
        stop2: Double
    ): Double {
        return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1))
    }

    fun setSunPosition(currentTime: LocalDateTime, sunrise: LocalDateTime, sunset: LocalDateTime) {
        val totalDuration = ChronoUnit.MINUTES.between(sunrise, sunset).toFloat()
        val elapsedDuration = ChronoUnit.MINUTES.between(sunrise, currentTime).toFloat()

        sunPositionRatio = when {
            elapsedDuration < 0 -> map(
                elapsedDuration,
                -totalDuration,
                0f,
                -Math.PI,
                -Math.PI / 2
            ).toFloat() // Before sunrise
            elapsedDuration > totalDuration -> map(
                elapsedDuration,
                totalDuration,
                2 * totalDuration,
                Math.PI / 2,
                Math.PI
            ).toFloat() // After sunset
            else -> map(
                elapsedDuration,
                0f,
                totalDuration,
                -Math.PI / 2,
                Math.PI / 2
            ).toFloat() // Between sunrise and sunset
        }

        invalidate()
    }

    fun getBitmapFromVectorDrawable(context: Context, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context, drawableId)
        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        } else if (drawable is VectorDrawable) {
            val bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            return bitmap
        } else {
            throw IllegalArgumentException("unsupported drawable type")
        }
    }
}