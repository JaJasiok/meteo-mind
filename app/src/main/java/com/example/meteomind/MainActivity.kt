package com.example.meteomind

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.meteomind.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

class MainActivity : AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: ViewPagerAdapter
    private lateinit var binding: ActivityMainBinding

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager

        adapter = ViewPagerAdapter(this)
        viewPager2.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Weather"
                1 -> tab.text = "Map"
            }
        }.attach()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            0
        )

        lifecycleScope.launch(Dispatchers.IO)
        {
            val mapsResponse = WeatherApi.retrofitService.getMaps()
            Log.d("MainActivity", "Getting maps data")
            if (mapsResponse.isSuccessful) {
                val responseBody = mapsResponse.body()
                val zipFile = File(cacheDir, "maps.zip")
                val fos = FileOutputStream(zipFile)
                fos.write(responseBody?.bytes())
                fos.close()

                Log.d("MainActivity", "Zip file received and saved to cache")

                // Unpack the zip file
                val zis = ZipInputStream(zipFile.inputStream())
                var entry = zis.nextEntry
                while (entry != null) {
                    val outputFile = File(cacheDir, entry.name)

                    val parentDir = outputFile.parentFile
                    if (parentDir != null) {
                        if (!parentDir.exists()) {
                            parentDir.mkdirs()
                        }
                    }

                    val fos = FileOutputStream(outputFile)
                    val buffer = ByteArray(1024)
                    var count = zis.read(buffer)
                    while (count != -1) {
                        fos.write(buffer, 0, count)
                        count = zis.read(buffer)
                    }
                    fos.close()
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                zis.closeEntry()
                zis.close()

                withContext(Dispatchers.Main) {
                    adapter.updateFragment(1, MapFragment())
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults) // Add this line

        when (requestCode) {
            0 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permissions were granted, get current location
                    try {
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location: Location? ->
                                lifecycleScope.launch {
                                    val weatherResponse =
                                        WeatherApi.retrofitService.getWeather(
                                            location?.latitude ?: 52.409538,
                                            (location?.longitude ?: 16.931992)
                                        )
                                    Log.d(
                                        "MainActivity",
                                        "Getting weather data for current location"
                                    )
                                    if (weatherResponse.isSuccessful) {
                                        Log.d("MainActivity", "Weather data received")
                                        adapter.updateFragment(0, WeatherFragment().apply {
                                            arguments = Bundle().apply {
                                                putParcelable("weatherData", weatherResponse.body())
                                            }
                                        })
                                    }
                                }
                            }
                    } catch (e: SecurityException) {
                        // Handle the SecurityException
                        Log.e("MainActivity", "Failed to get last location", e)
                    }
                } else {
                    // Permissions were not granted, use hardcoded coordinates
                    val hardcodedLatitude = 52.409538
                    val hardcodedLongitude = 16.931992

                    lifecycleScope.launch {
                        val weatherResponse =
                            WeatherApi.retrofitService.getWeather(
                                hardcodedLatitude,
                                hardcodedLongitude
                            )
                        Log.d("MainActivity", "Getting weather data for hardcoded coordinates")
                        if (weatherResponse.isSuccessful) {
                            Log.d("MainActivity", "Weather data received")
                            adapter.updateFragment(0, WeatherFragment().apply {
                                arguments = Bundle().apply {
                                    putParcelable("weatherData", weatherResponse.body())
                                }
                            })
                        }
                    }
                    Toast.makeText(
                        this,
                        "Please allow the app to access your location in device settings.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }

            else -> {
                // Ignore all other requests.
            }
        }
    }

}

