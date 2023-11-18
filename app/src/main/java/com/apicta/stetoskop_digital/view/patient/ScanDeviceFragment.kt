package com.apicta.stetoskop_digital.view.patient

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.apicta.stetoskop_digital.adapter.DeviceAdapter
import com.apicta.stetoskop_digital.databinding.FragmentScanDeviceBinding
import com.apicta.stetoskop_digital.listener.PairedDeviceListener
import com.psp.bluetoothlibrary.Bluetooth
import com.psp.bluetoothlibrary.BluetoothListener

class ScanDeviceFragment : Fragment() {


    private var _binding: FragmentScanDeviceBinding? = null
    private val binding get() = _binding!!

    private lateinit var bluetooth: Bluetooth

    private lateinit var deviceAdapter: DeviceAdapter

    private var listScanDevices: ArrayList<BluetoothDevice> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        bluetooth = Bluetooth(requireContext())

        initUi(listScanDevices)

        return view
    }

    private fun initUi(devices: ArrayList<BluetoothDevice>){
        initBluetooth()
        initRecyclerView(devices)
        binding.btnScan.setOnClickListener {
            clearDetectDeviceList()
            bluetooth.startDetectNearbyDevices()
        }
    }

    private fun initRecyclerView(devices: ArrayList<BluetoothDevice>) {
        deviceAdapter = DeviceAdapter(devices)
        binding.rvScanDevice.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = deviceAdapter
        }
        deviceAdapter.setOnClickListener(object : PairedDeviceListener {
            @SuppressLint("MissingPermission")
            override fun onClick(device: BluetoothDevice) {
                bluetooth.requestPairDevice(device)
            }

        })
    }

    private fun initBluetooth(){
        bluetooth.setOnDiscoveryStateChangedListener { state ->
            if (state == Bluetooth.DISCOVERY_STARTED) {
                Toast.makeText(requireContext(), "Start Scanning", Toast.LENGTH_SHORT).show()
                binding.loading.visibility = View.VISIBLE
            }
            if (state == Bluetooth.DISCOVERY_FINISHED) {
                Toast.makeText(requireContext(), "Scan Finished", Toast.LENGTH_SHORT).show()
                binding.loading.visibility = View.GONE
            }
        }
        bluetooth.setOnDetectNearbyDeviceListener(object :
            BluetoothListener.onDetectNearbyDeviceListener {
            override fun onDeviceDetected(device: BluetoothDevice) {
                deviceAdapter.addDevice(device)
            }
        })
        bluetooth.setOnDevicePairListener(object :
            BluetoothListener.onDevicePairListener{
            override fun onDevicePaired(device: BluetoothDevice?) {
                listScanDevices.remove(device)
                deviceAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(device: BluetoothDevice?) {
                Toast.makeText(requireContext(), "Cancelled pair", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun clearDetectDeviceList(){
        if (listScanDevices.size > 0){
            listScanDevices.clear()
        }
        deviceAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}