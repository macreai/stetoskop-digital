package com.apicta.stetoskop_digital.util

import android.content.ContentValues
import android.content.Context
import android.media.AudioFormat
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import java.io.IOException
import java.io.OutputStream
import kotlin.experimental.and
import kotlin.math.PI

object Wav {

    @Throws(IOException::class)
    fun generateWavHeader(audioData: ByteArray, channelConfig: Int, sampleRate: Int, audioFormat: Int): ByteArray {
        val audioDataLength = audioData.size.toLong()
        val overallSize = audioDataLength + 36
        val header = ByteArray(44)

        // RIFF chunk descriptor
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()

        // Overall size (file size - 8 bytes for RIFF and WAVE tags)
        header[4] = (overallSize and 0xffL).toByte()
        header[5] = (overallSize shr 8 and 0xffL).toByte()
        header[6] = (overallSize shr 16 and 0xffL).toByte()
        header[7] = (overallSize shr 24 and 0xffL).toByte()

        // WAVE chunk
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()

        // fmt sub-chunk
        header[12] = 'f'.code.toByte() // Sub-chunk identifier
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte() // Chunk size
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // Audio format (PCM = 1)
        header[20] = 1
        header[21] = 0

        // Number of channels (2 = stereo)
        header[22] = (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2).toByte()
        header[23] = 0

        // Sample rate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = (sampleRate shr 8 and 0xff).toByte()
        header[26] = (sampleRate shr 16 and 0xff).toByte()
        header[27] = (sampleRate shr 24 and 0xff).toByte()

        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
        val byteRate = sampleRate * (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()

        // Block align (Number of channels * Bits per sample / 8)
        header[32] = ((if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1).toByte()
        header[33] = 0

        // Bits per sample
        header[34] = (if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 16 else 8).toByte()
        header[35] = 0

        // data sub-chunk
        header[36] = 'd'.code.toByte() // Sub-chunk identifier
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte() // Chunk size
        header[40] = (audioDataLength and 0xffL).toByte()
        header[41] = (audioDataLength shr 8 and 0xffL).toByte()
        header[42] = (audioDataLength shr 16 and 0xffL).toByte()
        header[43] = (audioDataLength shr 24 and 0xffL).toByte()

        val wavHeader = ByteArray(44 + audioData.size)
        System.arraycopy(header, 0, wavHeader, 0, 44)
        System.arraycopy(audioData, 0, wavHeader, 44, audioData.size)

        return wavHeader
    }

    fun saveWavFile(context: Context, filename: String, sampleRate: Int, wavData: ByteArray): String? {
        val contentValues = ContentValues()
        val fileNameWithTime = "${System.currentTimeMillis()}_$filename.wav"
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileNameWithTime)
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/wav")

        val contentResolver = context?.contentResolver
        val uri = contentResolver?.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

        val desiredByteCount = ChartData.timeArray.last().toInt() * sampleRate
        Log.d(TAG, "desiredByCount: $desiredByteCount")
        Log.d(TAG, "lastTime: ${ChartData.timeArray.last().toInt()}")

        try {
            val outputStream: OutputStream = contentResolver?.openOutputStream(uri!!)!!
            if (desiredByteCount <= wavData.size){
                outputStream.write(wavData, 0, desiredByteCount)
                Log.d(TAG, "run: execute A")
            } else {
                outputStream.write(wavData)
                Log.d(TAG, "run: execute B")
            }
            outputStream.close()

            val filePath = "${Environment.getExternalStorageDirectory()}/Music/$fileNameWithTime"
            Log.d(TAG, "File saved to: $filePath")
            Toast.makeText(context, "File saved to: $filePath", Toast.LENGTH_SHORT).show()
            return filePath
        } catch (e: IOException){
            e.printStackTrace()
        }
        return null
    }



    fun applyLowPassFilter(wavHeader: ByteArray, cutoffFrequency: Double): ByteArray {
        // Define filter parameters
        val sampleRate = 44100.0 // Replace with your actual sample rate
        val numChannels = 2 // Replace with your actual number of channels
        val numSamples = wavHeader.size / (2 * numChannels) // Assuming 16-bit PCM, adjust if needed

        // Calculate filter coefficients
        val omega = 2.0 * PI * cutoffFrequency / sampleRate
        val alpha = Math.sin(omega) / (2.0 * sampleRate)
        val cosOmega = Math.cos(omega)
        val b0 = (1.0 - cosOmega) / 2.0
        val b1 = 1.0 - cosOmega
        val b2 = (1.0 - cosOmega) / 2.0
        val a0 = 1.0 + alpha
        val a1 = -2.0 * cosOmega
        val a2 = 1.0 - alpha

        // Initialize buffers
        var x1 = 0.0
        var x2 = 0.0
        var y1 = 0.0
        var y2 = 0.0

        val result = ByteArray(wavHeader.size)

        // Apply filter to each channel
        for (channel in 0 until numChannels) {
            for (i in 0 until numSamples) {
                val index = 2 * numChannels * i + 2 * channel

                // Extract 16-bit PCM sample (little-endian)
                val sample = (wavHeader[index + 1].toInt() and 0xFF shl 8) or (wavHeader[index].toInt() and 0xFF)

                // Normalize to floating point
                val x0 = sample.toDouble() / Short.MAX_VALUE

                // Apply filter
                val y0 = (b0 / a0) * x0 + (b1 / a0) * x1 + (b2 / a0) * x2 - (a1 / a0) * y1 - (a2 / a0) * y2

                // Denormalize to 16-bit PCM
                val resultSample = (y0 * Short.MAX_VALUE).toInt().toShort()

                // Update buffers
                x2 = x1
                x1 = x0
                y2 = y1
                y1 = y0

                // Store result (little-endian)
                result[index] = (resultSample and 0xFF).toByte()
                result[index + 1] = ((resultSample.toInt() ushr 8) and 0xFF).toByte()
            }
        }

        return result
    }

    fun applyLowPassFilter(data: ByteArray, cutoffFrequency: Double, sampleRate: Int): ByteArray {
        val samplingRate = sampleRate.toDouble()
        val dt = 1.0 / samplingRate
        val rc = 1.0 / (2 * Math.PI * cutoffFrequency)
        val alpha = dt / (dt + rc)

        // Apply a simple moving average low-pass filter
        var filteredValue = 0.0
        val filteredData = ByteArray(data.size)

        for (i in data.indices) {
            val inputValue = data[i].toDouble() / 32768.0  // Assuming 16-bit PCM data

            filteredValue = filteredValue + alpha * (inputValue - filteredValue)
            filteredData[i] = (filteredValue * 32768.0).toInt().toByte()
        }

        return filteredData
    }

    fun applyButterworthLowPassFilter(data: ByteArray, cutoffFrequency: Double, samplingRate: Double): ByteArray {
        val dt = 1.0 / samplingRate
        val rc = 1.0 / (2.0 * Math.PI * cutoffFrequency)
        val alpha = dt / (dt + rc)

        // Coefficients for the Butterworth filter
        val a0 = 1 - alpha
        val a1 = 0.0
        val b0 = 1.0
        val b1 = -alpha

        // Apply the Butterworth low-pass filter
        var yPrev = 0.0
        var xPrev = 0.0
        val filteredData = ByteArray(data.size)

        for (i in data.indices) {
            val x = data[i].toDouble() / 32768.0 // Assuming 16-bit PCM data

            val y = b0 * x + b1 * xPrev - a1 * yPrev

            yPrev = y
            xPrev = x

            filteredData[i] = (y * 32768.0).toInt().toByte()
        }

        return filteredData
    }


    private const val TAG = "WAV UTIL"

}