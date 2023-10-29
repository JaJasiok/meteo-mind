package com.example.meteomind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SuggestedLocationAdapter(private val locationList: List<Location>) :
    RecyclerView.Adapter<SuggestedLocationAdapter.LocationViewHolder>() {

    private var listener: Listener? = null

    interface Listener {
        fun onClick(position: Int)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    class LocationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.textView)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): LocationViewHolder {
        // Create a new view, which defines the UI of the location item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.text_row_item, viewGroup, false)

        return LocationViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(viewHolder: LocationViewHolder, position: Int) {

        // Get location from location list at this position and replace the
        // contents of the view with that location
        viewHolder.textView.text = locationList[position].locationName

        viewHolder.itemView.setOnClickListener{
            listener?.onClick(position)
        }
    }

    // Return the size of location list (invoked by the layout manager)
    override fun getItemCount() = locationList.size

}
