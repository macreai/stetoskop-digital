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
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.adapter.PairedDeviceAdapter
import com.apicta.stetoskop_digital.databinding.FragmentListDeviceBinding
import com.apicta.stetoskop_digital.listener.PairedDeviceListener
import com.apicta.stetoskop_digital.util.BluetoothSocketHolder
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel
import com.psp.bluetoothlibrary.Bluetooth
import com.psp.bluetoothlibrary.BluetoothListener
import java.io.IOException
import java.util.UUID

class ListDeviceFragment : Fragment() {

    private var _binding: FragmentListDeviceBinding? = null
    private val binding get()= _binding!!

//    private val REQUEST_PERMISSION = 1

    private lateinit var pairedDeviceAdapter: PairedDeviceAdapter
    private var listPairedDevices: ArrayList<BluetoothDevice> = ArrayList()

    private lateinit var bluetooth: Bluetooth
//    private lateinit var bluetoothSocket: BluetoothSocket
    private lateinit var myUUID: UUID
    private lateinit var threadConnectBTDevice: ThreadConnectBTDevice
    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

//    private lateinit var connection: Connection
//    private var deviceList: ArrayList<DeviceModel> = ArrayList()
//    private lateinit var bluetoothAdapter: BluetoothAdapter
//    private lateinit var myUUID: UUID
//    private lateinit var device: BluetoothDevice
//    private lateinit var myThreadConnectedDevice: ThreadConnectBTDevice
////    private lateinit var mutableBluetoothDevice: MutableList<BluetoothDevice>
//    private lateinit var scannedDevices: List<BluetoothDevice?>
//    private lateinit var bluetooth: Bluetooth
//
//    private val bluetoothCallback = object : BluetoothCallback{
//        override fun onBluetoothTurningOn() {
//
//        }
//
//        override fun onBluetoothOn() {
//            Toast.makeText(requireContext(), "Bluetooth Already Turn On", Toast.LENGTH_SHORT).show()
//        }
//
//        override fun onBluetoothTurningOff() {
//        }
//
//        override fun onBluetoothOff() {
//        }
//
//        override fun onUserDeniedActivation() {
//            requireActivity().finish()
//        }
//
//    }
//
//    private val discoveryCallback = object : DiscoveryCallback{
//        override fun onDiscoveryStarted() {
//            scannedDevices = emptyList()
//        }
//
//        override fun onDiscoveryFinished() {
//            TODO("Not yet implemented")
//        }
//
//        override fun onDeviceFound(device: BluetoothDevice?) {
//            scannedDevices = (scannedDevices + device)
//            listDeviceAdapter.
//        }
//
//        override fun onDevicePaired(device: BluetoothDevice?) {
//            TODO("Not yet implemented")
//        }
//
//        override fun onDeviceUnpaired(device: BluetoothDevice?) {
//            TODO("Not yet implemented")
//        }
//
//        override fun onError(errorCode: Int) {
//            TODO("Not yet implemented")
//        }
//
//    }


    @SuppressLint("MissingPermission")
    inner class ThreadConnectBTDevice constructor(device: BluetoothDevice) :
        Thread() {
        private var bluetoothSocket: BluetoothSocket? = null

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
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Connection Success!", Toast.LENGTH_SHORT).show()
                }
                bluetoothSocket.let { BluetoothSocketHolder.setBluetoothSocket(it!!) }
//                BluetoothSocketHolder.setBluetoothSocket(bluetoothSocket!!)
//                activity?.runOnUiThread {
//                    bluetoothViewModel.setBluetoothSocket(bluetoothSocket!!)
//                }
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

//    private val connectionListener: BluetoothListener.onConnectionListener = object :
//        BluetoothListener.onConnectionListener {
//        override fun onConnectionStateChanged(socket: BluetoothSocket?, state: Int) {
//            when(state){
//                Connection.CONNECTING -> {
//                    Toast.makeText(requireContext(), "Connecting...", Toast.LENGTH_SHORT).show()
//                }
//                Connection.CONNECTED -> {
//                    Toast.makeText(requireContext(), "Connected!", Toast.LENGTH_SHORT).show()
//                }
//                Connection.DISCONNECTED -> {
//                    Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
////
//        override fun onConnectionFailed(errorCode: Int) {
//            when(errorCode){
//                Connection.SOCKET_NOT_FOUND -> {
//                    Toast.makeText(requireContext(), "Socket not Found", Toast.LENGTH_SHORT).show()
//                }
//                Connection.CONNECT_FAILED -> {
//                    Toast.makeText(requireContext(), "Connect failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }
//    }
//    private val receiveListener: BluetoothListener.onReceiveListener = object :
//        BluetoothListener.onReceiveListener {
//        override fun onReceived(receivedData: String?) {
//            Log.d(TAG, "onReceived: $receivedData")
//            val intValue = receivedData?.toUInt(8)
//            Log.d(TAG, "intValue: $intValue")
//            receivedData.let {
//                try {
//                    val intValue = it?.toInt()
//                    Log.d(TAG, "intValue: $intValue")
//                } catch (e: NumberFormatException){
//                    Log.e(TAG, "Failed to Parse to int: $receivedData", )
//                }
//            }
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == Activity.RESULT_OK){
//            Toast.makeText(requireContext(), "Bluetooth On", Toast.LENGTH_SHORT).show()
//        }
//        if (requestCode == Activity.RESULT_CANCELED){
//            Toast.makeText(
//                requireContext(),
//                "Bluetooth turn on dialog canceled",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListDeviceBinding.inflate(inflater, container, false)
        val view = binding.root

        val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)

