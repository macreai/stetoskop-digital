package com.apicta.stetoskop_digital.view.patient

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.apicta.stetoskop_digital.R
import com.apicta.stetoskop_digital.dataStore
import com.apicta.stetoskop_digital.databinding.FragmentDetailFileBinding
import com.apicta.stetoskop_digital.model.remote.response.DataPredictionFileItem
import com.apicta.stetoskop_digital.util.DoctorHolder
import com.apicta.stetoskop_digital.view.patient.PredictPatientFragment.Companion.EXTRA_ID
import com.apicta.stetoskop_digital.viewmodel.RecordViewModel
import com.apicta.stetoskop_digital.viewmodel.ViewModelFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.util.MimeTypes


class DetailFileFragment : Fragment() {

    private var simpleExoPlayer: ExoPlayer? = null

    private var _binding: FragmentDetailFileBinding? = null
    private val binding get()= _binding!!

    private val recordViewModel: RecordViewModel by viewModels { ViewModelFactory(requireContext().dataStore) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDetailFileBinding.inflate(inflater, container, false)
        val view = binding.root

        val fileId = arguments?.getInt(EXTRA_ID)
        getDetailFile(fileId!!)

        return view
    }

    private fun getDetailFile(id: Int){
        binding.loading.visibility = View.VISIBLE
        lifecycleScope.launchWhenResumed {
            recordViewModel.getRecordById(id).collect{ result ->
                result.onSuccess {  response ->
                    if (response.status == "success"){
                        binding.loading.visibility = View.GONE
                        initPlayer(URL + response.data!![0].filePath)
                        Log.d(TAG, "getDetailFile: ${URL + response.data!![0].filePath}")
                        initUi(response.data[0])
                    } else {
                        binding.loading.visibility = View.GONE
                        Toast.makeText(requireContext(), "Unable to load file", Toast.LENGTH_SHORT).show()
                    }
                }
                result.onFailure {  throwable ->
                    binding.loading.visibility = View.GONE
                    Toast.makeText(requireContext(), "error: $throwable", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initUi(detail: DataPredictionFileItem) {
        binding.fileName.text = detail.suara ?: "Unable to load"

        if (detail.result == "0"){
            binding.diagnoseResult.text = "Aortic Stenosis (AS)"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.red))
        } else if (detail.result == "1"){
            binding.diagnoseResult.text = "Mitral Regurgitation (MR)"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.red))
        } else if (detail.result == "2"){
            binding.diagnoseResult.text = "Mitral Valve Prolapse (MVP)"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.red))
        } else if (detail.result == "3"){
            binding.diagnoseResult.text = "Mitral Stenosis (MS)"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.red))
        } else if (detail.result == "4"){
            binding.diagnoseResult.text = "Normal (N)"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.green))
        } else {
            binding.diagnoseResult.text = "Unknown"
            binding.cardDiagnose.setCardBackgroundColor(resources.getColor(R.color.gray))
        }

        if (detail.status == "0"){
            binding.verifiedResult.text = "Unverified"
            binding.cardVerified.setBackgroundColor(resources.getColor(R.color.red))
        } else {
            binding.verifiedResult.text = "Verified"
            binding.cardVerified.setCardBackgroundColor(resources.getColor(R.color.green))
        }

        binding.noteResult.text = detail.note

        binding.updatedAtResult.text = detail.updatedAt

        binding.doctorName.text = DoctorHolder.getDoctor()
    }

    private fun initPlayer(url: String?){
        simpleExoPlayer = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = simpleExoPlayer
        val mediaItem = MediaItem.Builder()
            .setUri(url)
            .setMimeType(MimeTypes.AUDIO_WAV)
            .build()

        ProgressiveMediaSource.Factory(
            DefaultDataSource.Factory(requireContext())
        ).createMediaSource(mediaItem)

        simpleExoPlayer!!.setMediaItem(mediaItem)
        simpleExoPlayer!!.prepare()
        simpleExoPlayer!!.play()


        binding.audioVisualizer.visibility = View.VISIBLE
        binding.audioVisualizer.setColor(ContextCompat.getColor(requireContext(), R.color.orange))
        binding.audioVisualizer.setPlayer(simpleExoPlayer!!.audioSessionId)

    }

    override fun onDestroy() {
        super.onDestroy()
        if (simpleExoPlayer != null){
            simpleExoPlayer!!.release()
            simpleExoPlayer = null
        }
    }

    companion object{
        private const val TAG = "DetailFragment"
        private const val URL = " https://vhd.telekardiologi.com/"
    }

}