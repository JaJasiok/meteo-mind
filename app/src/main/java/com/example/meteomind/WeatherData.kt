package com.example.meteomind

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherData(
    @Json(name = "lat") val lat: Double,
    @Json(name = "lng") val lng: Double,
    @Json(name = "timestamps") val timestamps: List<Timestamp>
) : Parcelable

@Parcelize
data class Timestamp(
    @Json(name = "timestamp") val timestamp: String,
    @Json(name = "values") val values: Values
) : Parcelable

@Parcelize
data class Values(
    @Json(name = "sp") val sp: Double,
    @Json(name = "tcc") val tcc: Double,
    @Json(name = "tp") val tp: Double,
    @Json(name = "u10") val u10: Double,
    @Json(name = "v10") val v10: Double,
    @Json(name = "t2m") val t2m: Double
) : Parcelable