package com.apicta.stetoskop_digital.audio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

public class FloatArrayListToWav {
//    public static void main(String[] args) {
//        ArrayList<Float> floatList = new ArrayList<>();
//        // Isi array list dengan nilai-nilai float yang ingin Anda konversi
//
//        String outputPath = "/sdcard/output.wav"; // Ganti dengan path penyimpanan eksternal Anda
//        int sampleRate = 44100; // Resolusi sampel
//        short bitDepth = 16; // Kedalaman bit
//
//        try {
//            FileOutputStream fos = new FileOutputStream(outputPath);
//
//            // Tulis header WAV
//            writeWavHeader(fos, floatList.size() * (bitDepth / 8), sampleRate, bitDepth);
//
//            // Konversi dan tulis data float menjadi data byte
//            for (Float value : floatList) {
//                short shortValue = (short) (value * 32767.0f); // Konversi nilai float ke short
//                byte[] bytes = shortToBytes(shortValue);
//                fos.write(bytes);
//            }
//
//            fos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

//    public static void writeWavHeader(FileOutputStream fos, int dataSize, int sampleRate, short bitDepth) throws IOException {
//        int totalDataLen = 36 + dataSize;
//        int totalAudioLen = dataSize - 8;
//        long byteRate = sampleRate * bitDepth / 8;
//
//        fos.write(new byte[] { 'R', 'I', 'F', 'F' });
//        fos.write(intToBytes(totalDataLen));
//        fos.write(new byte[] { 'W', 'A', 'V', 'E' });
//        fos.write(new byte[] { 'f', 'm', 't', ' ' });
//        fos.write(intToBytes(16)); // Subchunk1Size
//        fos.write(shortToBytes((short) 1)); // AudioFormat (1 for PCM)
//        fos.write(shortToBytes((short) 1)); // NumChannels
//        fos.write(intToBytes(sampleRate));
//        fos.write(intToBytes((int) byteRate));
//        fos.write(shortToBytes((short) (bitDepth / 8))); // BlockAlign
//        fos.write(shortToBytes(bitDepth)); // BitsPerSample
//        fos.write(new byte[] { 'd', 'a', 't', 'a' });
//        fos.write(intToBytes(totalAudioLen));
//    }

    public static void writeWavHeader(OutputStream os, int dataSize, int sampleRate, short bitDepth) throws IOException {
        int totalDataLen = 36 + dataSize;
        int totalAudioLen = dataSize - 8;
        long byteRate = sampleRate * bitDepth / 8;

        os.write(new byte[] { 'R', 'I', 'F', 'F' });
        os.write(intToBytes(totalDataLen));
        os.write(new byte[] { 'W', 'A', 'V', 'E' });
        os.write(new byte[] { 'f', 'm', 't', ' ' });
        os.write(intToBytes(16)); // Subchunk1Size
        os.write(shortToBytes((short) 1)); // AudioFormat (1 for PCM)
        os.write(shortToBytes((short) 1)); // NumChannels
        os.write(intToBytes(sampleRate));
        os.write(intToBytes((int) byteRate));
        os.write(shortToBytes((short) (bitDepth / 8))); // BlockAlign
        os.write(shortToBytes(bitDepth)); // BitsPerSample
        os.write(new byte[] { 'd', 'a', 't', 'a' });
        os.write(intToBytes(totalAudioLen));
    }


    private static byte[] intToBytes(int value) {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        return buffer.array();
    }

    public static byte[] shortToBytes(short value) {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putShort(value);
        return buffer.array();
    }
}

