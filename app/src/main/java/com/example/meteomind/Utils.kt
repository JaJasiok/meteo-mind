package com.example.meteomind

import kotlin.math.atan2

fun formatHour(timestamp: String) : String {

    val hour = timestamp.substring(11, 13).toInt()
    val minute = timestamp.substring(14, 16)
    val formattedHour = if (hour == 0 && minute == "00") "24" else hour.toString()

    return "$formattedHour:$minute"
}

fun calculateWindDirection(u: Double, v: Double): Float {
    // Calculate the wind direction in radians
    val windDirectionRadians = atan2(u, v)

    // Convert to degrees
    var windDirectionDegrees = Math.toDegrees(windDirectionRadians)

    // Adjust the wind direction degrees
    if (windDirectionDegrees < 0) {
        windDirectionDegrees += 360.0
    }

    return windDirectionDegrees.toFloat()
}

fun getWindDirection(u: Double, v: Double): String {
    var degrees = calculateWindDirection(u, v)

    degrees += 180
    if (degrees > 360) {
        degrees -= 360
    }

    return when (degrees) {
        in 337.5..360.0, in 0.0..22.5 -> "north"
        in 22.5..67.5 -> "northeast"
        in 67.5..112.5 -> "east"
        in 112.5..157.5 -> "southeast"
        in 157.5..202.5 -> "south"
        in 202.5..247.5 -> "southwest"
        in 247.5..292.5 -> "west"
        in 292.5..337.5 -> "northwest"
        else -> "Unknown"
    }
}