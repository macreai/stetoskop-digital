package com.apicta.stetoskop_digital.audio;

import android.content.ContentValues;
import android.content.Context;
import android.media.AudioFormat;
import android.provider.MediaStore;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class WavFileConverter {
    private static final String TAG = "WavFileConverter";

    public static Uri byteArrayToWavFile(String fileName, Context context, byte[] byteArray, int sampleRate, int channels, int bitsPerSample) {
        try {
            File wavFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), fileName);
            FileOutputStream fos = new FileOutputStream(wavFile);

            long totalAudioLen = byteArray.length;
            long totalDataLen = totalAudioLen + 36;
            long longSampleRate = sampleRate;
            int channelsCount = (channels == AudioFormat.CHANNEL_IN_MONO) ? 1 : 2;
            long byteRate = bitsPerSample * sampleRate * channelsCount / 8;

            byte[] header = new byte[44];
            header[0] = 'R';  // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = 1;  // format = 1 (PCM)
            header[21] = 0;
            header[22] = (byte) channelsCount;
            header[23] = 0;
            header[24] = (byte) (longSampleRate & 0xff);
            header[25] = (byte) ((longSampleRate >> 8) & 0xff);
            header[26] = (byte) ((longSampleRate >> 16) & 0xff);
            header[27] = (byte) ((longSampleRate >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) (2 * 16 / 8);  // block align
            header[33] = 0;
            header[34] = (byte) bitsPerSample;  // bits per sample
            header[35] = 0;
            header[36] = 'd';  // 'data' chunk
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte) (totalAudioLen & 0xff);
            header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
            header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
            header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

            fos.write(header);
            fos.write(byteArray);
            fos.close();

            // Use MediaStore to add the WAV file to the Media Library
            ContentValues values = new ContentValues();
            values.put(MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaColumns.MIME_TYPE, "audio/wav");
            values.put(MediaColumns.SIZE, wavFile.length());
            values.put(MediaColumns.DATA, wavFile.getAbsolutePath());

            Uri audioUri = context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            if (audioUri != null) {
                Log.d(TAG, "WAV file saved to MediaStore: " + audioUri.toString());
            } else {
                Log.e(TAG, "Failed to save WAV file to MediaStore");
            }

            return audioUri;
        } catch (IOException e) {
            Log.e(TAG, "Error converting bytearray to WAV: " + e.getMessage());
            return null;
        }
    }
}

