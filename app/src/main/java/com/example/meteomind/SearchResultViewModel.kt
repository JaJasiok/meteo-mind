package com.example.meteomind

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchResult(var locationName: String, var placeId: String = "", var locationLat: Double = 0.0, var locationLng: Double = 0.0)

class SearchResultViewModel : ViewModel() {
    private val searchResultList = MutableLiveData<List<SearchResult>>()

    init {
        // Initialize the LiveData with some initial data
        searchResultList.value = emptyList()
    }

    fun getResults(): LiveData<List<SearchResult>> {
        return searchResultList
    }

    fun deleteResults() {
        // Clear the location list
        searchResultList.value = emptyList()
    }

    fun replaceResults(newData: List<SearchResult>) {
        // Fill the location list with new data
        searchResultList.value = newData
    }

    fun getResultCount(): Int {
        // Return the size of the itemList
        return searchResultList.value?.size ?: 0
    }
}