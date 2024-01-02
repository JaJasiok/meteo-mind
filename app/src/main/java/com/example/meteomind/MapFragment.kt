package com.example.meteomind

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.meteomind.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.GroundOverlayOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: FragmentMapBinding
    private lateinit var layersFab: FloatingActionButton
    private lateinit var tempFab: FloatingActionButton
    private lateinit var preciFab: FloatingActionButton
    private lateinit var cloudFab: FloatingActionButton
    private lateinit var playPauseButton: MaterialButton
    private lateinit var speedButton: Button
    private lateinit var slider: Slider
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerCallback: ViewPager2.OnPageChangeCallback
    private var isViewPagerSwipeEnabled = true

    private val polandBounds = LatLngBounds(
        LatLng(49.0, 14.0),
        LatLng(55.0, 25.0)
    )

//    private val minZoom = 4.0f
//    private val maxZoom = 16.0f

    private var isMovingCamera = false
    private var isExpanded = false
    private var isPlaying: Boolean = false

    private var frameIndex = 0
    private val handler = Handler()
    private var frameDelay: Long = 1000

    private lateinit var currentAnimationFrames: List<BitmapDescriptor>
    private lateinit var tempMaps: List<BitmapDescriptor>
    private lateinit var precimaps: List<BitmapDescriptor>
    private lateinit var cloudMaps: List<BitmapDescriptor>

    private lateinit var tempLegend: Drawable
    private lateinit var preciLegend: Drawable
    private lateinit var cloudLegend: Drawable

    private var activeLayerIcon = R.drawable.thermometer_24px


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

//        animationFrames1 = listOf(
//            BitmapDescriptorFactory.fromResource(R.drawable.o1),
//            BitmapDescriptorFactory.fromResource(R.drawable.o2),
//            BitmapDescriptorFactory.fromResource(R.drawable.o3),
//            BitmapDescriptorFactory.fromResource(R.drawable.o4),
//            BitmapDescriptorFactory.fromResource(R.drawable.o5),
//            BitmapDescriptorFactory.fromResource(R.drawable.o6)
//        )
//
//        animationFrames2 = listOf(
//            BitmapDescriptorFactory.fromResource(R.drawable.s1),
//            BitmapDescriptorFactory.fromResource(R.drawable.s2),
//            BitmapDescriptorFactory.fromResource(R.drawable.s3),
//            BitmapDescriptorFactory.fromResource(R.drawable.s4),
//            BitmapDescriptorFactory.fromResource(R.drawable.s5),
//            BitmapDescriptorFactory.fromResource(R.drawable.s6)
//        )
//
//        animationFrames3 = listOf(
//            BitmapDescriptorFactory.fromResource(R.drawable.o1),
//            BitmapDescriptorFactory.fromResource(R.drawable.o2),
//            BitmapDescriptorFactory.fromResource(R.drawable.o3),
//            BitmapDescriptorFactory.fromResource(R.drawable.o4),
//            BitmapDescriptorFactory.fromResource(R.drawable.o5),
//            BitmapDescriptorFactory.fromResource(R.drawable.o6)
//        )
//
        currentAnimationFrames = listOf()

        layersFab = binding.layersFab
        tempFab = binding.tempFab
        preciFab = binding.preciFab
        cloudFab = binding.cloudFab

        val legend = binding.legend

        layersFab.setOnClickListener {
            isExpanded = if (!isExpanded) {
                tempFab.show()
                preciFab.show()
                cloudFab.show()
                layersFab.setImageResource(R.drawable.layers_24px)
                true
            } else {
                tempFab.hide()
                preciFab.hide()
                cloudFab.hide()
                layersFab.setImageResource(activeLayerIcon)
                false
            }
        }

        tempFab.setOnClickListener {
            if (currentAnimationFrames != tempMaps) {
                changeMapType(tempMaps)
                legend.setImageDrawable(tempLegend)
            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.thermometer_24px)
            activeLayerIcon = R.drawable.thermometer_24px
            isExpanded = false
        }

        preciFab.setOnClickListener {
            if (currentAnimationFrames != precimaps) {
                changeMapType(precimaps)
                legend.setImageDrawable(preciLegend)
            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.water_drop_24px)
            activeLayerIcon = R.drawable.water_drop_24px
            isExpanded = false
        }

        cloudFab.setOnClickListener {
            if (currentAnimationFrames != cloudMaps) {
                changeMapType(cloudMaps)
                legend.setImageDrawable(cloudLegend)
            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.cloud_24px)
            activeLayerIcon = R.drawable.cloud_24px
            isExpanded = false
        }

        playPauseButton = binding.playPauseButton
        playPauseButton.setOnClickListener {
            if (currentAnimationFrames.isNotEmpty()) {
                if (isPlaying) {
                    handler.removeCallbacksAndMessages(null)
                    playPauseButton.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.play_arrow_24px)

                } else {
                    startImageAnimation()
                    playPauseButton.icon =
                        ContextCompat.getDrawable(requireContext(), R.drawable.pause_24px)
                }
                isPlaying = !isPlaying
            }
        }

        slider = binding.slider
        slider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                if (isPlaying) {
                    frameIndex = value.toInt()
                    updateGroundOverlayImage()
                }
            }
        }