//        if (activity?.packageManager?.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) == false) {
//            Toast.makeText(requireContext(), "Your device does not support Bluetooth", Toast.LENGTH_LONG).show()
//            activity?.finish()
//            return view
//        }
//
//        val UUID_STRING_WELL_KNOWN_SPP = "00001101-0000-1000-8000-00805F9B34FB"
//        myUUID = UUID.fromString(UUID_STRING_WELL_KNOWN_SPP)
//
//        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
//        if (bluetoothAdapter == null) {
//            activity?.finish()
//        }
//
//        fragment = supportFragmentManager
//
//
//        binding.btnScan.setOnClickListener{
//        }
        bluetooth = Bluetooth(requireContext())
//        bluetooth.setCallbackOnUI(requireActivity())
//        bluetooth.setBluetoothCallback(bluetoothCallback)


//        mutableBluetoothDevice = mutableListOf()
        listPairedDevices = bluetooth.pairedDevices
        initBluetooth()
        initRecyclerView(listPairedDevices)
        scan()

        return view
    }

//    private fun checkBtPermission(): Boolean {
//        if (checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(
//                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
//                REQUEST_PERMISSION
//            )
//        }
//        val check = checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT)
//        return check == PackageManager.PERMISSION_GRANTED
//    }

//    override fun onStart() {
//        super.onStart()
//        checkPermission()
//
//        if (!bluetoothAdapter!!.isEnabled){
//            if (checkBtPermission()){
//                val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
//                startActivityForResult(enableIntent, REQUEST_PERMISSION)
//            }
//        }
//        setupBluetooth()
//    }
//
//    private fun setupBluetooth() {
//        if (checkBtPermission()) {
//            val pairedDevices = bluetoothAdapter!!.bondedDevices
//            if (pairedDevices.size > 0) {
//                listPairedDevices = ArrayList()
//                deviceList = ArrayList()
//                listPairedDevices?.addAll(pairedDevices)
//                for (item in listPairedDevices!!) {
//                    deviceList?.add(DeviceModel(item.name, item.address))
//                }
//
//                setupDeviceList(deviceList!!)
//
//                pairedDeviceAdapter.setOnClickListener(object: PairedDeviceListener{
//                    //                    override fun onDeviceClick(pos: Int) {
////                        device = listPairedDevices?.get(pos)
////                        if (checkBtPermission()) {
////                            Toast.makeText(
////                                this@ConnectBluetoothActivity,
////                                "Connectiong to  " + device!!.name,
////                                Toast.LENGTH_LONG
////                            ).show()
////                        }
////
////                        myThreadConnectBTdevice = device?.let { ThreadConnectBTdevice(it) }
////                        myThreadConnectBTdevice?.start()
////                    }
//                    override fun onClick(position: Int) {
//                        device = listPairedDevices[position]
//                        if (checkBtPermission()) {
//                            Toast.makeText(requireContext(), "Connecting to " + device.name, Toast.LENGTH_SHORT).show()
//                        }
//                        myThreadConnectedDevice= ThreadConnectBTDevice(device)
//                        myThreadConnectedDevice.start()
//                    }
//                })
//            }
//        }
//    }

