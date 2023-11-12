package com.apicta.stetoskop_digital.view.patient

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.ContentValues
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Audio.Media
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.apicta.stetoskop_digital.audio.ArrayStandardization
import com.apicta.stetoskop_digital.audio.FloatArrayListToWav
import com.apicta.stetoskop_digital.audio.PcmToWav
import com.apicta.stetoskop_digital.databinding.FragmentRecordPatientBinding
import com.apicta.stetoskop_digital.util.ArrayReceiver
import com.apicta.stetoskop_digital.util.BluetoothSocketHolder
import com.apicta.stetoskop_digital.util.Wav
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecordPatientFragment : Fragment()/*, OnSocketConnectedListener */ {

    private var _binding: FragmentRecordPatientBinding? = null
    private val binding get() = _binding!!

    private var myThreadConnected: ThreadConnected? = null

    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()

    private var startTime: Long = 0

    @SuppressLint("MissingPermission")
    inner class ThreadConnected(socket: BluetoothSocket, switch: Boolean): Thread(){
        private val mmSocket = socket
        private val mmInStream: InputStream = socket.inputStream
        private val mmBuffer: ByteArray = ByteArray(BUFFER_SIZE)

        private val byteArrayOutputStream = ByteArrayOutputStream()
        private lateinit var data: ByteArray

        private var isOn = switch

        private var dataSize = 0

        override fun run() {

            var numBytes: Int = 0

            while (isOn){
                try {
                    numBytes = mmInStream.read(mmBuffer)
                    byteArrayOutputStream.write(mmBuffer, 0, numBytes)
                } catch (e: IOException){
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                dataSize += numBytes
                Log.d(TAG, "numBytes: $numBytes")
                Log.d(TAG, "dataSize: $dataSize")

                activity?.runOnUiThread {

                    val currentTime = System.currentTimeMillis()
                    if (startTime == 0L){
                        startTime = currentTime
                    }
                    val elapsedTime = currentTime - startTime
                    val elapsedTimeFloat = millisToSeconds(elapsedTime)

                    if (elapsedTimeFloat >= 10.0){
                        startTime = currentTime
                        Log.d(TAG, "pcgArray: ${ArrayReceiver.pcgArray}")
                        Log.d(TAG, "timeArray: ${ArrayReceiver.timeArray}")
                        isOn = false
                        
                        val contentValues = ContentValues()
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}-${binding.fileName.text}.wav")
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")

                        val contentResolver = context?.contentResolver
                        val uri = contentResolver?.insert(Media.EXTERNAL_CONTENT_URI, contentValues)

                        val desiredByteCount = ArrayReceiver.timeArray.last().toInt() * SAMPLE_RATE
                        Log.d(TAG, "desiredByCount: $desiredByteCount")
                        Log.d(TAG, "lastTime: ${ArrayReceiver.timeArray.last().toInt()}")

                        data = byteArrayOutputStream.toByteArray()
                        val wavData: ByteArray = Wav.generateWavHeader(data, CHANNEL_CONFIG, SAMPLE_RATE, AUDIO_FORMAT)

                        try {
                            val outputStream: OutputStream = contentResolver?.openOutputStream(uri!!)!!
                            if (desiredByteCount <= wavData.size){
//                                outputStream.write(wavData)
                                outputStream.write(wavData, 0, desiredByteCount)
                                Log.d(TAG, "run: execute A")
                            } else {
                                outputStream.write(wavData)
                                Log.d(TAG, "run: execute B")
                            }
                            outputStream.close()
                            Log.d(TAG, "File saved to: $uri")
                            Toast.makeText(requireContext(), "File saved to: $uri", Toast.LENGTH_SHORT).show()
                        } catch (e: IOException){
                            e.printStackTrace()
                        }


//                        uri?.let {  mediaUri ->
//                            try {
//                                byteArrayOutputStream.use {  byteArrayOutputStream ->
//                                    data = byteArrayOutputStream.toByteArray()
//
//                                    mmFileOutStream = FileOutputStream("${System.currentTimeMillis()}_${binding.fileName.text!!.trim().toString()}")
//                                    Wav.writeWavHeader(mmFileOutStream, CHANNEL_CONFIG, SAMPLE_RATE, AUDIO_FORMAT)
//                                    mmFileOutStream.write(data)
//                                    mmFileOutStream.close()
//
//                                    Log.d(TAG, "File saved to: $mmFileOutStream")
//                                    Toast.makeText(requireContext(), "File saved to: $mmFileOutStream", Toast.LENGTH_SHORT).show()
//                                }
//                            } catch (e: IOException){
//                                e.printStackTrace()
//                            }
//                        }

//                        uri?.let { mediaUri ->
//                            try {
//                                contentResolver.openOutputStream(mediaUri).use { outputStream ->
//                                    FloatArrayListToWav.createWavHeader(outputStream, SAMPLE_RATE, 8, CHANNEL_CONFIG, ArrayReceiver.timeArray.last().toInt())
//                                    byteArrayOutputStream.writeTo(outputStream)
//                                }
//                                Log.d(TAG, "File saved to: $mediaUri")
//                                Toast.makeText(requireContext(), "File saved to: $mediaUri", Toast.LENGTH_SHORT).show()
//                            } catch (e: IOException){
//                                e.printStackTrace()
//                            } finally {
//                                byteArrayOutputStream.close()
//                            }
//                        }

                        // write the wav file here with mediastore

//                        val contentResolver = context?.contentResolver
//                        val uri = contentResolver?.insert(Media.EXTERNAL_CONTENT_URI, contentValues)

//                        uri?.let { mediaUri ->
//                            try {
//                                val outputStream = contentResolver.openOutputStream(mediaUri)
//
//                                outputStream?.use { stream ->
//                                    Log.d(TAG, "stream: $stream")
//                                    FloatArrayListToWav.createWavHeader(stream, SAMPLE_RATE, 8, CHANNEL_CONFIG, ArrayReceiver.timeArray.last().toInt())
//                                    // write here
//                                }
//                                Log.d(TAG, "File saved to: $mediaUri")
//                                Toast.makeText(requireContext(), "File saved to: $mediaUri", Toast.LENGTH_SHORT).show()
//                            } catch (e: IOException) {
//                                e.printStackTrace()
//                            }
//                        }
                    }

                    val receivedInt = ByteBuffer.wrap(mmBuffer, 0, numBytes).order(ByteOrder.LITTLE_ENDIAN).int
                    Log.d(TAG, "received: ${receivedInt.toFloat()}")
                    ArrayReceiver.pcgArray.add(receivedInt.toFloat())
                    ArrayReceiver.timeArray.add(elapsedTimeFloat)

                    updateChart()
                }
            }
        }

        fun cancel(){
            try {
                mmSocket.close()
            } catch (e: IOException){
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordPatientBinding.inflate(inflater, container, false)
        val view = binding.root

//        val deviceAddress = arguments?.getString(ListDeviceFragment.EXTRA_DEVICE_ADDRESS)

//        initUi()

        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewChart()


        binding.date.text = formatDate(LocalDateTime.now())
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (checkBtPermission()){
            if (bluetoothAdapter.isEnabled){
                val connectedDevices = bluetoothAdapter.getProfileProxy(requireContext(), object : BluetoothProfile.ServiceListener{
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        if (profile == BluetoothProfile.HEADSET){
                            val devices = proxy.connectedDevices
                            for (device in devices){
                                if (checkBtPermission()){
                                    val deviceName = device.name
                                    binding.deviceName.text = deviceName
                                }
                            }
                        }
                        bluetoothAdapter.closeProfileProxy(profile, proxy)
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        binding.deviceName.text = "Disconnected"
                    }
                }, BluetoothProfile.HEADSET)

                if (connectedDevices){
                    bluetoothViewModel.bluetoothName.observe(viewLifecycleOwner, Observer {
                        binding.deviceName.text = it
                    })
                } else {
                    binding.deviceName.text = "No Device Connected"
                }
            } else {
                binding.deviceName.text = "Bluetooth is not enabled"
            }
        }
        val socket = BluetoothSocketHolder.getBluetoothSocket()
//        val socket = bluetoothViewModel.bluetoothSocket.value
        Log.d(TAG, "onViewCreated: $socket")
        binding.record.setOnClickListener {
            if (binding.fileName.text?.isEmpty() == true){
                binding.fileName.requestFocus()
                binding.fileName.error = "please insert file name first"
            } else {
                if (socket != null){
                    myThreadConnected = ThreadConnected(socket, true)
                    myThreadConnected!!.start()
                } else {
                    Toast.makeText(requireContext(), "Connect to  your device first!", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.stop.setOnClickListener {
            myThreadConnected?.cancel()
//            val contextWrapper = ContextWrapper(context)
////            val externalStorage: File = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC)
//            val externalStorage: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
//            } else {
//                contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
//            }
//            val audioDirPath = externalStorage.absolutePath
//            val latestWavFile = getLatestWavFile(audioDirPath)
//            Log.d(TAG, "onViewCreated: $audioDirPath")
////            if (latestWavFile != null){
////                val requestWavFile = latestWavFile.
////            }
        }
    }


    private fun getLatestWavFile(audioDirPath: String): File? {
        val dir = File(audioDirPath)

        if (dir.exists() && dir.isDirectory){
            val wavFiles = dir.listFiles{ file -> file.isFile && file.extension == "wav"}

            if (wavFiles != null){
                if (wavFiles.isNotEmpty()){
                    val sortedWavFiles = wavFiles.sortedByDescending { it.lastModified() }

                    return sortedWavFiles[0]
                }
            }
        }
        return null
    }

    @SuppressLint("NewApi")
    override fun onStart() {
        super.onStart()
        checkPermission()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDate(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
        return localDateTime.format(formatter)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkBtPermission(): Boolean {
        if (requireActivity().checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION
            )
        }
        val check = ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)
        return check == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermission(): Boolean {
        if (requireContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION
            )
        }
        val check =
            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        return check == PackageManager.PERMISSION_GRANTED
    }

    private fun isExtStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    private fun initViewChart(){
        binding.signalView.description.text = "Raw PCG"
        binding.signalView.setNoDataText("Record Not Started")
    }

    private fun updateChart() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((time, audio) in ArrayReceiver.timeArray.zip(ArrayReceiver.pcgArray)) {
            if (time <= 10.0){
                    dataValues.add(Entry(time, audio))
                }
        }

        val lineDataset1 = LineDataSet(dataValues, "PCG")
        lineDataset1.setDrawCircles(false)

        val dataset: ArrayList<ILineDataSet> = ArrayList()
        dataset.add(lineDataset1)

        binding.signalView.xAxis.isEnabled = false
        binding.signalView.axisLeft.isEnabled = false
        binding.signalView.axisRight.isEnabled = false

        binding.signalView.xAxis.axisMinimum = 0f
        binding.signalView.xAxis.axisMaximum = 10f

        val data = LineData(dataset)
        binding.signalView.data = data
        binding.signalView.notifyDataSetChanged()
        binding.signalView.invalidate()
    }

    fun millisToSeconds(millis: Long): Float {
        return (millis / 1000.0).toFloat().let { "%.3f".format(it).toFloat() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        ArrayReceiver.pcgArray.clear()
        ArrayReceiver.timeArray.clear()
    }
    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        private const val TAG = "Record Patient Fragment"
        private const val REQUEST_PERMISSION = 1
        private const val SAMPLE_RATE = 36000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        // encoding float ok but time not ok
        //
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
//        private const val BUFFER_SIZE = 1024


    }
}