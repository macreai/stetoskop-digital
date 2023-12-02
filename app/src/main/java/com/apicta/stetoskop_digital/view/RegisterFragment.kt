package com.apicta.stetoskop_digital.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentLoginBinding
import com.apicta.stetoskop_digital.databinding.FragmentRegisterBinding
import com.apicta.stetoskop_digital.viewmodel.AuthViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private var registerJob: Job = Job()

    private val viewModel: AuthViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root

        register()
        
        return view
    }

    private fun register(){

        // Populate the gender spinner with options
        val genderOptions = arrayOf("Laki-laki", "Perempuan")
        val genderAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderOptions)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.gender.adapter = genderAdapter

        binding.register.setOnClickListener {
            binding.loading.visibility = View.VISIBLE

            // Handle registration button click here
            val emailValue = binding.email.text.toString()
            val nameValue = binding.name.text.toString()
            val genderValue = binding.gender.selectedItem.toString()
            val addressValue = binding.address.text.toString()
            val passwordValue = binding.password.text.toString()
            val confirmPasswordValue = binding.passwordConfirm.text.toString()

            // Validate email using regex
            if (emailValue.isEmpty() || !EMAIL_REGEX.matches(emailValue)) {
                binding.email.error = "Invalid email address"
                return@setOnClickListener
            }

            // Validate name
            if (nameValue.isEmpty()) {
                binding.name.error = "Name cannot be empty"
                return@setOnClickListener
            }

            // Validate gender
            if (genderValue.isEmpty()) {
                Toast.makeText(requireContext(), "Please select a gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate address
            if (addressValue.isEmpty()) {
                binding.address.error = "Address cannot be empty"
                return@setOnClickListener
            }

            // Validate password
            if (passwordValue.isEmpty()) {
                binding.password.error = "Password cannot be empty"
                return@setOnClickListener
            }

            // Validate confirm password
            if (confirmPasswordValue.isEmpty()) {
                binding.passwordConfirm.error = "Confirm password cannot be empty"
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (passwordValue != confirmPasswordValue) {
                binding.passwordConfirm.error = "Passwords do not match"
                return@setOnClickListener
            }

            lifecycleScope.launchWhenResumed {
                if (registerJob.isActive) registerJob.cancel()
                registerJob = launch {
                    viewModel.register(emailValue, nameValue, addressValue, genderValue, passwordValue).collect{ result ->
                        result.onSuccess {  response ->
                            binding.loading.visibility = View.GONE
                            if (response.status == "success"){
                                Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                                findNavController().popBackStack()
                            } else {
                                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                        result.onFailure {  throwable ->
                            binding.loading.visibility = View.GONE
                            Toast.makeText(requireContext(), "Failed to connect", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            Log.d("Register", "onViewCreated: $emailValue, $nameValue, $genderValue, $addressValue, $passwordValue, $confirmPasswordValue")
        }

        // Add logic for showing/hiding password visibility
        binding.showPassword.setOnClickListener {
            binding.password.transformationMethod = null
            binding.showPassword.visibility = View.INVISIBLE
            binding.hidePassword.visibility = View.VISIBLE
        }

        binding.hidePassword.setOnClickListener {
            binding.password.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.showPassword.visibility = View.VISIBLE
            binding.hidePassword.visibility = View.INVISIBLE
        }

        // Add logic for showing/hiding confirm password visibility
        binding.showConfirmPassword.setOnClickListener {
            binding.passwordConfirm.transformationMethod = null
            binding.showConfirmPassword.visibility = View.INVISIBLE
            binding.hideConfirmPassword.visibility = View.VISIBLE
        }

        binding.hideConfirmPassword.setOnClickListener {
            binding.passwordConfirm.transformationMethod = android.text.method.PasswordTransformationMethod.getInstance()
            binding.showConfirmPassword.visibility = View.VISIBLE
            binding.hideConfirmPassword.visibility = View.INVISIBLE
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b")
    }
}