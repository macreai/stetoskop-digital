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
import android.media.MediaRecorder.AudioSource
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
import com.apicta.stetoskop_digital.audio.FloatArrayListToWav.shortToBytes
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

        private lateinit var file: File
        private lateinit var mmFileOutStream: FileOutputStream
        private val contextWrapper = ContextWrapper(requireContext())

        private var isOn = switch

//        private val byteArrayOutputStream = ByteArrayOutputStream()


        override fun run() {


            var numBytes: Int

            while (isOn){
                numBytes = try {
                    mmInStream.read(mmBuffer)
                } catch (e: IOException){
                    Log.d(TAG, "Input stream was disconnected", e)
                    break
                }
                Log.d(TAG, "numBytes: $numBytes")

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
//                        ArrayReceiver.pcgArray.clear()
//                        ArrayReceiver.timeArray.clear()
                        isOn = false

                        ArrayReceiver.pcgArrayNormalize = ArrayStandardization.standardizationFloatArrayList(ArrayReceiver.pcgArray)
                        Log.d(TAG, "pcgArrayNormalize: ${ArrayReceiver.pcgArrayNormalize}")

                        val contentValues = ContentValues()
                        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "${System.currentTimeMillis()}-${binding.fileName.text}.wav")
                        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/x-wav")

                        val contentResolver = context?.contentResolver
                        val uri = contentResolver?.insert(Media.EXTERNAL_CONTENT_URI, contentValues)

                        uri?.let { mediaUri ->
                            try {
                                val outputStream = contentResolver.openOutputStream(mediaUri)
                                outputStream?.use { stream ->
                                    FloatArrayListToWav.writeWavHeader(stream, ArrayReceiver.pcgArray.size * 8, SAMPLE_RATE, 8)
                                    for (value in ArrayReceiver.pcgArrayNormalize) {
                                        val shortValue = (value * 32767.0f).toInt().toShort()
                                        val bytes: ByteArray = shortToBytes(shortValue)
                                        stream.write(bytes)
                                    }
                                }
                                Log.d(TAG, "File saved to: $mediaUri")
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                        val externalStorage: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
                        } else {
                            contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
                        }
                        Log.d(TAG, "externalStorage: $externalStorage")
                        file = File(externalStorage, System.currentTimeMillis().toString() + "-" + binding.fileName.text.toString() + ".wav")
                        try {
                            mmFileOutStream = FileOutputStream(file)

                            FloatArrayListToWav.writeWavHeader(mmFileOutStream, ArrayReceiver.pcgArray.size * 8, SAMPLE_RATE, 8)

                            for (value in ArrayReceiver.pcgArrayNormalize) {
                                val shortValue =
                                    (value * 32767.0f).toInt().toShort()
                                val bytes: ByteArray = shortToBytes(shortValue)
                                mmFileOutStream.write(bytes)
                            }

                            mmFileOutStream.close()

                        } catch (e: IOException){
                            e.printStackTrace()
                        }
//                        return@runOnUiThread
                    }

                    val receivedInt = ByteBuffer.wrap(mmBuffer, 0, numBytes).order(ByteOrder.LITTLE_ENDIAN).int
                    Log.d(TAG, "received: ${receivedInt.toFloat()}")
                    ArrayReceiver.pcgArray.add(receivedInt.toFloat())
                    ArrayReceiver.timeArray.add(elapsedTimeFloat)

                    updateChart()
                }
            }
//            super.run()
        }

        private fun getOutputPath(): String {
            val storageDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context?.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)
            } else {
                context?.getExternalFilesDir(Environment.DIRECTORY_MUSIC)
            }

            val fileName = "${System.currentTimeMillis()}_BluetoothRecording.wav"
            return "${storageDir?.absolutePath}/$fileName"
        }

        private fun saveToMediaStore(filePath: String) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "BluetoothRecording.wav")
                put(MediaStore.MediaColumns.MIME_TYPE, "audio/x-wav")
            }

            val contentResolver = context?.contentResolver
            val uri = contentResolver?.insert(Media.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let { mediaUri ->
                try {
                    val outputStream = contentResolver.openOutputStream(mediaUri)
                    outputStream?.use { stream ->
                        val file = File(filePath)
                        val fileInputStream = file.inputStream()
                        fileInputStream.copyTo(stream)
                    }
                    Log.d("BluetoothDataRecorder", "File saved to: $mediaUri")
                } catch (e: IOException) {
                    Log.e("BluetoothDataRecorder", "Error saving to MediaStore", e)
                }
            }
        }

