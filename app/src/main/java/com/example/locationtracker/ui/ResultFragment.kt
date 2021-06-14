package com.example.locationtracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.locationtracker.databinding.FragmentPermissionBinding
import com.example.locationtracker.databinding.FragmentResultBinding


class ResultFragment : Fragment()
{
    var _binding: FragmentResultBinding? = null
    val binding: FragmentResultBinding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentResultBinding.inflate(inflater,container,false)

        return binding.root

    } // onCreateView closed
}