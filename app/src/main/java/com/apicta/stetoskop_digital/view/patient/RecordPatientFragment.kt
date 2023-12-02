package com.apicta.stetoskop_digital.view.patient

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.filters.LowPassFS
import be.tarsos.dsp.io.TarsosDSPAudioInputStream
import com.apicta.stetoskop_digital.audio.MFCC
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentRecordPatientBinding
import com.apicta.stetoskop_digital.ml.Model
import com.apicta.stetoskop_digital.util.ChartData
import com.apicta.stetoskop_digital.util.BluetoothSocketHolder
import com.apicta.stetoskop_digital.util.Wav
import com.apicta.stetoskop_digital.viewmodel.AudioViewModel
import com.apicta.stetoskop_digital.viewmodel.AuthViewModel
import com.apicta.stetoskop_digital.viewmodel.BluetoothViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class RecordPatientFragment : Fragment() {

    private var _binding: FragmentRecordPatientBinding? = null
    private val binding get() = _binding!!

    private var myThreadConnected: ThreadConnected? = null

    private var postJob: Job = Job()

    private val bluetoothViewModel: BluetoothViewModel by activityViewModels()
    private val audioViewModel: AudioViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }
    private val authViewModel: AuthViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }


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

        @RequiresApi(Build.VERSION_CODES.O)
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
                        Log.d(TAG, "pcgArray: ${ChartData.pcgArray}")
                        Log.d(TAG, "timeArray: ${ChartData.timeArray}")

                        isOn = false

                        data = byteArrayOutputStream.toByteArray()
//                        val filteredData = Wav.applyLowPassFilter(data, CUTOFF_FREQUENCY, SAMPLE_RATE)
//                        val filteredData = Wav.applyButterworthLowPassFilter(data, CUTOFF_FREQUENCY, SAMPLE_RATE.toDouble())
                        val wavData: ByteArray = Wav.generateWavHeader(data, CHANNEL_CONFIG, SAMPLE_RATE, AUDIO_FORMAT)

                        val filePath: String? = Wav.saveWavFile(requireContext(), binding.fileName.text.toString(), SAMPLE_RATE, wavData)
                        if (filePath != null){
                            authViewModel.getId().observe(viewLifecycleOwner, Observer {  id ->
                                sendWavFile(filePath, id.toString())
                            })
                        }
//                        val inputData = mfccProcess(data)

//                        val byteBufferInput = prepareInputData(inputData)

//                        runModel(byteBufferInput)

                    }

                    val receivedInt = ByteBuffer.wrap(mmBuffer, 0, numBytes).order(ByteOrder.LITTLE_ENDIAN).int
                    Log.d(TAG, "received: ${receivedInt.toFloat()}")
                    ChartData.pcgArray.add(receivedInt.toFloat())
                    ChartData.timeArray.add(elapsedTimeFloat)
                    ChartData.setCurrentTime(formatDate(LocalDateTime.now()))

                    updateChart()
                }
            }
        }

        fun cancel(){
            try {
                mmSocket.close()
                bluetoothViewModel.destroyBluetoothName()
                Toast.makeText(requireContext(), "Disconnected", Toast.LENGTH_SHORT).show()
            } catch (e: IOException){
                Log.e(TAG, "Could not close the connect socket", e)
            }
        }

    }

    private fun sendWavFile(filePath: String, id: String){
        val file = File(filePath)
        val requestWavFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val wavFilePart = MultipartBody.Part.createFormData("file", file.name, requestWavFile)
        val idRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), id)
        lifecycleScope.launchWhenResumed {
            if (postJob.isActive) postJob.cancel()
            postJob = launch {
                audioViewModel.postWav(idRequestBody, wavFilePart).collect{ result ->
                    result.onSuccess {  response ->
                        binding.loading.visibility = View.GONE
                        Log.d(TAG, "jenis: ${response.data?.jenis}")
                        Log.d(TAG, "result: ${response.data?.result}")
                        binding.tvStatus.text = "Result"
                        if (response.data?.result == "0"){
                            binding.statusResult.text = "Aortic Stenosis (AS)"
                        } else if (response.data?.result == "1"){
                            binding.statusResult.text = "Mitral Regurgitation (MR)"
                        } else if (response.data?.result == "2"){
                            binding.statusResult.text = "Mitral Valve Prolapse (MVP)"
                        } else if (response.data?.result == "3"){
                            binding.statusResult.text = "Mitral Stenosis (MS)"
                        } else if (response.data?.result == "4"){
                            binding.statusResult.text = "Normal (N)"
                        } else {
                            binding.statusResult.text = "Unknown"
                        }
                    }
                    result.onFailure {  throwable ->
                        binding.loading.visibility = View.GONE
                        Log.d(TAG, "throwable: $throwable")
                        binding.statusResult.text = "Error -- Try Again"
                    }
                }
            }
        }
    }

    private fun filterWav(wavFile: File){
    }

    private fun mfccProcess(data: ByteArray): FloatArray {
        val mfcc = MFCC()
        mfcc.setSampleRate(SAMPLE_RATE)
        mfcc.setN_mfcc(40)
        return mfcc.process(data)
    }

    private fun prepareInputData(data: FloatArray): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(data.size * 4) // 4 bytes per float
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.rewind() // Reset position to the beginning
        for (value in data) {
            byteBuffer.putFloat(value)
        }
        byteBuffer.rewind()
        return byteBuffer
    }

    private fun runModel(byteBuffer: ByteBuffer){
        val model = Model.newInstance(requireContext())

// Creates inputs for reference.
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 40, 1, 1), DataType.FLOAT32)
        inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        handleOutput(outputFeature0)

