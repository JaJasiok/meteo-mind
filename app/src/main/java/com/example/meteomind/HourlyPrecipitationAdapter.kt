package com.example.meteomind

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class HourlyPrecipitationAdapter(private var weatherData: WeatherData) :
    RecyclerView.Adapter<HourlyPrecipitationAdapter.ViewHolder>() {

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
            .inflate(R.layout.card_precipitation, parent, false) as CardView
        return ViewHolder(cv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView

        var precipitationValue = ""

        when(weatherData.timestamps[position].values.tp){
            0.0 -> {}
            in 0.0..0.25 -> precipitationValue = "<0.25"
            else -> precipitationValue = String.format(Locale.US, "%.1f", weatherData.timestamps[position].values.tp)        }

        cardView.findViewById<TextView>(R.id.precipitation_value).text = precipitationValue

        var precipitationImage = "precipitation1"

        when(weatherData.timestamps[position].values.tp){
            in 0.0..0.25 -> { }
            in 0.25..0.5 -> precipitationImage = "precipitation2"
            in 0.5..1.0 -> precipitationImage = "precipitation3"
            else -> precipitationImage = "precipitation4"
        }

        cardView.findViewById<ImageView>(R.id.precipitation_image).setImageDrawable(getDrawableByName(cardView.context, precipitationImage))

        cardView.findViewById<TextView>(R.id.precipitation_hour).text = formatHour(weatherData.timestamps[position].timestamp)
        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.timestamps.size
    }
}
