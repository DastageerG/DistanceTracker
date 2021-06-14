package com.example.locationtracker.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService

class LocationTrackingService : LifecycleService()
{
    override fun onCreate()
    {
        super.onCreate()
    } // onCreate closed

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
    {
        return super.onStartCommand(intent, flags, startId)
    } // onStartCommandClosed

} //  LocationTrackingService closed