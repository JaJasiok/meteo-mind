package com.example.meteomind

import android.content.ContentValues
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
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
import dev.zotov.phototime.solarized.Solarized
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.util.Locale
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.random.Random


class WeatherFragment : Fragment(R.layout.fragment_weather)/*, SensorEventListener*/ {

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

    private lateinit var particleSystem: ParticleSystem
    private lateinit var particleView: ParticleView
    private var currentRunnable: Runnable? = null

    private lateinit var weatherData: WeatherData

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeatherBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(FlowPreview::class)
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

        val searchBar = binding.weatherView.searchBar
//        setSupportActionBar(searchBar)

        searchResultViewModel = ViewModelProvider(this)[SearchResultViewModel::class.java]

        val recyclerView = binding.recyclerView
//        recyclerView.adapter = searchResultAdapter

        searchView = binding.searchView

        locationViewModel.locations.observe(requireActivity()) { locations ->
            locationAdapter = LocationAdapter(locations).apply {
                setListener(object : LocationAdapter.Listener {
                    override fun onClick(position: Int) {
                        if (!isNetworkAvailable(requireContext())){
                            Toast.makeText(
                                requireContext(),
                                "No internet connection. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else{
                            val location = locations[position]
                            searchBar.setText(location.locationName)

                            lifecycleScope.launch {
                                val weatherResponse =
                                    WeatherApi.retrofitService.getWeather(
                                        location.locationLat,
                                        location.locationLng
                                    )
                                Log.d("WeatherFragment", "Getting weather data")
                                if (weatherResponse.isSuccessful) {
                                    Log.d("WeatherFragment", "Weather data received")
                                    weatherData = weatherResponse.body()!!
                                    updateWeather()
                                }
                            }
                        }
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
                        if (!isNetworkAvailable(requireContext())){
                            Toast.makeText(
                                requireContext(),
                                "No internet connection. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else {
                            val result = results[position]
                            getPlaceByResult(result)
                            searchBar.setText(result.locationName)
                            searchView.editText.text.clear()
                            recyclerView.adapter = locationAdapter

                            lifecycleScope.launch {
                                val weatherResponse =
                                    WeatherApi.retrofitService.getWeather(
                                        result.locationLat,
                                        result.locationLng
                                    )
                                Log.d("WeatherFragment", "Getting weather data")
                                if (weatherResponse.isSuccessful) {
                                    Log.d("WeatherFragment", "Weather data received")
                                    weatherData = weatherResponse.body()!!
                                    updateWeather()
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "Error connecting to the server. Please try again later.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                        searchView.hide()
                        searchResultViewModel.deleteResults()
                    }
                })
            }
        }

        searchView.addTransitionListener { _: SearchView?, _: TransitionState?, newState: TransitionState ->
            val tabLayout = (activity as MainActivity).tabLayout
            if (newState == TransitionState.SHOWING) {
                tabLayout.visibility = View.GONE
            } else if (newState == TransitionState.HIDING) {
                tabLayout.visibility = View.VISIBLE
            }
        }

        val searchFlow = MutableSharedFlow<String>(replay = 1)

        searchView
            .editText
            .doOnTextChanged { text, _, _, _ ->
                if(!isNetworkAvailable(requireContext())){
                    Toast.makeText(
                        requireContext(),
                        "No internet connection.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else {
                    if (text.isNullOrEmpty()) {
                        searchResultViewModel.deleteResults()
                        recyclerView.adapter = locationAdapter
                    } else {
                        recyclerView.adapter = searchResultAdapter
                        placesAutocomplete(text.toString())
                    }
                }
            }

        lifecycleScope.launch {
            searchFlow
                .debounce(300) // delay of 300 milliseconds
                .distinctUntilChanged() // only proceed if the text has changed
                .collect { query ->
                    recyclerView.adapter = searchResultAdapter
                    placesAutocomplete(query)
                }
        }

        val viewPager = requireActivity().findViewById<ViewPager2>(R.id.view_pager)
        val searchView = binding.searchView

        searchView.addTransitionListener { _, _, newState ->
            viewPager.isUserInputEnabled = newState != TransitionState.SHOWING
        }

        val divider = MaterialDividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL)
        divider.isLastItemDecorated = false
        recyclerView.addItemDecoration(divider)

        weatherData =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                arguments?.getParcelable("weatherData", WeatherData::class.java)!!
            } else {
                arguments?.getParcelable<WeatherData>("weatherData")!!
            }

        val latitude = weatherData.lat
        val longitude = weatherData.lng

        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses!!.isNotEmpty()) {
            val address = addresses[0]
            val cityName = address.locality

            searchBar.hint = cityName
        }

        particleView = binding.particleView
        particleSystem = ParticleSystem(requireContext())

        particleView.setParticleSystem(particleSystem)

        updateWeather()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateWeather() {
        val timestamp = weatherData.timestamps[0].timestamp

//        Toast.makeText(requireContext(), timestamp, Toast.LENGTH_LONG).show()

        val t2m = weatherData.timestamps[0].values.t2m
        val sp = weatherData.timestamps[0].values.sp
        val u10 = weatherData.timestamps[0].values.u10
        val v10 = weatherData.timestamps[0].values.v10
        val tcc = weatherData.timestamps[0].values.tcc
        val tp = weatherData.timestamps[0].values.tp

        if (tp > 0.1) {
            val random = Random
            val screenWidth = requireContext().resources.displayMetrics.widthPixels

            val handler = Handler(Looper.getMainLooper())

            if (currentRunnable == null) {
                if (t2m > 1.0) {
                    currentRunnable = object : Runnable {
                        override fun run() {
                            val randomX = random.nextInt(screenWidth)
                            particleSystem.emitWaterDrop(randomX.toFloat())
                            particleSystem.update()
                            particleView.invalidate()
                            handler.postDelayed(this, 10)
                        }
                    }
                } else {
                    currentRunnable = object : Runnable {
                        override fun run() {
                            val randomX = random.nextInt(screenWidth)
                            particleSystem.emitSnowflake(randomX.toFloat())
                            particleSystem.update()
                            particleView.invalidate()
                            handler.postDelayed(this, 10)
                        }
                    }
                }

                // Post the new Runnable to the Handler
                handler.post(currentRunnable!!)
            }
        }

        val localDateTime = LocalDateTime.parse(timestamp)

        val sunrise = Solarized(weatherData.lat, weatherData.lng, localDateTime).sunrise?.date
        val sunset = Solarized(weatherData.lat, weatherData.lng, localDateTime).sunset?.date

        val weatherImageFile = getWeatherImageName(
            weatherData.timestamps[0].values,
            localDateTime,
            sunrise!!,
            sunset!!
        )
//        Toast.makeText(requireContext(), weatherImageFile, Toast.LENGTH_LONG).show()

        val weatherImage = binding.weatherView.weatherImage
        weatherImage.setImageDrawable(getDrawableByName(requireContext(), weatherImageFile))

        val tempText = binding.weatherView.tempText
        tempText.text = t2m.toInt().toString()

        val hourlyWeatherRecyclerView = binding.weatherView.hourlyWeather

        hourlyWeatherRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val hourlyWeatherAdapter = HourlyWeatherAdapter(weatherData)
        hourlyWeatherRecyclerView.adapter = hourlyWeatherAdapter

        val windArrow = binding.weatherView.windArrow
        windArrow.rotation = calculateWindDirection(u10, v10)
        val windDirection = binding.weatherView.windDirection
        windDirection.text = getWindDirection(u10, v10)
        val windValue = binding.weatherView.windValue
        windValue.text = sqrt(u10.pow(2) + v10.pow(2)).toInt().toString()

        val pressureValue = binding.weatherView.pressureValue
        pressureValue.text = sp.toInt().toString()

        val barometer = binding.weatherView.barometerView
        barometer.setProgress(scalePressure(sp))

        val sun = binding.weatherView.sunPositionView
        sun.setSunPosition(localDateTime, sunrise!!, sunset!!)

        val sunriseValue = binding.weatherView.sunriseValue
        sunriseValue.text = sunrise.toString().substring(11, 16).removePrefix("0")

        val sunsetValue = binding.weatherView.sunsetValue
        sunsetValue.text = sunset.toString().substring(11, 16).removePrefix("0")

        val hourlyDetailsRecyclerView = binding.weatherView.hourlyDetails
        hourlyDetailsRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        val hourlyPrecipitationAdapter = HourlyPrecipitationAdapter(weatherData)
        val hourlyWindAdapter = HourlyWindAdapter(weatherData)

        hourlyDetailsRecyclerView.adapter = hourlyPrecipitationAdapter

        val toggleButton = binding.weatherView.toggleButton
        toggleButton.addOnButtonCheckedListener { _, checkedId, isChecked ->
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
                        val placeId = response.autocompletePredictions[i].placeId
                        val placeFields = listOf(Place.Field.LAT_LNG)
                        val fetchPlaceRequest = FetchPlaceRequest.newInstance(placeId, placeFields)
                        placesClient.fetchPlace(fetchPlaceRequest)
                            .addOnSuccessListener { fetchPlaceResponse: FetchPlaceResponse ->
                                val latLng = fetchPlaceResponse.place.latLng
                                newSearchResults += SearchResult(
                                    response.autocompletePredictions[i].getPrimaryText(null)
                                        .toString(),
                                    placeId,
                                    latLng?.latitude ?: 0.0,
                                    latLng?.longitude ?: 0.0
                                )
                            }
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

        val request = FetchPlaceRequest.newInstance(result.placeId, placeFields)

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
                }
            }
    }
}
