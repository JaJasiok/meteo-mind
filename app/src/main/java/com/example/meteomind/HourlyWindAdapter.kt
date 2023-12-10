package com.example.meteomind

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.pow
import kotlin.math.sqrt

class HourlyWindAdapter(private var weatherData: WeatherData) :
    RecyclerView.Adapter<HourlyWindAdapter.ViewHolder>() {

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
            .inflate(R.layout.card_wind, parent, false) as CardView
        return ViewHolder(cv)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cardView = holder.cardView

        cardView.findViewById<ImageView>(R.id.wind_image).rotation = calculateWindDirection(weatherData.timestamps[position].values.u10, weatherData.timestamps[position].values.v10)

        val speed = sqrt(weatherData.timestamps[position].values.u10.pow(2) + weatherData.timestamps[position].values.v10.pow(2))

        cardView.findViewById<TextView>(R.id.wind_value).text = speed.toInt().toString()

        cardView.findViewById<TextView>(R.id.wind_hour).text = formatHour(weatherData.timestamps[position].timestamp)
        cardView.setOnClickListener {
            listener?.onClick(position)
        }

        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return weatherData.timestamps.size
    }

    fun getDrawableByName(context: Context, drawableName: String): Drawable? {
        val resources = context.resources
        val packageName = context.packageName
        val resourceId = resources.getIdentifier(drawableName, "drawable", packageName)
        return if (resourceId != 0) {
            resources.getDrawable(resourceId, null)
        } else {
            null
        }
    }
}
