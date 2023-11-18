package com.apicta.stetoskop_digital.view.patient

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.apicta.stetoskop_digital.adapter.DeviceAdapter
import com.apicta.stetoskop_digital.databinding.FragmentListDeviceBinding
import com.apicta.stetoskop_digital.listener.PairedDeviceListener
import com.apicta.stetoskop_digital.util.BluetoothSocketHolder
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel
import com.psp.bluetoothlibrary.Bluetooth
import com.psp.bluetoothlibrary.BluetoothListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.UUID

class PairedDeviceFragment : Fragment() {

    private var _binding: FragmentListDeviceBinding? = null
    private val binding get()= _binding!!

    private lateinit var deviceAdapter: DeviceAdapter
    private var listPairedDevices: ArrayList<BluetoothDevice> = ArrayList()

    private lateinit var bluetooth: Bluetooth
    private lateinit var myUUID: UUID
    private lateinit var threadConnectBTDevice: ThreadConnectBTDevice

    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()


    @SuppressLint("MissingPermission")
    inner class ThreadConnectBTDevice constructor(device: BluetoothDevice) :
        Thread() {
        private var bluetoothSocket: BluetoothSocket? = null

        private val deviceName = device.name

        init {
            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun run() {
            var success = false
            try {
                bluetoothSocket!!.connect()
                success = true
            } catch (e: IOException){
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Couldn't connect to your device", Toast.LENGTH_SHORT).show()
                }
                try {
                    bluetoothSocket!!.close()
                } catch (e: IOException){
                    e.printStackTrace()
                }
            }
            if (success){
                bluetoothSocket.let { BluetoothSocketHolder.setBluetoothSocket(it!!) }
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection Success!", Toast.LENGTH_SHORT).show()
                    bluetoothViewModel.setBluetoothName(deviceName)
                }
            }

        }

        fun cancel() {
            try {
                bluetoothSocket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)

        bluetooth = Bluetooth(requireContext())


        listPairedDevices = bluetooth.pairedDevices
        initUi(listPairedDevices)
        return view
    }

    private fun initUi(devices: ArrayList<BluetoothDevice>) {
        initRecyclerView(devices)
        binding.btnRefresh.setOnClickListener {
            refresh()
            Toast.makeText(requireContext(), "Refreshed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun initRecyclerView(devices: ArrayList<BluetoothDevice>) {
        deviceAdapter = DeviceAdapter(devices)
        binding.rvDeviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
        deviceAdapter.setOnClickListener(object : PairedDeviceListener {
            @SuppressLint("MissingPermission")
            override fun onClick(device: BluetoothDevice) {
                Toast.makeText(requireContext(), "Connecting to ${device.name}..", Toast.LENGTH_SHORT).show()
                threadConnectBTDevice = ThreadConnectBTDevice(device)
                threadConnectBTDevice.start()
                Log.d(TAG, "onClick: $threadConnectBTDevice")
            }

        })
    }

    private fun refresh(){
        if (listPairedDevices.size > 0){
            listPairedDevices.clear()
            val updatedDevice = bluetooth.pairedDevices
            listPairedDevices.addAll(updatedDevice)
            deviceAdapter.notifyDataSetChanged()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        private const val TAG = "ListDeviceFragment"
    }
}