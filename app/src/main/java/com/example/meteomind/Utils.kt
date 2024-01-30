package com.example.meteomind

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.ContextCompat
import java.time.LocalDateTime
import kotlin.math.atan2

fun formatHour(timestamp: String) : String {

    val hour = timestamp.substring(11, 13).toInt()
    val minute = timestamp.substring(14, 16)
    val formattedHour = hour.toString()

    return "$formattedHour:$minute"
}

fun calculateWindDirection(u: Double, v: Double): Float {
    val windDirectionRadians = atan2(u, v)

    var windDirectionDegrees = Math.toDegrees(windDirectionRadians)

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

fun scalePressure(value: Double): Float {
    val min = 900f
    val max = 1100f

    if(value < min) return 0.01f
    if(value > max) return 1f

    return ((value - min) / (max - min)).toFloat()
}

fun getDrawableByName(context: Context, drawableName: String): Drawable? {
    val resourceId = context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    return if (resourceId != 0) {
        ContextCompat.getDrawable(context, resourceId)
    } else {
        null
    }
}

fun getWeatherImageName(data: Values, localDateTime: LocalDateTime, sunrise: LocalDateTime, sunset: LocalDateTime): String{

    val t2m = data.t2m
    val tcc = data.tcc
    val tp = data.tp

    var weatherImageFile: String
    if (tp < 0.1){
        if(tcc < 0.2) {
            weatherImageFile = if(localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                "sun"
            } else {
                "moon"
            }
        } else if (tcc < 0.35) {
            weatherImageFile = if (localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                "cloud_light_sun"
            } else {
                "cloud_light_moon"
            }
        } else if (tcc < 0.5) {
            weatherImageFile = if (localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                "cloud_grey_sun"
            } else {
                "cloud_grey_moon"
            }
        } else if (tcc < 0.75) {
            weatherImageFile = "cloud_grey"
        } else {
            weatherImageFile = "cloud_dark"
        }
    } else
        if (t2m < 0) {
            weatherImageFile = if (tcc < 0.5) {
                if (localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                    "cloud_grey_sun_snow"
                } else {
                    "cloud_grey_moon_snow"
                }
            } else if (tcc < 0.75) {
                "cloud_grey_snow"
            } else {
                "cloud_dark_snow1"
            }
            if(tp > 3.0){
                weatherImageFile = "cloud_dark_snow2"
            }
        } else if (t2m > 2) {
            weatherImageFile = if (tcc < 0.5) {
                if (localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                    "cloud_grey_sun_rain"
                } else {
                    "cloud_grey_moon_rain"
                }
            } else if (tcc < 0.75) {
                "cloud_grey_rain"
            } else {
                "cloud_dark_rain1"
            }
            if(tp > 3.0){
                weatherImageFile = "cloud_dark_rain2"
            }
        } else {
            weatherImageFile = if (tcc < 0.5) {
                if (localDateTime.isAfter(sunrise) && localDateTime.isBefore(sunset)){
                    "cloud_grey_moon_rain_snow"
                } else {
                    "cloud_grey_sun_rain_snow"
                }
            } else{
                "cloud_grey_rain_snow"
            }
            if(tp > 3.0){
                weatherImageFile = "cloud_dark_rain_snow"
            }
        }

    return weatherImageFile
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}