// Releases model resources if no longer used.
        model.close()

    }

    private fun handleOutput(outputTensorBuffer: TensorBuffer) {
        // Example: Print the output values to the console.
        val outputValues = outputTensorBuffer.floatArray
        for (value in outputValues) {
            Log.d(TAG, "handleOutput: $value")
        }

        // TODO: Implement further processing based on your application's requirements.
    }

//    private fun runInference(byteBuffer: ByteBuffer, context: Context): FloatArray {
//        // Load the TFLite model
//        val model = Model.newInstance(context)
//
//        // Create input tensor
//        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 40, 1, 1), DataType.FLOAT32)
//        inputFeature0.loadBuffer(byteBuffer)
//
//        // Run model inference and get results
//        val outputs = model.process(inputFeature0)
//        val outputFeature0 = outputs.outputFeature0AsTensorBuffer
//
//        // Retrieve and return the output results directly
//        return FloatArray(outputFeature0.shape()[1].toInt()).apply {
//            outputFeature0.floatArray.get(this)
//        }.also {
//            // Release model resources
//            model.close()
//        }
//    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRecordPatientBinding.inflate(inflater, container, false)
        val view = binding.root


        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val socket = BluetoothSocketHolder.getBluetoothSocket()
        Log.d(TAG, "onViewCreated: $socket")

        initViewChart()
        bluetoothService()

        bluetoothViewModel.bluetoothName.observe(viewLifecycleOwner, Observer {
            binding.deviceName.text = it ?: "No Device"
            Log.d(TAG, "btName: $it")
        })

        binding.btnRecord.setOnClickListener {
            if (binding.fileName.text?.isEmpty() == true){
                binding.fileName.requestFocus()
                binding.fileName.error = "please insert file name first"
            } else {
                if (socket != null){
                    ChartData.apply {
                        pcgArray.clear()
                        timeArray.clear()
                    }
                    myThreadConnected = ThreadConnected(socket, true)
                    myThreadConnected!!.start()
                    startCountdown()
                } else {
                    Toast.makeText(requireContext(), "Connect to  your device first!", Toast.LENGTH_LONG).show()
                }
            }
        }

        binding.deviceNameFrame.setOnClickListener {
            myThreadConnected?.cancel()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun bluetoothService(){
        val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (checkBtPermission()){
            if (bluetoothAdapter.isEnabled){
                val connectedDevices = bluetoothAdapter.getProfileProxy(requireContext(), object : BluetoothProfile.ServiceListener{
                    override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
                        bluetoothAdapter.closeProfileProxy(profile, proxy)
                    }

                    override fun onServiceDisconnected(profile: Int) {
                        binding.deviceName.text = "Disconnected"
                    }
                }, BluetoothProfile.HEADSET)

                if (!connectedDevices){
                    binding.deviceName.text = "No Device Connected"
                }
            } else {
                binding.deviceName.text = "Bluetooth is not enabled"
            }
        }
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

    private fun initViewChart(){
        binding.signalView.description.text = "Raw PCG"
        binding.signalView.setNoDataText("Record Not Started")
    }

    private fun updateChart() {
        val dataValues: ArrayList<Entry> = ArrayList()
        for ((time, audio) in ChartData.timeArray.zip(ChartData.pcgArray)) {
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

    private fun startCountdown() {
        val countDownTimer = object : CountDownTimer(10_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                val countdownMessage =
                    "Please wait $secondsRemaining seconds to give you the best accurate result"
                binding.statusResult.text = countdownMessage
            }

            override fun onFinish() {
                binding.statusResult.text = "Please wait, we're analyzing your record.."
                binding.loading.visibility = View.VISIBLE
                binding.cardView.visibility = View.VISIBLE
            }
        }
        countDownTimer.start()
        binding.cardView.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
//        ArrayReceiver.pcgArray.clear()
//        ArrayReceiver.timeArray.clear()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    companion object{
        private const val TAG = "Record Patient Fragment"
        private const val REQUEST_PERMISSION = 1
//        private const val CUTOFF_FREQUENCY = 1000
        private const val SAMPLE_RATE = 24_000
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_FLOAT
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT)
    }
}