package com.example.locationtracker.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.locationtracker.utils.Constants.BACKGROUND_LOCATION_PERMISSION_CODE
import com.example.locationtracker.utils.Constants.LOCATION_PERMISSION_CODE
import pub.devrel.easypermissions.EasyPermissions
import java.util.jar.Manifest

object Permissions
{

    fun hasLocationPermission(context: Context) =
        EasyPermissions.hasPermissions(context,android.Manifest.permission.ACCESS_FINE_LOCATION)


    fun requestLocationPermission(fragment:Fragment)
    {
        EasyPermissions.requestPermissions(fragment,"App requires Permission to Run",LOCATION_PERMISSION_CODE,android.Manifest.permission.ACCESS_FINE_LOCATION)
    }

     fun hasBackgroundLocationPermission(context: Context) : Boolean
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
           return EasyPermissions.hasPermissions(context,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        return true
    }


    fun requestBackgroundLocationPermission(fragment:Fragment)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        {
            EasyPermissions.requestPermissions(fragment,"App requires Background Location permission", BACKGROUND_LOCATION_PERMISSION_CODE,android.Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }


} // permissions