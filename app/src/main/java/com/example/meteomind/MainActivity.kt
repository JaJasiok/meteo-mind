package com.example.meteomind

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meteomind.databinding.ActivityMainBinding
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.google.android.material.search.SearchView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var placesClient: PlacesClient
    private lateinit var suggestedLocationAdapter: SuggestedLocationAdapter
    private lateinit var locationViewModel: LocationViewModel
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        Places.initialize(this, "AIzaSyA-WCq8dGUwASP8hJvLCl0-B5we_XxuYtE", Locale.US)
        placesClient = Places.createClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchBar = binding.searchBar
        setSupportActionBar(searchBar)

        locationViewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        val recyclerView = binding.recyclerView
        suggestedLocationAdapter = SuggestedLocationAdapter(emptyList())
        recyclerView.adapter = suggestedLocationAdapter

        searchView = binding.searchView

        locationViewModel.getLocations().observe(this) { locations ->
            suggestedLocationAdapter = SuggestedLocationAdapter(locations)
            suggestedLocationAdapter.apply {
                setListener(object: SuggestedLocationAdapter.Listener{
                    override fun onClick(position: Int) {
                        val location = locations[position]
                        getPlaceById(location.paceId)
                        searchBar.setText(location.locationName)
                        searchView.hide();
                    }
                })
            }
            recyclerView.adapter = suggestedLocationAdapter
        }

        searchView
            .editText
            .doOnTextChanged { text, start, before, count ->
                if (!text.isNullOrEmpty()){
                    placesAutocomplete(text.toString())
                }
            }
        searchView
            .editText
            .doAfterTextChanged { text ->
                if(text.isNullOrEmpty()){
                    locationViewModel.deleteAllLocations()
                }
            }

        val divider = MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL /*or LinearLayoutManager.HORIZONTAL*/)
        divider.isLastItemDecorated = false
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
                // setLocationBias(bounds)
                .setLocationRestriction(bounds)
                .setOrigin(LatLng(-33.8749937, 151.2041382))
                .setCountries("PL")
                .setTypesFilter(listOf(PlaceTypes.CITIES))
                .setSessionToken(token)
                .setQuery(query)
                .build()
        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response: FindAutocompletePredictionsResponse ->
                locationViewModel.deleteAllLocations()
                if(searchView.text.isNotEmpty()){
                    val newLocations = emptyList<Location>().toMutableList()
                    for (i in 0 until minOf(5, response.autocompletePredictions.size)) {
                        newLocations += Location(response.autocompletePredictions[i].getPrimaryText(null).toString(),
                            response.autocompletePredictions[i].placeId)
                    }
                    locationViewModel.fillLocationsWithNewData(newLocations)
                }
//                for (prediction in response.autocompletePredictions) {
//                    Log.i(TAG, prediction.placeId)
//                    Log.i(TAG, prediction.getPrimaryText(null).toString())
//                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.statusCode}")
                }
            }
    }

    private fun getPlaceById(placeId: String){

        val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)

        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Toast.makeText(this, place.latLng?.toString() ?: "", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }

    }
}