package com.example.meteomind

import android.util.Log
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

        val sunrise = Solarized(weatherData.lat, weatherData.lng, localDateTime).sunrise?.date
        val sunset = Solarized(weatherData.lat, weatherData.lng, localDateTime).sunset?.date

        cardView.findViewById<TextView>(R.id.hourly_temp).text = t2m.toInt().toString()

        val weatherImageFile: String = getWeatherImageName(
            weatherData.timestamps[position].values, localDateTime,
            sunrise!!,
            sunset!!
        )

        cardView.findViewById<ImageView>(R.id.hourly_image)
            .setImageDrawable(getDrawableByName(cardView.context, weatherImageFile))

        cardView.findViewById<TextView>(R.id.hourly_hour).text = formatHour(timestamp)

        Log.i("HourlyWeatherAdapter", "sunrise: $sunrise, sunset: $sunset, localDateTime: $localDateTime")

        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.timestamps.size
    }
}
