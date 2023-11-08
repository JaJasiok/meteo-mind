package com.example.meteomind

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class HourlyPrecipitationAdapter(private var list: List<Int>) : RecyclerView.Adapter<HourlyPrecipitationAdapter.ViewHolder>() {

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

//        cardView.findViewById<TextView>(R.id.temp_text).text = "21°C"
//
//        cardView.findViewById<ImageView>(R.id.hourly_image).setImageDrawable(getDrawableByName(cardView.context, "cloud.xml"))
//
//        cardView.findViewById<TextView>(R.id.card_hour).text = "12:00"

        cardView.setOnClickListener {
            listener?.onClick(position)
        }
    }

    override fun getItemCount(): Int {
        return list.size
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
