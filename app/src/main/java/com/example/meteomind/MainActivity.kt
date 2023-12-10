package com.example.meteomind

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.meteomind.databinding.ActivityMainBinding
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

        lifecycleScope.launch {
            val weatherResponse =
                WeatherApi.retrofitService.getWeather(52.409538, 16.931992)
            Log.d("MainActivity", "Getting weather data")
            if (weatherResponse.isSuccessful) {
                Log.d("MainActivity", "Weather data received")
                adapter.updateFragment(0, WeatherFragment().apply {
                    arguments = Bundle().apply {
                        putParcelable("weatherData", weatherResponse.body())
                    }
                })
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
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
}