//        suspend fun cancel() = withContext(Dispatchers.IO) {
//            try {
//                mmSocket.close()
//                val receivedData = byteArrayOutputStream.toByteArray()
//                Log.d(TAG, "cancel: ${decodeByteArrayToInt(receivedData)}")
//
//                val wavData = PcmToWav.pcmToWav(receivedData, 1, SAMPLE_RATE, 8)
//                val externalStorage: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
//                } else {
//                    contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
//                }
//
//                try {
//                    val outputFile = File(externalStorage, "${binding.fileName}_${System.currentTimeMillis()}.wav")
//                    val deferredResult = async {
//                        FileOutputStream(outputFile).use { fos ->
//                            fos.write(wavData)
//                        }
//                        Log.d(TAG, "cancel: finish creating wav file")
//                    }
//
//                    deferredResult.await() // Menunggu hingga file WAV selesai dibuat
//                    withContext(Dispatchers.Main) {
//                        activity?.runOnUiThread {
//                            Toast.makeText(requireContext(), "File saved successfully", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                } catch (e: Exception) {
//                    withContext(Dispatchers.Main) {
//                        activity?.runOnUiThread {
//                            Toast.makeText(requireContext(), "Cannot create wav file", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    Log.d(TAG, "cancel: $e")
//                }
//            } catch (e: IOException) {
//                Log.e(TAG, "Could not close the connect socket", e)
//            }
//        }


        fun cancel(){
            try {
                mmSocket.close()
////                // Setelah memanggil cancel, Anda dapat mengambil data dari byteArrayOutputStream
////                val receivedData = byteArrayOutputStream.toByteArray()
////                Log.d(TAG, "cancel: ${decodeByteArrayToInt(receivedData)}")
////
////                GlobalScope.launch(Dispatchers.IO) {
////                    try {
////                        // Sekarang Anda dapat mengkonversi data ini menjadi file WAV, jika diperlukan
////                        val wavData = PcmToWav.pcmToWav(receivedData, 1, SAMPLE_RATE, 8)
////
////                        // Simpan ke file WAV jika diperlukan
//////                val file = File(Environment.getExternalStorageDirectory(), "${binding.fileName}_${System.currentTimeMillis()}.wav")
////                        val externalStorage: File = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
////                            contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
////                        } else {
////                            contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
////                        }
////                        try {
////                            val outputFile = File(externalStorage, "${binding.fileName}_${System.currentTimeMillis()}.wav")
////                            FileOutputStream(outputFile).use { fos ->
////                                fos.write(wavData)
////                            }
////                            deferredResult.await()
////                            Log.d(TAG, "cancel: finish creating wav file")
////                            withContext(Dispatchers.Main) {
////                                // Any UI updates or further processing after file save
////                                // For example, show a toast message.
////                                activity?.runOnUiThread {
////                                    Toast.makeText(requireContext(), "File saved successfully", Toast.LENGTH_SHORT).show()
////                                }}
////                        } catch (e: Exception){
////                            withContext(Dispatchers.Main){
////                                activity?.runOnUiThread {
////                                    Toast.makeText(requireContext(), "Cannot create wav file", Toast.LENGTH_SHORT).show()
////                                }
////                            }
////                            Log.d(TAG, "cancel: $e")
////                        }
//////                val outputFile = File(externalStorage, "${binding.fileName}_${System.currentTimeMillis()}.wav")
//////                FileOutputStream(outputFile).use { fos ->
//////                    fos.write(wavData)
//////                }
////                    } catch (e: Exception){
////                        withContext(Dispatchers.Main){
////                            activity?.runOnUiThread {
////                                Toast.makeText(requireContext(), "Error saving file: ${e.message}", Toast.LENGTH_SHORT).show()
////                            }
////                        }
////                        Log.d(TAG, "cancel: ${e.message}")
////                    }
////                }
            } catch (e: IOException){
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }

    }

