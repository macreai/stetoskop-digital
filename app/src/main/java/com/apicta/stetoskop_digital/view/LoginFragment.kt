package com.apicta.stetoskop_digital.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentLoginBinding
import com.apicta.stetoskop_digital.viewmodel.AuthViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private var loginJob: Job = Job()

    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val view = binding.root

        login()
        checkLoginUser()
        binding.register.setOnClickListener {
            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }

    private fun checkLoginUser(){
        viewModel.getId().observe(viewLifecycleOwner, Observer { id ->
            if (id != 0){
                Log.d(TAG, "checkLoginUser: $id")
                view?.findNavController()?.navigate(R.id.action_loginFragment_to_homePatientFragment)
            }
        })
    }

    private fun login() {
        binding.login.setOnClickListener {
            binding.loading.visibility = View.VISIBLE
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()

            lifecycleScope.launchWhenResumed {
                if (email.isEmpty() || password.isEmpty()){
                    Toast.makeText(requireContext(), "Please insert the username or password", Toast.LENGTH_SHORT).show()
                } else {
                    if (loginJob.isActive) loginJob.cancel()
                    loginJob = launch {
                        viewModel.login(email, password).collect{ result ->
                            result.onSuccess { loginResponse ->
                                binding.loading.visibility = View.GONE
                                viewModel.saveId(loginResponse.user?.id!!)
                                Log.d(TAG, "login: ${loginResponse.user?.id.toString()}")
                                viewModel.saveToken(loginResponse.authorisation!!.token!!)
                                if (loginResponse.status == "success" && loginResponse.user?.role == "pasien"){
                                    Log.d(TAG, "login: $loginResponse")
                                    Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                                    view?.findNavController()?.navigate(R.id.action_loginFragment_to_homePatientFragment)
                                } else if (loginResponse.status == "success" && loginResponse.user?.role == "dokter"){
                                    Log.d(TAG, "login: $loginResponse")
                                    Toast.makeText(requireContext(), "Login success", Toast.LENGTH_SHORT).show()
                                    view?.findNavController()?.navigate(R.id.action_loginFragment_to_homeDoctorFragment)
                                }
                            }
                            result.onFailure { throwable ->
                                binding.loading.visibility = View.GONE
                                Log.e(TAG, "login: $throwable")
                                throwable.printStackTrace()
                                Toast.makeText(requireContext(), "Failed to Connect", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        private const val TAG = "Login Fragment"
    }
}