//        setSliderLabels(weatherData.timestamps)
//        setSliderLabels(listOf("24:00", "6:00", "12:00", "18:00"))

        speedButton = binding.speedButton
        speedButton.setOnClickListener {
            if (isPlaying) {
                if (frameDelay == 1000.toLong()) {
                    frameDelay = 500.toLong()
                    speedButton.text = "×2"
                } else {
                    frameDelay = 1000.toLong()
                    speedButton.text = "×1"
                }
            }
        }

        viewPager = requireActivity().findViewById(R.id.viewPager)

        viewPagerCallback = object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Enable or disable swipe based on the current position
                isViewPagerSwipeEnabled =
                    position == 1 // Assuming the map fragment is at position 0
            }
        }

        viewPager.registerOnPageChangeCallback(viewPagerCallback)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val tempMaps = mutableListOf<BitmapDescriptor>()
                val preciMaps = mutableListOf<BitmapDescriptor>()
                val cloudMaps = mutableListOf<BitmapDescriptor>()

                val mapsDir = File(requireContext().cacheDir, "maps")
                val files = mapsDir.listFiles()

                files?.forEach { file ->
                    if (file.isFile && file.extension == "png") {

                        if (file.name.contains("legend")) {
                            val drawable = Drawable.createFromPath(file.absolutePath)
                            when (file.name) {
                                "t2m_legend.png" -> tempLegend = drawable!!
                                "tp_legend.png" -> preciLegend = drawable!!
                                "tcc_legend.png" -> cloudLegend = drawable!!
                            }
                        } else {
                            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                            val bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmap)
                            when {
                                file.name.contains("t2m") -> tempMaps.add(bitmapDescriptor)
                                file.name.contains("tp") -> preciMaps.add(bitmapDescriptor)
                                file.name.contains("tcc") -> cloudMaps.add(bitmapDescriptor)
                            }
                        }
                    }
                }

                this@MapFragment.tempMaps = tempMaps
                this@MapFragment.precimaps = preciMaps
                this@MapFragment.cloudMaps = cloudMaps

                withContext(Dispatchers.Main) {
                    if (tempMaps.size == 1 || preciMaps.size == 1 || cloudMaps.size == 1) {
                        slider.visibility = View.GONE
                    } else {
                        slider.valueTo = (tempMaps.size - 1).toFloat()
                        slider.value = 0f
                    }
                }

                currentAnimationFrames = this@MapFragment.tempMaps
                legend.setImageDrawable(tempLegend)

                layersFab.setImageResource(R.drawable.thermometer_24px)
                activeLayerIcon = R.drawable.thermometer_24px

                withContext(Dispatchers.Main) {
                    startImageAnimation()
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("MapFragment", "Failed to read the files", e)
                    Toast.makeText(
                        requireContext(),
                        "Failed to read the files",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewPager.unregisterOnPageChangeCallback(viewPagerCallback)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val padding = 100

        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(polandBounds, padding)
        mMap.moveCamera(cameraUpdate)

        mMap.setOnCameraMoveStartedListener {
            isMovingCamera = true
            viewPager.isUserInputEnabled = false

        }


        mMap.setOnCameraIdleListener {
            if (isMovingCamera) {
                if (!polandBounds.contains(mMap.projection.visibleRegion.latLngBounds.northeast)
                    && !polandBounds.contains(mMap.projection.visibleRegion.latLngBounds.southwest)
                ) {
                    resetMapToBounds()
                }
            }
            isMovingCamera = false
            viewPager.isUserInputEnabled = true
        }
    }

    private fun setSliderLabels(timestamps: List<String>) {
        val sliderLabelsLayout = binding.sliderLabels
        sliderLabelsLayout.removeAllViews() // Clear any existing views

        for (timestamp in timestamps) {
            val textView = TextView(requireContext())
//            textView.text = formatHour(timestamp)
            textView.text = timestamp
            textView.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f // This makes all TextViews have equal width
            )
            textView.gravity = Gravity.CENTER
            sliderLabelsLayout.addView(textView)
        }
    }

    private fun startImageAnimation() {
        handler.post(object : Runnable {
            override fun run() {
                updateGroundOverlayImage()
                handler.postDelayed(this, frameDelay)
                isPlaying = true
            }
        })
    }

    private fun changeMapType(newMapType: List<BitmapDescriptor>) {
        handler.removeCallbacksAndMessages(null)
        isPlaying = false

        currentAnimationFrames = newMapType

        startImageAnimation()
    }

    private fun updateGroundOverlayImage() {
        if (isPlaying) {
            if (frameIndex < currentAnimationFrames.size) {
                val currentFrame = currentAnimationFrames[frameIndex]
                val overlayOptions = GroundOverlayOptions()
                    .image(currentFrame)
                    .positionFromBounds(polandBounds)
//                .transparency(0.5f)
                mMap.clear()
                mMap.addGroundOverlay(overlayOptions)

                slider.value = frameIndex.toFloat()
                frameIndex++
            } else {
                frameIndex = 0
            }
        }
    }

    private fun resetMapToBounds() {
        val padding = 100
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(polandBounds, padding)
        mMap.animateCamera(cameraUpdate)
    }
}
