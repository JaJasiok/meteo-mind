package com.example.meteomind

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
class SearchResultAdapter(private val searchResultList: List<SearchResult>) :
    RecyclerView.Adapter<SearchResultAdapter.SearchResultViewHolder>() {

    private var listener: Listener? = null

    interface Listener {
        fun onClick(position: Int)
    }

    fun setListener(listener: Listener?) {
        this.listener = listener
    }

    class SearchResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.result)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): SearchResultViewHolder {
        // Create a new view, which defines the UI of the location item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.suggested_location, viewGroup, false)

        return SearchResultViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: SearchResultViewHolder, position: Int) {

        viewHolder.textView.text = searchResultList[position].locationName

        viewHolder.itemView.setOnClickListener{
            listener?.onClick(position)
        }
    }

    override fun getItemCount() = searchResultList.size
}
