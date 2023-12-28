package com.example.meteomind

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

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

        cardView.findViewById<TextView>(R.id.hourly_temp).text = weatherData.timestamps[position].values.t2m.toInt().toString() + "°C"
//
        cardView.findViewById<ImageView>(R.id.hourly_image).setImageDrawable(getDrawableByName(cardView.context, "cloud.xml"))
//
        cardView.findViewById<TextView>(R.id.hourly_hour).text = formatHour(weatherData.timestamps[position].timestamp)

        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.timestamps.size
    }
}
