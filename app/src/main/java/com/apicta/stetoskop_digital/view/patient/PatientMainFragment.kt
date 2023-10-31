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
import androidx.navigation.ui.setupWithNavController
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView


class PatientMainFragment : Fragment() {

    private var _binding: FragmentPatientMainBinding? = null
    private val binding get()= _binding!!

    private lateinit var navController: NavController
    private lateinit var bottomNavigation: BottomNavigationView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPatientMainBinding.inflate(inflater, container, false)
        val view = binding.root

        navController = findNavController()
        bottomNavigation = binding.navView

        bottomNavigation.setupWithNavController(navController)

//        bottomNavigation.setOnItemSelectedListener(object: NavigationBarView.OnItemSelectedListener{
//            override fun onNavigationItemSelected(item: MenuItem): Boolean {
//                when(item.itemId){
//                    R.id.homePatientFragment -> {
//                        val action = Fragment1D
//                        navController.navigate()
//                    }
//                }
//            }
//
//        })
//       binding.navView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
//            navController = findNavController(requireActivity(), R.id.nav_host_fragment_activity_main)
//            when (item.itemId) {
//                R.id.patient_bottom_navigation -> navController.navigate(R.id.action_WeightFragment_to_IntakeFragment)
//                R.id.bottom_navi_weight -> navController.navigate(R.id.action_IntakeFragment_to_WeightFragment)
//            }
//            true
//        })

        return view
    }
}