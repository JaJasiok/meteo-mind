package com.example.meteomind

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var searchResultAdapter = SearchResultAdapter(emptyList())
    private lateinit var searchResultViewModel: SearchResultViewModel
    private lateinit var searchView: SearchView
    private val locationViewModel: LocationViewModel by viewModels {
        LocationModelFactory((this.application as MyApplication).repository)
    }
    private lateinit var locationAdapter: LocationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        val applicationInfo: ApplicationInfo = application.packageManager
            .getApplicationInfo(application.packageName, PackageManager.GET_META_DATA)
        val apiKey = applicationInfo.metaData["MAPS_API_KEY"].toString()

        Places.initialize(this, apiKey, Locale.US)
        placesClient = Places.createClient(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val searchBar = binding.searchBar
        setSupportActionBar(searchBar)

        searchResultViewModel = ViewModelProvider(this)[SearchResultViewModel::class.java]

        val mapsButton = binding.weatherView.mapsButton
        mapsButton.setOnClickListener {
            val intent = Intent(this@MainActivity, MapsActivity::class.java)
            startActivity(intent)
        }

        val recyclerView = binding.recyclerView
//        recyclerView.adapter = searchResultAdapter

        searchView = binding.searchView

        locationViewModel.locations.observe(this){ locations ->
            locationAdapter = LocationAdapter(locations).apply {
                setListener(object : LocationAdapter.Listener {
                    override fun onClick(position: Int) {
                        val location = locations[position]
                        searchBar.setText(location.locationName)
                        searchView.hide()
                    }
                })
                setRemoveLocationClickListener(object : LocationAdapter.RemoveLocationClickListener{
                    override fun onRemoveLocationClick(position: Int) {
                        locationViewModel.deleteLocation(locations[position])
                    }
                })
            }
            recyclerView.adapter = locationAdapter
        }

        searchResultViewModel.getResults().observe(this) { results ->
            searchResultAdapter = SearchResultAdapter(results)
            searchResultAdapter.apply {
                setListener(object: SearchResultAdapter.Listener{
                    override fun onClick(position: Int) {
                        val result = results[position]
                        getPlaceByResult(result)
                        searchBar.setText(result.locationName)
                        recyclerView.adapter = locationAdapter
                        searchView.hide()
                        searchResultViewModel.deleteResults()

                    }
                })
            }
        }


        searchView
            .editText
            .doOnTextChanged { text, start, before, count ->
                if(text.isNullOrEmpty()){
                    searchResultViewModel.deleteResults()
                    recyclerView.adapter =  locationAdapter
                }
                else{
                    recyclerView.adapter = searchResultAdapter
                    placesAutocomplete(text.toString())
                }
            }

        val divider = MaterialDividerItemDecoration(this, LinearLayoutManager.VERTICAL /*or LinearLayoutManager.HORIZONTAL*/)
        divider.isLastItemDecorated = false
        recyclerView.addItemDecoration(divider)

        val hourlyWeatherRecyclerView = binding.weatherView.hourlyWeather

        hourlyWeatherRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        val hourlyWeatherAdapter = HourlyWeatherAdapter(listOf(1,2,3,4,5,6,7,8))
        hourlyWeatherRecyclerView.adapter = hourlyWeatherAdapter



        val hourlyDetailsRecyclerView = binding.weatherView.hourlyDetails
        hourlyDetailsRecyclerView.layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.HORIZONTAL, false)
        val hourlyPrecipitationAdapter = HourlyPrecipitationAdapter(listOf(1,2,3,4,5,6,7,8))
        val hourlyWindAdapter = HourlyWindAdapter(listOf(1,2,3,4,5,6,7,8))

        val toggleButton = binding.weatherView.toggleButton
        toggleButton.addOnButtonCheckedListener { toggleButton, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.button_precipitation -> {
                        hourlyDetailsRecyclerView.adapter = hourlyPrecipitationAdapter
                    }
                    R.id.button_wind -> {
                        hourlyDetailsRecyclerView.adapter = hourlyWindAdapter
                    }
                }
            }
        }

        toggleButton.check(R.id.button_precipitation)
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
                searchResultViewModel.deleteResults()
                if(searchView.text.isNotEmpty()){
                    val newSearchResults = emptyList<SearchResult>().toMutableList()
                    for (i in 0 until minOf(5, response.autocompletePredictions.size)) {
                        newSearchResults += SearchResult(response.autocompletePredictions[i].getPrimaryText(null).toString(),
                            response.autocompletePredictions[i].placeId)
                    }
                    searchResultViewModel.replaceResults(newSearchResults)
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

    private fun getPlaceByResult(result: SearchResult){

        val placeFields = listOf(Place.Field.ID, Place.Field.LAT_LNG)

        val request = FetchPlaceRequest.newInstance(result.paceId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                locationViewModel.addLocation(
                    Location(
                        result.locationName,
                        place.id ?: "",
                        place.latLng?.latitude ?: 0.0,
                        place.latLng?.longitude ?: 0.0,
                        System.currentTimeMillis()
                    )
                )
                Toast.makeText(this, place.latLng?.toString() ?: "", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }

    }
}