package com.apicta.stetoskop_digital.audio;

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

//    public static void writeWavHeader(OutputStream os, int dataSize, int sampleRate, short bitDepth) throws IOException {
//        int totalDataLen = 36 + dataSize;
//        int totalAudioLen = dataSize - 8;
//        long byteRate = sampleRate * bitDepth / 8;
//
//        os.write(new byte[] { 'R', 'I', 'F', 'F' });
//        os.write(intToBytes(totalDataLen));
//        os.write(new byte[] { 'W', 'A', 'V', 'E' });
//        os.write(new byte[] { 'f', 'm', 't', ' ' });
//        os.write(intToBytes(16)); // Subchunk1Size
//        os.write(shortToBytes((short) 1)); // AudioFormat (1 for PCM)
//        os.write(shortToBytes((short) 1)); // NumChannels
//        os.write(intToBytes(sampleRate));
//        os.write(intToBytes((int) byteRate));
//        os.write(shortToBytes((short) (bitDepth / 8))); // BlockAlign
//        os.write(shortToBytes(bitDepth)); // BitsPerSample
//        os.write(new byte[] { 'd', 'a', 't', 'a' });
//        os.write(intToBytes(totalAudioLen));
//        int totalDataLen = 36 + dataSize;
//        int totalAudioLen = dataSize - 8;
//        long byteRate = sampleRate * bitDepth / 8;
//        int audioDuration = (int) (totalAudioLen / byteRate); // Calculate duration in seconds
//
//        os.write(new byte[] { 'R', 'I', 'F', 'F' });
//        os.write(intToBytes(totalDataLen));
//        os.write(new byte[] { 'W', 'A', 'V', 'E' });
//        os.write(new byte[] { 'f', 'm', 't', ' ' });
//        os.write(intToBytes(16)); // Subchunk1Size
//        os.write(shortToBytes((short) 1)); // AudioFormat (1 for PCM)
//        os.write(shortToBytes((short) 1)); // NumChannels
//        os.write(intToBytes(sampleRate));
//        os.write(intToBytes((int) byteRate));
//        os.write(shortToBytes((short) (bitDepth / 8))); // BlockAlign
//        os.write(shortToBytes(bitDepth)); // BitsPerSample
//        os.write(new byte[] { 'd', 'a', 't', 'a' });
//        os.write(intToBytes(totalAudioLen));
//        os.write(intToBytes(audioDuration)); // Set the audio duration
//    }

//    public static void writeWavHeader(OutputStream os, int dataSize, int sampleRate, short bitDepth, int audioDuration) throws IOException {
//        int totalDataLen = 36 + dataSize;
//        int totalAudioLen = dataSize - 8;
//        long byteRate = sampleRate * bitDepth / 8;
//
//        os.write(new byte[] { 'R', 'I', 'F', 'F' });
//        os.write(intToBytes(totalDataLen));
//        os.write(new byte[] { 'W', 'A', 'V', 'E' });
//        os.write(new byte[] { 'f', 'm', 't', ' ' });
//        os.write(intToBytes(16)); // Subchunk1Size
//        os.write(shortToBytes((short) 1)); // AudioFormat (1 for PCM)
//        os.write(shortToBytes((short) 1)); // NumChannels
//        os.write(intToBytes(sampleRate));
//        os.write(intToBytes((int) byteRate));
//        os.write(shortToBytes((short) (bitDepth / 8))); // BlockAlign
//        os.write(shortToBytes(bitDepth)); // BitsPerSample
//        os.write(new byte[] { 'd', 'a', 't', 'a' });
//        os.write(intToBytes(totalAudioLen));
//        os.write(intToBytes(audioDuration)); // Set the audio duration
//    }
//
//    public static int calculateAudioDuration(int dataSize, int sampleRate, short bitDepth) {
//        long byteRate = sampleRate * bitDepth / 8;
//        return (int) (dataSize / byteRate);
//    }
//
//
//    private static byte[] intToBytes(int value) {
//        ByteBuffer buffer = ByteBuffer.allocate(4);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        buffer.putInt(value);
//        return buffer.array();
//    }
//
//    public static byte[] shortToBytes(short value) {
//        ByteBuffer buffer = ByteBuffer.allocate(2);
//        buffer.order(ByteOrder.LITTLE_ENDIAN);
//        buffer.putShort(value);
//        return buffer.array();
//    }

//    public static void createWavHeader(OutputStream os, int sampleRate, int bitsPerSample, int numberOfChannels, int duration) throws IOException {
//        // Calculate the file size.
//        int fileSize = getFileSize(sampleRate, bitsPerSample, numberOfChannels, duration);
//
//        // Write the RIFF chunk.
//        os.write("RIFF".getBytes());
//        os.write(intToBytes(fileSize));
//        os.write("WAVE".getBytes());
//
//        // Write the fmt chunk.
//        os.write("fmt ".getBytes());
//        os.write(intToBytes(16)); // Length of format data.
//        os.write(shortToBytes((short) WAVE_FORMAT_PCM)); // Wave format PCM.
//        os.write(shortToBytes((short) numberOfChannels));
//        os.write(intToBytes(sampleRate));
//        os.write(intToBytes(sampleRate * bitsPerSample * numberOfChannels / 8)); // Byte rate.
//        os.write(shortToBytes((short) (bitsPerSample * numberOfChannels / 8)));
//        os.write(shortToBytes((short) bitsPerSample));
//
//        // Write the data chunk.
//        os.write("data".getBytes());
//        os.write(intToBytes(getFileSize(sampleRate, bitsPerSample, numberOfChannels, duration) - 44));
//    }
//
//    private static int getFileSize(int sampleRate, int bitsPerSample, int numberOfChannels, int duration) {
//        return sampleRate * bitsPerSample * numberOfChannels * duration / 8 + 44;
//    }
//
//    private static byte[] intToBytes(int value) {
//        return ByteBuffer.allocate(4).putInt(value).array();
//    }
//
//    private static byte[] shortToBytes(short value) {
//        return ByteBuffer.allocate(2).putShort(value).array();
//    }
//
//    private static final int WAVE_FORMAT_PCM = 1;
}

