package com.example.locationtracker.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.example.locationtracker.utils.Constants
import com.example.locationtracker.utils.Constants.NOTIFICATION_CHANNEL_ID
import com.example.locationtracker.utils.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.locationtracker.utils.Constants.NOTIFICATION_ID
import com.example.locationtracker.utils.Constants.TAG
import com.example.locationtracker.utils.MapUtil
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : LifecycleService()
{


    @Inject
    lateinit var notification:NotificationCompat.Builder
    @Inject
    lateinit var notificationManager:NotificationManager

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object
    {
        val serviceStarted = MutableLiveData<Boolean>()
        val locationList = MutableLiveData<MutableList<LatLng>>()
        val startTime = MutableLiveData<Long>()
        val stopTime = MutableLiveData<Long>()
    }

    fun initialValues()
    {
        serviceStarted.postValue(false)
        locationList.postValue(mutableListOf())
        startTime.postValue(0)
        stopTime.postValue(0)

    } // initialValues closed

    private val locationCallback:LocationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult)
        {
            super.onLocationResult(locationResult)
            locationResult.locations.let()
            {
                locations ->
                for(location in locations)
                {
                    updateLocationList(location)
                    updateNotification()
                }
            }
        } // onLocationResult closed

    } // locationCallBack closed

    private fun updateNotification()
    {
        notification.apply ()
        {
            setContentTitle("Distance Traveled")
            setContentText(locationList.value?.let { MapUtil.calculateDistance(it)}+ "km")
        } // apply closed
        notificationManager.notify(Constants.NOTIFICATION_ID,notification.build())
    } // updateNotification closed

    override fun onCreate()
    {
        super.onCreate()
        initialValues()
    } // onCreate closed

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        when(intent?.action)
        {
            Constants.ACTION_SERVICE_START ->
            {
                serviceStarted.value = true
                startForeGroundService()
                startLocationUpdates()
                Log.d(TAG, "onStartCommand: starrtted")
            }
            Constants.ACTION_SERVICE_STOP ->
            {
                serviceStarted.value = false
                stopForegroundService();
            }
            else-> {}
        } // when closed
        return super.onStartCommand(intent, flags, startId)
    } // onStartCommand closed

    private fun stopForegroundService()
    {
        removeLocationUpdates();
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
        stopSelf()
        stopTime.postValue(System.currentTimeMillis())

    }

    private fun removeLocationUpdates()
    {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


    fun startForeGroundService()
    {
        createNotificationChannel()
        startForeground(NOTIFICATION_ID,notification.build())
        Log.d(TAG, "startForeGroundService: notificationCreated")
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates()
    {
        val locationRequest = LocationRequest.create().apply ()
        {
            interval = Constants.LOCATION_UPDATES_INTERVAL
            fastestInterval = Constants.LOCATION_FAST_UPDATES_INTERVAL
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        } // locationRequest
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper())
        startTime.postValue(System.currentTimeMillis())

    } // startLocationUpdates cosed

    fun updateLocationList(location:Location)
    {
        val latLang = LatLng(location.latitude,location.longitude)
        locationList.value?.apply ()
        {
            add(latLang)
            locationList.postValue(this)
        }
    }

    fun createNotificationChannel()
    {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME,IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        } // if closed
    }



} //  LocationTrackingService closed