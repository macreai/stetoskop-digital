package com.apicta.stetoskop_digital.view.patient

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore.Audio.Media
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.adapter.DeviceAdapter
import com.apicta.stetoskop_digital.adapter.FileAdapter
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentHomePatientBinding
import com.apicta.stetoskop_digital.databinding.FragmentPatientMainBinding
import com.apicta.stetoskop_digital.databinding.FragmentPredictPatientBinding
import com.apicta.stetoskop_digital.listener.FileListener
import com.apicta.stetoskop_digital.model.local.UserPreference
import com.apicta.stetoskop_digital.model.remote.response.FileItem
import com.apicta.stetoskop_digital.viewmodel.AudioViewModel
import com.apicta.stetoskop_digital.viewmodel.AuthViewModel
import com.apicta.stetoskop_digital.viewmodel.RecordViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collect

class PredictPatientFragment : Fragment() {


    private var _binding: FragmentPredictPatientBinding? = null
    private val binding get()= _binding!!

    private lateinit var fileAdapter: FileAdapter

    private var mediaPlayer: MediaPlayer? = null

    private val recordViewModel: RecordViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }
    private val authViewModel: AuthViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPredictPatientBinding.inflate(inflater, container, false)
        val view = binding.root

        authViewModel.getId().observe(viewLifecycleOwner, Observer {  id ->
            initUi(id)
        })
        askPermission()

        return view
    }

    private fun initFileList(id: Int) {
        binding.loading.visibility = View.VISIBLE
        lifecycleScope.launchWhenResumed {
            recordViewModel.getAllRecord(id).collect{ result ->
                result.onSuccess { response ->
                    binding.loading.visibility = View.GONE
                    if (response.status == "success"){
                        initRecyclerView(response.data!!)
                    } else {
                        Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
                    }
                }
                result.onFailure {  throwable ->
                    binding.loading.visibility = View.GONE
                    Log.d(TAG, "initFileList: $throwable")
                    Toast.makeText(requireContext(), "error: $throwable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initUi(id: Int){
        initFileList(id)
        binding.searchInput.apply {
            clearFocus()
            addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    fileAdapter.filter.filter(s)
                }

                override fun afterTextChanged(s: Editable?) {
                }

            })
        }

    }

    private fun initRecyclerView(files: List<FileItem>){
        fileAdapter = FileAdapter(files)
        binding.fileList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = fileAdapter
        }
        fileAdapter.setOnClickListener(object : FileListener{
            override fun onClick(file: FileItem) {
                val mBundle = Bundle()
                mBundle.putInt(EXTRA_ID, file.id!!)
                view?.findNavController()?.navigate(R.id.action_predictPatientFragment_to_detailFileFragment, mBundle)
            }

        })
    }

    private fun askPermission(){
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request the RECORD_AUDIO permission
            requestPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            RECORD_AUDIO_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Permission Granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Permission denied. Some features may not work.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    companion object {
        private const val TAG = "FileListFrag"
        const val EXTRA_ID = "extra_id"
        private const val RECORD_AUDIO_PERMISSION_REQUEST_CODE = 4
    }
}