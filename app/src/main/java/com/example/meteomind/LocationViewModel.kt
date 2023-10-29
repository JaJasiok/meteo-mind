package com.example.meteomind

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class Location(val locationName: String, val paceId: String = "")

class LocationViewModel : ViewModel() {
    private val locationList = MutableLiveData<List<Location>>()

    init {
        // Initialize the LiveData with some initial data
        locationList.value = emptyList()
    }

    fun getLocations(): LiveData<List<Location>> {
        return locationList
    }

    fun deleteAllLocations() {
        // Clear the location list
        locationList.value = emptyList()
    }

    fun fillLocationsWithNewData(newData: List<Location>) {
        // Fill the location list with new data
        locationList.value = newData
    }

    fun getLocationCount(): Int {
        // Return the size of the itemList
        return locationList.value?.size ?: 0
    }
}