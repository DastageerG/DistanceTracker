package com.example.locationtracker.ui

import android.os.Binder
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.navigation.fragment.findNavController
import com.example.locationtracker.R
import com.example.locationtracker.databinding.FragmentPermissionBinding
import com.example.locationtracker.utils.Permissions
import com.example.locationtracker.utils.Permissions.hasLocationPermission
import com.example.locationtracker.utils.Permissions.requestLocationPermission
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class PermissionFragment : Fragment() , EasyPermissions.PermissionCallbacks
{
    var _binding:FragmentPermissionBinding? = null
    val binding:FragmentPermissionBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentPermissionBinding.inflate(inflater,container,false)

        binding.buttonContinuePermissionFrag.setOnClickListener()
        {
            if(hasLocationPermission(requireContext()))
            {
                findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)

            }
            else
            {
                requestLocationPermission(this)
            }
        } // clickListener
        return binding.root
    } // onCreateView closed



    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)

    } // onRequestPermissionsResult


    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>)
    {
        findNavController().navigate(R.id.action_permissionFragment_to_mapsFragment)
    } // onPermissionsGranted

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>)
    {
        if(EasyPermissions.somePermissionPermanentlyDenied(this,perms))
        {
            AppSettingsDialog.Builder(this).build().show()
        } else
        {
            requestLocationPermission(this)
        }
    } //  onPermissionsDenied

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

} // onCreateViewClosed