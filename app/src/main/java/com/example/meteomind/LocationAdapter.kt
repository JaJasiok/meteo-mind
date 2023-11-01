package com.example.meteomind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(private val locations: List<Location>) :
    RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    private var listener: Listener? = null
    private var removeLocationClickListener: RemoveLocationClickListener? = null

    interface Listener {
        fun onClick(position: Int)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    interface RemoveLocationClickListener {
        fun onRemoveLocationClick(position: Int)
    }

    fun setRemoveLocationClickListener(listener: RemoveLocationClickListener) {
        this.removeLocationClickListener = listener
    }

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val locationNameTextView: TextView = view.findViewById(R.id.locationName)
        val removeLocationTextView: TextView = view.findViewById(R.id.removeLocation)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): LocationViewHolder {
        // Create a new view, which defines the UI of the location item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.visited_location, viewGroup, false)

        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: LocationViewHolder, position: Int) {
        viewHolder.locationNameTextView.text = locations[position].locationName

        viewHolder.itemView.setOnClickListener {
            listener?.onClick(position /*, locationName*/)
        }

        viewHolder.removeLocationTextView.setOnClickListener {
            removeLocationClickListener?.onRemoveLocationClick(position)
        }
    }

    override fun getItemCount() = locations.size
}