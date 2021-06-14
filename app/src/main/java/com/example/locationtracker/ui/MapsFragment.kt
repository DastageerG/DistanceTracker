package com.example.locationtracker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color.red
import androidx.fragment.app.Fragment

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.databinding.FragmentMapsBinding
import com.example.locationtracker.databinding.FragmentPermissionBinding
import com.example.locationtracker.utils.ExtensionFunction.disable
import com.example.locationtracker.utils.ExtensionFunction.hide
import com.example.locationtracker.utils.ExtensionFunction.show
import com.example.locationtracker.utils.Permissions
import com.example.locationtracker.utils.Permissions.hasBackgroundLocationPermission
import com.example.locationtracker.utils.Permissions.requestBackgroundLocationPermission

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MapsFragment : Fragment() , OnMapReadyCallback , GoogleMap.OnMyLocationButtonClickListener , EasyPermissions.PermissionCallbacks
{

    private lateinit var googleMap: GoogleMap

    var _binding: FragmentMapsBinding? = null
    val binding: FragmentMapsBinding get() = _binding!!

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
            }
            buttonMapsFragReset.setOnClickListener()
            {

            }
        }

        return binding.root

    } // onCreateView closed

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
        binding.buttonMapsFragStop.display

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
                binding.texViewMapsFragCountDown.hide()
            } // onFinish closed
        } // timer closed

        timer.start()

    } // startCountdown closed

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(map: GoogleMap)
    {
        googleMap = map
        googleMap.isMyLocationEnabled = true
        googleMap.setOnMyLocationButtonClickListener(this)
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


} // MapsFragment closed