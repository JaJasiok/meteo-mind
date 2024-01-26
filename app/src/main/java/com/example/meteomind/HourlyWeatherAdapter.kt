package com.example.meteomind

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import dev.zotov.phototime.solarized.Solarized
import java.time.LocalDateTime

class HourlyWeatherAdapter(private var weatherData: WeatherData) :
    RecyclerView.Adapter<HourlyWeatherAdapter.ViewHolder>() {

    private var listener: Listener? = null

    interface Listener {
        fun onClick(position: Int)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    class ViewHolder(itemView: CardView) : RecyclerView.ViewHolder(itemView) {
        var cardView: CardView = itemView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val cv = LayoutInflater.from(parent.context)
            .inflate(R.layout.card_weather, parent, false) as CardView
        return ViewHolder(cv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView

        val timestamp = weatherData.timestamps[position].timestamp

        val t2m = weatherData.timestamps[position].values.t2m
        val sp = weatherData.timestamps[position].values.sp
        val w10 = weatherData.timestamps[position].values.u10
        val v10 = weatherData.timestamps[position].values.v10
        val tcc = weatherData.timestamps[position].values.tcc
        val tp = weatherData.timestamps[position].values.tp

        val localDateTime = LocalDateTime.parse(timestamp)
//        val localDateTime = LocalDateTime.now()

        val sunrise = Solarized(weatherData.lat, weatherData.lng, LocalDateTime.now()).sunrise?.date
        val sunset = Solarized(weatherData.lat, weatherData.lng, LocalDateTime.now()).sunset?.date

        cardView.findViewById<TextView>(R.id.hourly_temp).text = t2m.toInt().toString()

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
//
        cardView.findViewById<ImageView>(R.id.hourly_image).setImageDrawable(getDrawableByName(cardView.context, weatherImageFile))
//
        cardView.findViewById<TextView>(R.id.hourly_hour).text = formatHour(timestamp)

        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.timestamps.size
    }
}
