package com.example.locationtracker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Color.red
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.MainActivity
import com.example.locationtracker.R
import com.example.locationtracker.data.Result
import com.example.locationtracker.databinding.FragmentMapsBinding
import com.example.locationtracker.databinding.FragmentPermissionBinding
import com.example.locationtracker.service.LocationTrackingService
import com.example.locationtracker.utils.Constants
import com.example.locationtracker.utils.Constants.ACTION_SERVICE_STOP
import com.example.locationtracker.utils.Constants.TAG
import com.example.locationtracker.utils.ExtensionFunction.disable
import com.example.locationtracker.utils.ExtensionFunction.enable
import com.example.locationtracker.utils.ExtensionFunction.hide
import com.example.locationtracker.utils.ExtensionFunction.show
import com.example.locationtracker.utils.MapUtil
import com.example.locationtracker.utils.MapUtil.calculateDistance
import com.example.locationtracker.utils.MapUtil.calculateElapsedTime
import com.example.locationtracker.utils.MapUtil.setCameraPosition
import com.example.locationtracker.utils.Permissions
import com.example.locationtracker.utils.Permissions.hasBackgroundLocationPermission
import com.example.locationtracker.utils.Permissions.requestBackgroundLocationPermission
import com.google.android.gms.location.FusedLocationProviderClient

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class MapsFragment : Fragment() , OnMapReadyCallback , GoogleMap.OnMyLocationButtonClickListener , EasyPermissions.PermissionCallbacks , GoogleMap.OnMarkerClickListener
{

    private lateinit var googleMap: GoogleMap

    private var _binding: FragmentMapsBinding? = null
    private val binding: FragmentMapsBinding get() = _binding!!
    private var locationList = mutableListOf<LatLng>()
    private var polyLineList = mutableListOf<Polyline>()
    private var markerList = mutableListOf<Marker>()

    private var startTime = 0L
    private var stopTime = 0L

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient



    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?):View
    {
        _binding = FragmentMapsBinding.inflate(inflater,container,false)


        binding.apply ()
        {
            buttonMapsFragStart.setOnClickListener()
            {
                buttonStartClicked()
            }
            buttonMapsFragStop.setOnClickListener()
            {
                sendActionCommandToService(ACTION_SERVICE_STOP)
                binding.buttonMapsFragStop.hide()
                binding.buttonMapsFragStart.show()
            } //
            buttonMapsFragReset.setOnClickListener()
            {
                onResetButtonClicked()
            }
        }

        observeTrackerService()

        return binding.root

    } // onCreateView closed

    @SuppressLint("MissingPermission")
    private fun onResetButtonClicked()
    {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener()
        {
            val lastLocation = LatLng(it.result.latitude,it.result.longitude)
            for(polyline in polyLineList)
            {
                polyline.remove()
            }
             googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(setCameraPosition(lastLocation)))
            locationList.clear()

            for(marker in markerList)
                marker.remove()
            markerList.clear()

            binding.buttonMapsFragReset.hide()
            binding.buttonMapsFragStart.show()
        } // fusedLocationProvideClient closed


    } // onResetButtonClicked

    private fun buttonStartClicked()
    {
        if(hasBackgroundLocationPermission(requireContext()))
        {
            startCountdown();
            binding.buttonMapsFragStart.disable()
            binding.buttonMapsFragStart.hide()
            binding.buttonMapsFragStop.show()
        } // if closed
        else
        {
            requestBackgroundLocationPermission(this)
        } // else closed

    } // buttonStartClicked

    private fun startCountdown()
    {
        binding.texViewMapsFragCountDown.show()
        binding.buttonMapsFragStop.disable()
        val timer:CountDownTimer = object :CountDownTimer(4000,1000)
        {
            override fun onTick(millisUntilFinished: Long)
            {
                val currentSecond = millisUntilFinished/1000
                if(currentSecond.toString() == "0")
                {
                    binding.texViewMapsFragCountDown.text = "Go"
                    binding.texViewMapsFragCountDown.setTextColor(ContextCompat.getColor(requireContext(),R.color.black))
                }// if closed
                else
                {
                    binding.texViewMapsFragCountDown.text = currentSecond.toString()
                    binding.texViewMapsFragCountDown.setTextColor(ContextCompat.getColor(requireContext(),R.color.colorRed))
                } // else closed
            } // onTick closed

            override fun onFinish()
            {
                sendActionCommandToService(Constants.ACTION_SERVICE_START)
                binding.texViewMapsFragCountDown.hide()
            } // onFinish closed
        } // timer closed

        timer.start()

    } // startCountdown closed

    fun sendActionCommandToService(action:String)
    {
        Intent(requireContext(),LocationTrackingService::class.java).apply()
        {
            this.action = action
            requireContext().startService(this)
        }
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

   private fun observeTrackerService()
    {
        LocationTrackingService.locationList.observe(viewLifecycleOwner)
        {
            it?.let()
            {
                locationList = it
                if(locationList.size > 1)
                    binding.buttonMapsFragStop.enable()
                drawPolyLine()
                followPolyLine()
            } // let closed
        } // observer closed

        LocationTrackingService.startTime.observe(viewLifecycleOwner)
        {
            startTime = it
        } // startTimeObserver closed

        LocationTrackingService.stopTime.observe(viewLifecycleOwner)
        {
            stopTime = it
            if(stopTime !=0L)
            {
                showBiggerPicture();
                displayResults();
            }
        } // startTimeObserver closed


    } // observerTrackerService closed

    private fun displayResults()
    {
        val result = Result(calculateDistance(locationList),calculateElapsedTime(startTime,stopTime))

        lifecycleScope.launch()
        {
            delay(2000)
            val action = MapsFragmentDirections.actionMapsFragmentToResultFragment(result)
            findNavController().navigate(action)
            binding.buttonMapsFragStart.apply ()
            {
                enable()
                hide()
            }
            binding.buttonMapsFragStop.hide()
            binding.buttonMapsFragReset.show()
        }



    }

    private fun showBiggerPicture()
    {
        val bounds = LatLngBounds.builder()
        for(location in locationList)
        {
            bounds.include(location)
        } // for closed
        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(),100),2000,null)
        addMarker(locationList.first())
        addMarker(locationList.last())
    } // showBiggerPicture closed

    private fun addMarker(position: LatLng)
    {
        val marker = googleMap.addMarker(MarkerOptions().position(position))
        markerList.add(marker!!)
    }

    fun drawPolyLine()
    {
        val polyline = googleMap.addPolyline(
            PolylineOptions().apply()
            {
                width(10f)
                color(Color.BLUE)
                jointType(JointType.ROUND)
                startCap(ButtCap())
                endCap(ButtCap())
                addAll(locationList)
            } // apply closed
        ) // addPolyLine closed
        polyLineList.add(polyline)

    } // drawPolyLine closed

    fun followPolyLine()
    {
        if (locationList.isNotEmpty())
        {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(MapUtil.setCameraPosition(locationList.last())),1000,null)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap)
    {
        googleMap = map
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
        googleMap.setOnMarkerClickListener(this)
        googleMap.uiSettings.apply ()
        {
            isZoomControlsEnabled = false
            isZoomGesturesEnabled = false
            isRotateGesturesEnabled = false
            isTiltGesturesEnabled = false
            isCompassEnabled = false
            isScrollGesturesEnabled = false
        }

    }

    override fun onMyLocationButtonClick(): Boolean
    {
        binding.textViewTapOnMyLocationButton.animate().alpha(0f).duration = 1500
        lifecycleScope.launch()
        {
            delay(2000)
            binding.textViewTapOnMyLocationButton.hide()
            binding.buttonMapsFragStart.show()
        }
        return false
    } // onMyLocationButtonClicked


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)

    } // onRequestPermissionsResult


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) { } // onPermissionsGranted

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>)
    {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
        {
            AppSettingsDialog.Builder(this).build().show()
        } // if closed
        else
        {
            requestBackgroundLocationPermission(this)
        } // else closed
    } //  onPermissionsDenied

    override fun onMarkerClick(p0: Marker): Boolean
    {
        return true
    }


} // MapsFragment closed