package com.example.meteomind

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat

class ParticleView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private lateinit var particleSystem: ParticleSystem

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (particle in particleSystem.getParticles()) {
            particle.drawable?.setBounds(
                (particle.x - particle.size).toInt(),
                (particle.y - particle.size).toInt(),
                (particle.x + particle.size).toInt(),
                (particle.y + particle.size).toInt()
            )
            particle.drawable?.draw(canvas)
        }

        // Invalidate the view to cause a redraw
        invalidate()
    }

    fun setParticleSystem(particleSystem: ParticleSystem) {
        this.particleSystem = particleSystem
    }
}