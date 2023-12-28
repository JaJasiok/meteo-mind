package com.example.meteomind

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.random.Random

class ParticleSystem(private val context: Context) {
    private val particles = mutableListOf<Particle>()
    private val random = Random

    private var ax = 0f
    private var ay = 0f

    fun setAcceleration(ax: Float, ay: Float) {
        Log.d("ParticleSystem", "Acceleration set to $ax, $ay")
        this.ax = ax
        this.ay = ay
    }

    fun emitSnowflake(x: Float) {
        val vx = (random.nextFloat() * 2 - 1) * 0.6F // Small random value for slight horizontal drift
        val vy = (random.nextFloat() * 2 - 1) * 4F
        val life = random.nextInt(100) + 50
        val size = random.nextFloat() * 20 // Random size between 0 and 20
        val drawable = ContextCompat.getDrawable(context, R.drawable.snowflake_white)
        particles.add(Snowflake(x, 0f, vx - ax, vy - ay, life, size, drawable)) // Add acceleration to velocity
    }

    fun emitWaterDrop(x: Float) {
        val vx = (random.nextFloat() * 2 - 1) * 0.2F // Small random value for slight horizontal drift
        val vy = (random.nextFloat() * 2 - 1) * 1F + 6F // Random vertical velocity between 5 and 7
        val life = random.nextInt(50) + 20
        val size = random.nextFloat() * 20 // Random size between 0 and 20
        val drawable = ContextCompat.getDrawable(context, R.drawable.drop)
        particles.add(WaterDrop(x, 0f, vx - ax, vy - ay, life, size, drawable)) // Add acceleration to velocity
    }

    fun update() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.update(ax, ay)
            if (!p.isAlive()) {
                iterator.remove()
            }
        }
    }

    fun getParticles() = particles
}

open class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Int,
    var size: Float,
    var drawable: Drawable?
) {
    open fun update(ax: Float, ay: Float) {
        if (abs(vx) < 5) vx += ax
        if (abs(vy) < 5) vy += ay
        x += vx
        y += vy
        life--
    }

    fun isAlive() = life > 0
}

class Snowflake(x: Float, y: Float, vx: Float, vy: Float, life: Int, size: Float, drawable: Drawable?)
    : Particle(x, y, vx, vy, life, size, drawable) {
    // Specific properties and methods for Snowflake
}

class WaterDrop(x: Float, y: Float, vx: Float, vy: Float, life: Int, size: Float, drawable: Drawable?)
    : Particle(x, y, vx, vy, life, size, drawable) {
    // Specific properties and methods for WaterDrop
}