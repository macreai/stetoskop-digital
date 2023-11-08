package com.apicta.stetoskop_digital.util

import android.media.AudioFormat
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

object Wav {

//    @Throws(IOException::class)
//    fun writeWavHeader(outputStream: FileOutputStream, channelConfig: Int, sampleRate: Int, audioFormat: Int) {
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
//        header[22] = (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2).toByte()
//        header[23] = 0
//
//        // Sample rate
//        header[24] = (sampleRate and 0xff).toByte()
//        header[25] = (sampleRate shr 8 and 0xff).toByte()
//        header[26] = (sampleRate shr 16 and 0xff).toByte()
//        header[27] = (sampleRate shr 24 and 0xff).toByte()
//
//        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
//        val byteRate = sampleRate * (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1
//        header[28] = (byteRate and 0xff).toByte()
//        header[29] = (byteRate shr 8 and 0xff).toByte()
//        header[30] = (byteRate shr 16 and 0xff).toByte()
//        header[31] = (byteRate shr 24 and 0xff).toByte()
//
//        // Block align (Number of channels * Bits per sample / 8)
//        header[32] = ((if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1).toByte()
//        header[33] = 0
//
//        // Bits per sample
//        header[34] = (if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 16 else 8).toByte()
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

//    public fun writeWavHeader(outputStream: OutputStream, totalAudioLen: Int, totalDataLen: Int, longSampleRate: Long, channels: Int, byteRate: Long) {
//        val header = ByteArray(44)
//
//        header[0] = 'R'.toByte() // RIFF/WAVE header
//        header[1] = 'I'.toByte()
//        header[2] = 'F'.toByte()
//        header[3] = 'F'.toByte()
//
//        header[4] = (totalDataLen and 0xFF).toByte() // file size (totalDataLen + 36)
//        header[5] = (totalDataLen shr 8 and 0xFF).toByte()
//        header[6] = (totalDataLen shr 16 and 0xFF).toByte()
//        header[7] = (totalDataLen shr 24 and 0xFF).toByte()
//
//        header[8] = 'W'.toByte() // WAVE format
//        header[9] = 'A'.toByte()
//        header[10] = 'V'.toByte()
//        header[11] = 'E'.toByte()
//
//        header[12] = 'f'.toByte() // 'fmt ' chunk
//        header[13] = 'm'.toByte()
//        header[14] = 't'.toByte()
//        header[15] = ' '.toByte()
//
//        header[16] = 16 // 16 for PCM
//        header[17] = 0
//        header[18] = 0
//        header[19] = 0
//
//        header[20] = 1 // 1 for PCM
//        header[21] = 0
//
//        header[22] = channels.toByte() // number of channels
//        header[23] = 0
//
//        header[24] = (longSampleRate and 0xFF).toByte() // sample rate
//        header[25] = (longSampleRate shr 8 and 0xFF).toByte()
//        header[26] = (longSampleRate shr 16 and 0xFF).toByte()
//        header[27] = (longSampleRate shr 24 and 0xFF).toByte()
//
//        header[28] = (byteRate and 0xFF).toByte() // byte rate
//        header[29] = (byteRate shr 8 and 0xFF).toByte()
//        header[30] = (byteRate shr 16 and 0xFF).toByte()
//        header[31] = (byteRate shr 24 and 0xFF).toByte()
//
//        header[32] = (channels * 8).toByte() // block align
//        header[33] = 0
//
//        header[34] = 8 // bits per sample
//        header[35] = 0
//
//        header[36] = 'd'.toByte() // 'data' chunk
//        header[37] = 'a'.toByte()
//        header[38] = 't'.toByte()
//        header[39] = 'a'.toByte()
//
//        header[40] = (totalAudioLen and 0xFF).toByte() // data size (totalDataLen)
//        header[41] = (totalAudioLen shr 8 and 0xFF).toByte()
//        header[42] = (totalAudioLen shr 16 and 0xFF).toByte()
//        header[43] = (totalAudioLen shr 24 and 0xFF).toByte()
//
//        outputStream.write(header, 0, 44)
//    }

//    @Throws(IOException::class)
//    fun writeWavHeader(outputStream: OutputStream, channelConfig: Int, sampleRate: Int, audioFormat: Int) {
//        val audioDataLength: Long = 0 // Set to 0 for now
//        val overallSize: Long = audioDataLength + 36 // Add header size
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
//        header[22] = (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2).toByte()
//        header[23] = 0
//
//        // Sample rate
//        header[24] = (sampleRate and 0xff).toByte()
//        header[25] = (sampleRate shr 8 and 0xff).toByte()
//        header[26] = (sampleRate shr 16 and 0xff).toByte()
//        header[27] = (sampleRate shr 24 and 0xff).toByte()
//
//        // Byte rate (Sample rate * Number of channels * Bits per sample / 8)
//        val byteRate = sampleRate * (if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1
//        header[28] = (byteRate and 0xff).toByte()
//        header[29] = (byteRate shr 8 and 0xff).toByte()
//        header[30] = (byteRate shr 16 and 0xff).toByte()
//        header[31] = (byteRate shr 24 and 0xff).toByte()
//
//        // Block align (Number of channels * Bits per sample / 8)
//        header[32] = ((if (channelConfig == AudioFormat.CHANNEL_IN_MONO) 1 else 2) * if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 2 else 1).toByte()
//        header[33] = 0
//
//        // Bits per sample
//        header[34] = (if (audioFormat == AudioFormat.ENCODING_PCM_16BIT) 16 else 8).toByte()
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
//
//        outputStream.write(header)
//    }

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

}