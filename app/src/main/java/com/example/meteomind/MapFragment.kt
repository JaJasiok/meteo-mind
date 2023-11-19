package com.example.meteomind

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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
        LatLng(48.75, 13.5),
        LatLng(55.25, 24.5)
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
    private lateinit var animationFrames1: List<BitmapDescriptor>
    private lateinit var animationFrames2: List<BitmapDescriptor>

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

        animationFrames1 = listOf(
            BitmapDescriptorFactory.fromResource(R.drawable.o1),
            BitmapDescriptorFactory.fromResource(R.drawable.o2),
            BitmapDescriptorFactory.fromResource(R.drawable.o3),
            BitmapDescriptorFactory.fromResource(R.drawable.o4),
            BitmapDescriptorFactory.fromResource(R.drawable.o5),
            BitmapDescriptorFactory.fromResource(R.drawable.o6)
        )

        animationFrames2 = listOf(
            BitmapDescriptorFactory.fromResource(R.drawable.s1),
            BitmapDescriptorFactory.fromResource(R.drawable.s2),
            BitmapDescriptorFactory.fromResource(R.drawable.s3),
            BitmapDescriptorFactory.fromResource(R.drawable.s4),
            BitmapDescriptorFactory.fromResource(R.drawable.s5),
            BitmapDescriptorFactory.fromResource(R.drawable.s6)
        )

        currentAnimationFrames = animationFrames1

        layersFab = binding.layersFab
        tempFab = binding.tempFab
        preciFab = binding.preciFab
        cloudFab = binding.cloudFab

        layersFab.setOnClickListener {
            if (!isExpanded) {
                tempFab.show()
                preciFab.show()
                cloudFab.show()
                layersFab.setImageResource(R.drawable.layers_24px)
                isExpanded = true
            } else {
                tempFab.hide()
                preciFab.hide()
                cloudFab.hide()
                layersFab.setImageResource(activeLayerIcon)
                isExpanded = false
            }
        }

        tempFab.setOnClickListener {
            if (currentAnimationFrames == animationFrames1) {
                currentAnimationFrames = animationFrames2
            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.thermometer_24px)
            activeLayerIcon = R.drawable.thermometer_24px
            isExpanded = false
        }

        preciFab.setOnClickListener {
            if (currentAnimationFrames == animationFrames2) {
                currentAnimationFrames = animationFrames1
            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.water_drop_24px)
            activeLayerIcon = R.drawable.water_drop_24px
            isExpanded = false
        }

        cloudFab.setOnClickListener {
//            if (currentAnimationFrames == animationFrames3) {
//                currentAnimationFrames = animationFrames3
//            }
            tempFab.hide()
            preciFab.hide()
            cloudFab.hide()

            layersFab.setImageResource(R.drawable.cloud_24px)
            activeLayerIcon = R.drawable.cloud_24px
            isExpanded = false
        }

        playPauseButton = binding.playPauseButton
        playPauseButton.setOnClickListener {
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

        slider = binding.slider
        slider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                frameIndex = value.toInt()
                updateGroundOverlayImage()
            }
        }
        speedButton = binding.speedButton
        speedButton.setOnClickListener {
            if (frameDelay == 1000.toLong()) {
                frameDelay = 500.toLong()
                speedButton.text = "×2"
            } else {
                frameDelay = 1000.toLong()
                speedButton.text = "×1"
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
//                val zoom = mMap.cameraPosition.zoom
//                if (zoom < minZoom) {
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(minZoom))
//                }
//                else if (zoom > maxZoom) {
//                    mMap.animateCamera(CameraUpdateFactory.zoomTo(maxZoom))
//                }

                if (!polandBounds.contains(mMap.projection.visibleRegion.latLngBounds.northeast)
                    && !polandBounds.contains(mMap.projection.visibleRegion.latLngBounds.southwest)
                ) {
                    resetMapToBounds()
                }
            }
            isMovingCamera = false
            viewPager.isUserInputEnabled = true
        }

//        val papaj = GroundOverlayOptions()
//            .image(BitmapDescriptorFactory.fromResource(R.drawable.papaj))
//            .positionFromBounds(polandBounds)
//            .transparency(0.5f)
//
//        mMap.addGroundOverlay(papaj)
        startImageAnimation()
    }

    private fun startImageAnimation() {
        handler.post(object : Runnable {
            override fun run() {
                // Update the ground overlay image with the next frame
                updateGroundOverlayImage()
                handler.postDelayed(this, frameDelay)
                isPlaying = true
            }
        })
    }

    private fun updateGroundOverlayImage() {
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

    private fun resetMapToBounds() {
        val padding = 100
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(polandBounds, padding)
        mMap.animateCamera(cameraUpdate)
    }
}