//    @RequiresApi(Build.VERSION_CODES.M)
//    inner class ThreadConnected(socket: BluetoothSocket): Thread() {
//        private val connectedInputStream: InputStream?
//        private val connectedOutputStream: FileOutputStream?
//        private val connectedSocket: BluetoothSocket
//        private val buffer = ByteArray(BUFFER_SIZE)
//
//        init {
//            var `in`: InputStream? = null
//            var out: FileOutputStream? = null
//            connectedSocket = socket
//            try {
//                `in` = socket.inputStream
//            } catch (e: IOException){
//                e.printStackTrace()
//            }
//
//            if (isExtStorageWritable() && checkPermission()){
//                try {
//                    f
////                    val audioDir = contextWrapper.getExternalFilesDir("sample wav")
//                    val audioDir = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
//                    } else {
//                        contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
//                    }
////                    val files = File(audioDir, "sample.wav")
//                    val files = File(audioDir, System.currentTimeMillis().toString() + "-" + binding.fileName.text.toString() + ".wav")
//                    out = FileOutputStream(files)
//                    writeWavHeader(out)
//                } catch (e: Exception){
//                    Toast.makeText(requireContext(), "$e", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(requireContext(), "Can't write data", Toast.LENGTH_SHORT).show()
//            }
//            connectedInputStream = `in`
//            connectedOutputStream = out
//        }
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        override fun run() {
////            val buffer = ByteArray(BUFFER_SIZE)
//            var bytesRead = 0
//            var audioLength: Long = 0
//            val byteArrayOutputStream = ByteArrayOutputStream()
//
//            Log.d(TAG, "run: $buffer")
//
//            audioLength += (44 - 8).toLong()
//            try {
//                while (bytesRead != -1){
//                    bytesRead = connectedInputStream!!.read(buffer)
//                    Log.d(TAG, "run bytesRead: $bytesRead")
//                    if (bytesRead != -1) {
//                        connectedOutputStream!!.write(buffer, 0, bytesRead)
//                        byteArrayOutputStream.write(buffer, 0, bytesRead)
//                        audioLength += bytesRead.toLong()
//                    }
//                }
//            } catch (e: IOException){
//                try {
//                    val contextWrapper = ContextWrapper(requireContext())
////                    val file = contextWrapper.getExternalFilesDir("sample wav")
//                    val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                        contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
//                    } else {
//                        contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
//                    }
//                    val audioFileAbsolutePath = file.absolutePath + "/sample.wav"
//                    val normalizeAudio = NormalizeAudio()
//                    normalizeAudio.normalize(audioFileAbsolutePath, binding.fileName.text.toString(), requireContext())
//                    connectedOutputStream!!.close()
//                } catch (e: IOException){
//                    throw RuntimeException(e)
//                }
//                e.printStackTrace()
//            }
//        }
//
//        @RequiresApi(Build.VERSION_CODES.O)
//        fun cancel(){
//            try {
//                val contextWrapper = ContextWrapper(requireContext())
////                val file = contextWrapper.getExternalFilesDir("sample wav")
//                val file = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    contextWrapper.getExternalFilesDir(Environment.DIRECTORY_RECORDINGS)!!
//                } else {
//                    contextWrapper.getExternalFilesDir(Environment.DIRECTORY_MUSIC)!!
//                }
//                val audioFileAbsolutePath = file.absolutePath +"/sample.wav"
//                val normalizeAudio = NormalizeAudio()
//                normalizeAudio.normalize(audioFileAbsolutePath, binding.fileName.text.toString(), requireContext())
//                connectedOutputStream!!.close()
//                connectedSocket.close()
//            } catch (e: IOException){
//                e.printStackTrace()
//            }
//        }
//    }

//    private var startTime: Long = 0

//    val timeArrayChart: ArrayList<Float> by lazy { ArrayList() }
//    val audioChart: ArrayList<Float> by lazy { ArrayList() }

//    private lateinit var connection: Connection
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
//
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
//            if (receivedData!!.trim().isDigitsOnly()){
//                val currentTime = System.currentTimeMillis()
//                if (startTime == 0L){
//                    startTime = currentTime
//                }
//                val elapsedTime = currentTime - startTime
//                val elapsedTimeFloat = millisToSeconds(elapsedTime)
//
//                if (elapsedTime >= 15.0){
//                    startTime = currentTime
//                    timeArrayChart.clear()
//                    audioChart.clear()
//                }
//                Log.d(TAG, "onReceived: Sampai Sini")
//                audioChart.add(receivedData.toFloat())
//                timeArrayChart.add(elapsedTimeFloat)
//                Log.d(TAG, "audioChart: $audioChart")
//                Log.d(TAG, "timeArrayChart: $timeArrayChart")
//
//                updateChart()
//            }
//        }
//    }

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

//    private fun writeWavHeader(outputStream: FileOutputStream) {
//        val audioDataLength = outputStream.channel.size() - 44 // Subtract header size
//        val overallSize = audioDataLength + 36 // Add header size
//        val header = ByteArray(44)
//
//        // RIFF chunk descriptor
//        header[0] = 'R'.code.toByte()
//        header[1] = 'I'.code.toByte()
//        header[2] = 'F'.code.toByte()
//        header[3] = 'F'.code.toByte()
//
//        // Overall size (file size - 8 bytes for RIFF and WAVE tags)
//        header[4] = (overallSize and 0xffL).toByte()
//        header[5] = (overallSize shr 8 and 0xffL).toByte()
//        header[6] = (overallSize shr 16 and 0xffL).toByte()
//        header[7] = (overallSize shr 24 and 0xffL).toByte()
//
//        // WAVE chunk
//        header[8] = 'W'.code.toByte()
//        header[9] = 'A'.code.toByte()
//        header[10] = 'V'.code.toByte()
//        header[11] = 'E'.code.toByte()
//
//        // fmt sub-chunk
//        header[12] = 'f'.code.toByte() // Sub-chunk identifier
//        header[13] = 'm'.code.toByte()
//        header[14] = 't'.code.toByte()
//        header[15] = ' '.code.toByte() // Chunk size
//        header[16] = 16
//        header[17] = 0
//        header[18] = 0
//        header[19] = 0
//        // Audio format (PCM = 1)
//        header[20] = 1
//        header[21] = 0
//
//        // Number of channels (2 = stereo)
//        header[22] = (if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2).toByte()
//        header[23] = 0
//
//        // Sample rate
//        header[24] = (SAMPLE_RATE and 0xff).toByte()
//        header[25] = (SAMPLE_RATE shr 8 and 0xff).toByte()
//        header[26] = (SAMPLE_RATE shr 16 and 0xff).toByte()
//        header[27] = (SAMPLE_RATE shr 24 and 0xff).toByte()
//
//        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
//        val byteRate = SAMPLE_RATE * (if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 2 else 1
//        header[28] = (byteRate and 0xff).toByte()
//        header[29] = (byteRate shr 8 and 0xff).toByte()
//        header[30] = (byteRate shr 16 and 0xff).toByte()
//        header[31] = (byteRate shr 24 and 0xff).toByte()
//
//        // Block align (Number of channels * Bits per sample / 8)
//        header[32] = ((if (CHANNEL_CONFIG == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 2 else 1).toByte()
//        header[33] = 0
//
//        // Bits per sample
//        header[34] = (if (AUDIO_FORMAT == AudioFormat.ENCODING_PCM_16BIT) 16 else 8).toByte()
//        header[35] = 0
//
//        // data sub-chunk
//        header[36] = 'd'.code.toByte() // Sub-chunk identifier
//        header[37] = 'a'.code.toByte()
//        header[38] = 't'.code.toByte()
//        header[39] = 'a'.code.toByte() // Chunk size
//        header[40] = (audioDataLength and 0xffL).toByte()
//        header[41] = (audioDataLength shr 8 and 0xffL).toByte()
//        header[42] = (audioDataLength shr 16 and 0xffL).toByte()
//        header[43] = (audioDataLength shr 24 and 0xffL).toByte()
//        outputStream.write(header)
//    }

//    private fun initUi() {
//        initViewChart()
//        binding.record.setOnClickListener {
////            connectDevice(deviceAddress, connectionListener, receiveListener)
//        }
//
//    }

    private fun initViewChart(){
        binding.signalView.description.text = "Raw PCG"
        binding.signalView.setNoDataText("Record Not Started")
    }

//    private fun updateChart() {
//        activity?.runOnUiThread {
//            val dataValues: ArrayList<Entry> = ArrayList()
//            for ((time, audio) in ArrayReceiver.timeArray.zip(ArrayReceiver.pcgArray)){
//                if (time <= 3){
//                    dataValues.add(Entry(time, audio))
//                }
//            }
//            val lineDataset = LineDataSet(dataValues, "PCG")
//            lineDataset.setDrawCircles(false)
//            lineDataset.setDrawValues(false)
//
//            val dataset: ArrayList<ILineDataSet> = ArrayList()
//            dataset.add(lineDataset)
//
//            val data = LineData(dataset)
//            binding.signalView.data = data
//
//            val lastXValue = ArrayReceiver.pcgArray.last()
//            if (lastXValue >= 5f) {
//                val nextMultipleOf5 = ((lastXValue / 5).toInt() + 1) * 5
//                binding.signalView.setVisibleXRange(5f, nextMultipleOf5.toFloat())
//            } else {
//                binding.signalView.setVisibleXRange(0f, 2f)
//            }
//
//            binding.signalView.xAxis.isEnabled = true
//            binding.signalView.axisLeft.isEnabled = false
//            binding.signalView.axisRight.isEnabled = false
//            binding.signalView.invalidate()
//        }
//    }

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
//
//    private fun connectDevice(deviceAddress: String, onConnectionListener: BluetoothListener.onConnectionListener, receiveListener: BluetoothListener.onReceiveListener) {
////        connection = Connection(requireContext())
////        connection.connect(deviceAddress, true, onConnectionListener, receiveListener)
//    }

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
        private const val SAMPLE_RATE = 8000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val BUFFER_SIZE = 1024
//        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_8BIT
//        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)

    }
}