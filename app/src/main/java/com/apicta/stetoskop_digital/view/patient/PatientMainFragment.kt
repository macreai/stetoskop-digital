package com.apicta.stetoskop_digital.view.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemSelectedListener
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class PatientMainFragment : Fragment() {

    private var _binding: FragmentPatientMainBinding? = null
    private val binding get()= _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientMainBinding.inflate(inflater, container, false)
        val view = binding.root

        val navView = binding.navView
        val navController = findNavController()

        navView.setupWithNavController(navController)

        return view
    }
}