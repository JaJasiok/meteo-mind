package com.example.meteomind

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meteomind.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.divider.MaterialDividerItemDecoration
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Places.initialize(this, "AIzaSyA-WCq8dGUwASP8hJvLCl0-B5we_XxuYtE", Locale.US)
        placesClient = Places.createClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchBar = binding.searchBar
        setSupportActionBar(searchBar)

        val searchView = binding.searchView
        searchView
            .editText
            .setOnEditorActionListener { v: TextView?, actionId: Int, event: KeyEvent? ->

//                placesAutocomplete(searchView.text.toString())
                false
            }

        val dataset = arrayOf("January", "February", "March")
        val customAdapter = CustomAdapter(dataset)

        val recyclerView = binding.recyclerView
        recyclerView.adapter = customAdapter

        val divider = MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL /*or LinearLayoutManager.HORIZONTAL*/)
        divider.setLastItemDecorated(false)
        recyclerView.addItemDecoration(divider)


    }

    private fun placesAutocomplete(query: String){
        // Create a new token for the autocomplete session. Pass this to FindAutocompletePredictionsRequest,
        // and once again when the user makes a selection (for example when calling fetchPlace()).
        val token = AutocompleteSessionToken.newInstance()

        // Create a RectangularBounds object.
        val bounds = RectangularBounds.newInstance(
            LatLng(49.29899, 14.24712),
            LatLng(54.79086, 23.89251)
        )
        // Use the builder to create a FindAutocompletePredictionsRequest.
        val request =
            FindAutocompletePredictionsRequest.builder()
                // Call either setLocationBias() OR setLocationRestriction().
                .setLocationRestriction(bounds)
                //.setLocationRestriction(bounds)
                .setOrigin(LatLng(-33.8749937, 151.2041382))
                .setCountries("PL")
                .setTypesFilter(listOf(PlaceTypes.CITIES))
                .setSessionToken(token)
                .setQuery(query)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                for (prediction in response.autocompletePredictions) {
                    Log.i(TAG, prediction.placeId)
                    Log.i(TAG, prediction.getPrimaryText(null).toString())
                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }


    }
}