package com.example.meteomind

import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.meteomind.databinding.FragmentWeatherBinding
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
import com.google.android.material.search.SearchView.TransitionState
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt


class WeatherFragment : Fragment(R.layout.fragment_weather) {

    private var _binding: FragmentWeatherBinding? = null
    private val binding get() = _binding!!

    private lateinit var placesClient: PlacesClient
    private lateinit var searchResultAdapter: SearchResultAdapter
    private lateinit var searchResultViewModel: SearchResultViewModel
    private lateinit var searchView: SearchView
    private val locationViewModel: LocationViewModel by viewModels {
        LocationModelFactory((requireActivity().application as MyApplication).repository)
    }
    private lateinit var locationAdapter: LocationAdapter

    private lateinit var weatherData: WeatherData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val applicationInfo: ApplicationInfo = requireActivity().application.packageManager
            .getApplicationInfo(
                requireActivity().application.packageName,
                PackageManager.GET_META_DATA
            )
        val apiKey = applicationInfo.metaData["MAPS_API_KEY"].toString()

        Places.initialize(requireContext(), apiKey, Locale.US)
        placesClient = Places.createClient(requireContext())

        val searchBar = binding.searchBar
//        setSupportActionBar(searchBar)

        searchResultViewModel = ViewModelProvider(this)[SearchResultViewModel::class.java]

        val recyclerView = binding.recyclerView
//        recyclerView.adapter = searchResultAdapter

        searchView = binding.searchView

        locationViewModel.locations.observe(requireActivity()) { locations ->
            locationAdapter = LocationAdapter(locations).apply {
                setListener(object : LocationAdapter.Listener {
                    override fun onClick(position: Int) {
                        val location = locations[position]
                        searchBar.setText(location.locationName)
                        searchView.hide()
                    }
                })
                setRemoveLocationClickListener(object :
                    LocationAdapter.RemoveLocationClickListener {
                    override fun onRemoveLocationClick(position: Int) {
                        locationViewModel.deleteLocation(locations[position])
                    }
                })
            }
            recyclerView.adapter = locationAdapter
        }

        searchResultViewModel.getResults().observe(requireActivity()) { results ->
            searchResultAdapter = SearchResultAdapter(results)
            searchResultAdapter.apply {
                setListener(object : SearchResultAdapter.Listener {
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

        searchView.addTransitionListener { searchView: SearchView?, previousState: TransitionState?, newState: TransitionState ->
            val tabLayout = (activity as MainActivity).tabLayout
            if (newState == TransitionState.SHOWING) {
                tabLayout.visibility = View.GONE
            } else if (newState == TransitionState.HIDING) {
                tabLayout.visibility = View.VISIBLE
            }
        }

        searchView
            .editText
            .doOnTextChanged { text, start, before, count ->
                if (text.isNullOrEmpty()) {
                    searchResultViewModel.deleteResults()
                    recyclerView.adapter = locationAdapter
                } else {
                    recyclerView.adapter = searchResultAdapter
                    placesAutocomplete(text.toString())
                }
            }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        recyclerView.addItemDecoration(divider)

        if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            weatherData = arguments?.getParcelable("weatherData", WeatherData::class.java)!!
        }else{
            weatherData = arguments?.getParcelable<WeatherData>("weatherData")!!
        }

        val tempText = binding.weatherView.tempText
        tempText.text = weatherData.timestamps[0].values.t2m.toInt().toString() + "°C"

        val hourlyWeatherRecyclerView = binding.weatherView.hourlyWeather

        hourlyWeatherRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val hourlyWeatherAdapter = HourlyWeatherAdapter(weatherData)
        hourlyWeatherRecyclerView.adapter = hourlyWeatherAdapter

        val windArrow = binding.weatherView.windArrow
        windArrow.rotation = calculateWindDirection(weatherData.timestamps[0].values.u10, weatherData.timestamps[0].values.v10)
        val windDirection = binding.weatherView.windDirection
        windDirection.text = getWindDirection(weatherData.timestamps[0].values.u10, weatherData.timestamps[0].values.v10)
        val windValue = binding.weatherView.windValue
        windValue.text = sqrt(weatherData.timestamps[0].values.u10.pow(2) + weatherData.timestamps[0].values.v10.pow(2)).toInt().toString() + " km/h"

        val hourlyDetailsRecyclerView = binding.weatherView.hourlyDetails
        hourlyDetailsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val hourlyPrecipitationAdapter = HourlyPrecipitationAdapter(weatherData)
        val hourlyWindAdapter = HourlyWindAdapter(weatherData)

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
//
//
//        lifecycleScope.launch {
//            val latitude = 49.7128
//            val longitude = 21.006
//            try {
//                val response = WeatherApi.retrofitService.getWeather(latitude, longitude)
//
//                if (response.isSuccessful) {
//                    val weatherData = response.body()
//                    Log.i(ContentValues.TAG, weatherData.toString())
//                    Toast.makeText(requireContext(), weatherData.toString(), Toast.LENGTH_LONG).show()
//                } else {
//                    Log.e(ContentValues.TAG, "Error: ${response.errorBody()?.string()}")
//                }
//            } catch (e: Exception) {
//                Log.e(ContentValues.TAG, "Failed to connect to the server", e)
//                Toast.makeText(
//                    requireContext(),
//                    "Failed to connect to the server",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun placesAutocomplete(query: String) {
        val token = AutocompleteSessionToken.newInstance()

        val bounds = RectangularBounds.newInstance(
            LatLng(49.29899, 14.24712),
            LatLng(54.79086, 23.89251)
        )
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
                if (searchView.text.isNotEmpty()) {
                    val newSearchResults = emptyList<SearchResult>().toMutableList()
                    for (i in 0 until minOf(5, response.autocompletePredictions.size)) {
                        newSearchResults += SearchResult(
                            response.autocompletePredictions[i].getPrimaryText(null).toString(),
                            response.autocompletePredictions[i].placeId
                        )
                    }
                    searchResultViewModel.replaceResults(newSearchResults)
                }
//                for (prediction in response.autocompletePredictions) {
//                    Log.i(TAG, prediction.placeId)
//                    Log.i(TAG, prediction.getPrimaryText(null).toString())
//                }
            }.addOnFailureListener { exception: Exception? ->
                if (exception is ApiException) {
                    Log.e(ContentValues.TAG, "Place not found: ${exception.statusCode}")
                }
            }
    }

    private fun getPlaceByResult(result: SearchResult) {

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
//                Toast.makeText(requireContext(), place.latLng?.toString() ?: "", Toast.LENGTH_LONG).show()
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(ContentValues.TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }
    }
}
