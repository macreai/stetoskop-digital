package com.apicta.stetoskop_digital.view.patient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.apicta.stetoskop_digital.MainActivity
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.databinding.FragmentHomePatientBinding
import com.apicta.stetoskop_digital.databinding.FragmentLoginBinding
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding

class HomePatientFragment : Fragment() {

    private var _binding: FragmentHomePatientBinding? = null
    private val binding get()= _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePatientBinding.inflate(inflater, container, false)
        val view = binding.root

        initUi()

        return view
    }

    override fun onResume() {
        super.onResume()
        requestStoragePermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSION_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    activity?.finish()
                }
            }
        }
    }

    private fun initUi() {
        binding.connect.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homePatientFragment_to_listDeviceFragment)
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE

            if (ContextCompat.checkSelfPermission(requireContext(), storagePermission) != PermissionChecker.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(storagePermission),
                    REQUEST_CODE_PERMISSION_STORAGE
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        private const val REQUEST_CODE_PERMISSION_STORAGE = 3
    }
}