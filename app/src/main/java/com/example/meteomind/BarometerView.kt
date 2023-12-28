package com.example.meteomind

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class BarometerView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val paint = Paint()
    private val progressPaint = Paint()
    private val trianglePaint = Paint()
    private val rect = RectF()
    private var sweepAngle: Float = 270f  // Set a fixed value for sweepAngle
    private var progressSweepAngle: Float = 0f

    init {
        paint.isAntiAlias = true
        paint.color = 0xFFCEE4FF.toInt() // Set the color to light blue
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 30f // Increase the thickness of the arc
        paint.strokeCap = Paint.Cap.ROUND // Round the start and end of the arc

        progressPaint.isAntiAlias = true
        progressPaint.color = 0xFF5FB1F8.toInt() // Set the color to blue for the progress arc
        progressPaint.style = Paint.Style.STROKE
        progressPaint.strokeWidth = 30f // Increase the thickness of the arc
        progressPaint.strokeCap = Paint.Cap.ROUND // Round the start and end of the arc

        trianglePaint.isAntiAlias = true
        trianglePaint.color = 0xFF000000.toInt() // Set the color to black for the triangle
        trianglePaint.style = Paint.Style.FILL
    }

override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)

    val width = width
    val height = height

    val diameter = if (width < height) width.toFloat() else height.toFloat()
    val pad = paint.strokeWidth / 2  // Adjust the padding to be at least half of the stroke width

    rect.set(pad, pad, diameter - pad, diameter - pad)

    // Draw the range arc
    canvas.drawArc(rect, -225f, sweepAngle, false, paint)

    // Draw the progress arc
    canvas.drawArc(rect, -225f, progressSweepAngle, false, progressPaint)

    // Calculate the position of the triangle
    val radius = diameter / 2 * 0.5  // Multiply the radius by 0.8 to move the triangle closer to the center
    val angle = Math.toRadians((-225 + progressSweepAngle).toDouble())
    val triangleX = (width / 2 + radius * cos(angle)).toFloat()
    val triangleY = (height / 2 + radius * sin(angle)).toFloat()

    // Draw the triangle
    val trianglePath = Path()
    val triangleSize = diameter / 10  // Adjust the size of the triangle as needed
    trianglePath.moveTo(0f, -triangleSize / 2)
    trianglePath.lineTo(-triangleSize / 2, triangleSize / 2)
    trianglePath.lineTo(triangleSize / 2, triangleSize / 2)
    trianglePath.close()
    trianglePaint.color = 0xFFC5C5C9.toInt() // Set the color to light purple

    // Save the current state of the canvas
    canvas.save()

    // Translate and rotate the canvas
    canvas.translate(triangleX, triangleY)
    canvas.rotate((-225 + progressSweepAngle + 90))  // Add 90 to the rotation angle

    // Draw the triangle on the rotated canvas
    canvas.drawPath(trianglePath, trianglePaint)

    // Restore the canvas to its original state
    canvas.restore()
}

    fun setProgress(progress: Float) {
        progressSweepAngle = 270 * progress
        invalidate()
    }
}