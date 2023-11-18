package com.apicta.stetoskop_digital.view.patient

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.apicta.stetoskop_digital.MainActivity
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentHomePatientBinding
import com.apicta.stetoskop_digital.databinding.FragmentLoginBinding
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding
import com.apicta.stetoskop_digital.model.remote.response.LogoutResponse
import com.apicta.stetoskop_digital.view.LoginFragment
import com.apicta.stetoskop_digital.viewmodel.AuthViewModel
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.launch
import java.lang.Exception

class HomePatientFragment : Fragment() {

    private var _binding: FragmentHomePatientBinding? = null
    private val binding get()= _binding!!

    private val viewModel: AuthViewModel by viewModels{ ViewModelFactory(requireContext().dataStore) }

    private var homeJob: Job = Job()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomePatientBinding.inflate(inflater, container, false)
        val view = binding.root

        viewModel.getId().observe(viewLifecycleOwner, Observer { userId ->
            initUi(userId)
        })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true){
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
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

    private fun initUi(id: Int) {
        Glide.with(requireActivity())
            .load(R.drawable.profile).circleCrop()
            .into(binding.profilePhoto)
        lifecycleScope.launchWhenResumed {
            if (homeJob.isActive) homeJob.cancel()
            homeJob = launch {
                viewModel.getUserById(id).collect{ result ->
                    result.onSuccess {  response ->
                        if (response.status == "success"){
                            binding.username.text = if (response.user?.namaLengkap.isNullOrEmpty()) {
                                "User"
                            } else {
                                response.user?.namaLengkap
                            }
                            Log.d(TAG, "initUi: ${response.user}")
                            if (response.user?.gender == "Laki-Laki") {
//                                binding.profilePhoto.setImageDrawable()
                            }
                        } else {
                            binding.username.text = "Failed to connect"
                        }
                    }
                    result.onFailure { throwable ->
                        Log.e(TAG, "login: $throwable")
                        throwable.printStackTrace()
                        Toast.makeText(requireContext(), "Failed to Connect", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        binding.connect.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homePatientFragment_to_deviceFragment)
        }
        binding.logout.setOnClickListener{
            showLogoutDialog()
        }
        binding.record.setOnClickListener {
            view?.findNavController()?.navigate(R.id.action_homePatientFragment_to_recordPatientFragment)
        }
    }

    private fun showLogoutDialog(){
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure want to logout?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Yes"){ _, _ ->
                lifecycleScope.launch {
                    try {
                        val result = viewModel.logout().single()
                        handleLogoutResult(result)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }.show()

    }

    private fun handleLogoutResult(result: Result<LogoutResponse>) {
        result.onSuccess { response ->
            if (response.status == "success"){
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                viewModel.deleteId()
                viewModel.deleteToken()
                activity?.finish()
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        }
        result.onFailure { throwable ->
            Log.e(TAG, "logout: $throwable")
            throwable.printStackTrace()
            Toast.makeText(requireContext(), "Failed to Logout", Toast.LENGTH_SHORT).show()
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
        private const val TAG = "HomePatientFragment"
    }
}