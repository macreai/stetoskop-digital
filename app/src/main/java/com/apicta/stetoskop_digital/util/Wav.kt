package com.apicta.stetoskop_digital.util

import java.io.OutputStream

object Wav {
    public fun writeWavHeader(outputStream: OutputStream, totalAudioLen: Int, totalDataLen: Int, longSampleRate: Long, channels: Int, byteRate: Long) {
        val header = ByteArray(44)

        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()

        header[4] = (totalDataLen and 0xFF).toByte() // file size (totalDataLen + 36)
        header[5] = (totalDataLen shr 8 and 0xFF).toByte()
        header[6] = (totalDataLen shr 16 and 0xFF).toByte()
        header[7] = (totalDataLen shr 24 and 0xFF).toByte()

        header[8] = 'W'.toByte() // WAVE format
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()

        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()

        header[16] = 16 // 16 for PCM
        header[17] = 0
        header[18] = 0
        header[19] = 0

        header[20] = 1 // 1 for PCM
        header[21] = 0

        header[22] = channels.toByte() // number of channels
        header[23] = 0

        header[24] = (longSampleRate and 0xFF).toByte() // sample rate
        header[25] = (longSampleRate shr 8 and 0xFF).toByte()
        header[26] = (longSampleRate shr 16 and 0xFF).toByte()
        header[27] = (longSampleRate shr 24 and 0xFF).toByte()

        header[28] = (byteRate and 0xFF).toByte() // byte rate
        header[29] = (byteRate shr 8 and 0xFF).toByte()
        header[30] = (byteRate shr 16 and 0xFF).toByte()
        header[31] = (byteRate shr 24 and 0xFF).toByte()

        header[32] = (channels * 8).toByte() // block align
        header[33] = 0

        header[34] = 8 // bits per sample
        header[35] = 0

        header[36] = 'd'.toByte() // 'data' chunk
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()

        header[40] = (totalAudioLen and 0xFF).toByte() // data size (totalDataLen)
        header[41] = (totalAudioLen shr 8 and 0xFF).toByte()
        header[42] = (totalAudioLen shr 16 and 0xFF).toByte()
        header[43] = (totalAudioLen shr 24 and 0xFF).toByte()

        outputStream.write(header, 0, 44)
    }

}