//    private fun setupDeviceList(list: ArrayList<DeviceModel>) {
//        pairedDeviceAdapter = PairedDeviceAdapter(list)
////        layoutManager = LinearLayoutManager(requireContext())
//        binding.rvDeviceList.layoutManager = LinearLayoutManager(requireContext())
//        binding.rvDeviceList.adapter = pairedDeviceAdapter
//    }
//
//    private fun checkPermission(): Boolean {
//        if (checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION
//            )
//        }
//        val check =
//            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
//        return check == PackageManager.PERMISSION_GRANTED
//    }

    private fun initBluetooth(){
        bluetooth = Bluetooth(requireContext())
        bluetooth.turnOnWithPermission(requireActivity())
        bluetooth.setOnDiscoveryStateChangedListener(object :
            BluetoothListener.onDiscoveryStateChangedListener {
            override fun onDiscoveryStateChanged(state: Int) {
                if (state == Bluetooth.DISCOVERY_STARTED){
                    Toast.makeText(requireContext(), "Discovery Started", Toast.LENGTH_SHORT).show()
                }

                if (state == Bluetooth.DISCOVERY_FINISHED){
                    Toast.makeText(requireContext(), "Discovery Finished", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
//
    private fun initRecyclerView(devices: ArrayList<BluetoothDevice>) {
        bluetooth.setOnDetectNearbyDeviceListener(object :
            BluetoothListener.onDetectNearbyDeviceListener {
            override fun onDeviceDetected(device: BluetoothDevice) {
                pairedDeviceAdapter.addDevice(device)
            }
        })
        pairedDeviceAdapter = PairedDeviceAdapter(devices)
        binding.rvDeviceList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = pairedDeviceAdapter
        }
        pairedDeviceAdapter.setOnClickListener(object : PairedDeviceListener {
            @SuppressLint("MissingPermission")
            override fun onClick(device: BluetoothDevice) {
                Toast.makeText(requireContext(), "Connecting to ${device.name}..", Toast.LENGTH_SHORT).show()
                threadConnectBTDevice = ThreadConnectBTDevice(device)
                threadConnectBTDevice.start()
                Log.d(TAG, "onClick: $threadConnectBTDevice")
                bluetoothViewModel.setBluetoothName(device.name)
//                connectDevice(device, connectionListener, receiveListener)
//                bluetooth.setOnDevicePairListener(object : BluetoothListener.onDevicePairListener{
//                    @SuppressLint("MissingPermission")
//                    override fun onDevicePaired(device: BluetoothDevice?) {
////                        bluetoothSocket = device!!.createRfcommSocketToServiceRecord(myUUID)
////                        bluetoothSocket.connect()
////                        bluetoothSocket.let { BluetoothSocketHolder.setBluetoothSocket(it) }
////                        connectionListener.onConnectionStateChanged(bluetoothSocket, Connection.CONNECTING)
//                        threadConnectBTDevice = device?.let { ThreadConnectBTDevice(it) }!!
//                        threadConnectBTDevice.start()
//                        Log.d(TAG, "onDevicePaired: $bluetoothSocket")
//                        Toast.makeText(requireContext(), "Successfully Paired", Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onCancelled(device: BluetoothDevice?) {
//                        Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_SHORT).show()
//                    }
//
//                })
//                if (bluetooth.requestPairDevice(device)){
////                    val mBundle: Bundle = Bundle()
////                    mBundle.putString(EXTRA_DEVICE_ADDRESS, device.address)
////                    Toast.makeText(requireContext(), "Connect Success", Toast.LENGTH_SHORT).show()
//                    view?.findNavController()?.navigate(R.id.action_listDeviceFragment_to_recordPatientFragment)
//                } else {
//                    Toast.makeText(requireContext(), "Failed to Connect", Toast.LENGTH_SHORT).show()
//                }
            }

        })
    }

//    private fun connectDevice(device: BluetoothDevice, onConnectionListener: BluetoothListener.onConnectionListener, receiveListener: BluetoothListener.onReceiveListener) {
//        connection = Connection(requireContext())
//        connection.connect(device, true, onConnectionListener, receiveListener)
//    }
//

    private fun scan(){
        binding.btnScan.setOnClickListener {
//            clearDetectDeviceList()
//            bluetooth.startDetectNearbyDevices()
            view?.findNavController()?.navigate(R.id.action_listDeviceFragment_to_recordPatientFragment)
        }
    }
//
    private fun clearDetectDeviceList(){
        if (listPairedDevices.size > 0){
            listPairedDevices.clear()
        }
        pairedDeviceAdapter.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object{
        private const val TAG = "ListDeviceFragment"
        const val EXTRA_DEVICE_ADDRESS = "extra_device_address"
    }
}