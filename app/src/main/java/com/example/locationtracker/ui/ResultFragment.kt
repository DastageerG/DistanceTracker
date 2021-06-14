package com.example.locationtracker.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.example.locationtracker.databinding.FragmentPermissionBinding
import com.example.locationtracker.databinding.FragmentResultBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class ResultFragment : BottomSheetDialogFragment()
{
    var _binding: FragmentResultBinding? = null
    val binding: FragmentResultBinding get() = _binding!!
    val args by navArgs<ResultFragmentArgs>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentResultBinding.inflate(inflater,container,false)

        binding.apply ()
        {
            textViewResultFragDistance.text = args.result.distance+" km"
            textViewResultFragTime.text = args.result.time
        }


        return binding.root

    } // onCreateView closed
}