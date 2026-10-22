package net.gsantner.opoc.ui;

import android.annotation.SuppressLint;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Small PCM-16 WAV recorder on top of AudioRecord. Replaces om-recorder, which was only published
 * on jcenter and can no longer be resolved.
 */
public class WavRecorder {
    static final int SAMPLE_RATE = 44100;
    static final int CHANNELS = 2;
    static final int BITS = 16;

    private final File file;
    private volatile boolean recording;
    private Thread thread;

    public WavRecorder(File file) {
        this.file = file;
    }

    @SuppressLint("MissingPermission") // caller checks RECORD_AUDIO
    public void startRecording() {
        if (recording) return;
        final int minBuf = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT);
        final int bufSize = Math.max(minBuf, 4096) * 2;
        final AudioRecord record = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufSize);
        recording = true;
        thread = new Thread(() -> {
            byte[] buffer = new byte[bufSize];
            try (RandomAccessFile out = new RandomAccessFile(file, "rw")) {
                out.setLength(0);
                out.write(header(0));
                record.startRecording();
                long dataLen = 0;
                while (recording) {
                    int read = record.read(buffer, 0, buffer.length);
                    if (read > 0) {
                        out.write(buffer, 0, read);
                        dataLen += read;
                    }
                }
                out.seek(0);
                out.write(header(dataLen));
            } catch (IOException | IllegalStateException ignored) {
            } finally {
                try {
                    record.stop();
                } catch (IllegalStateException ignored) {
                }
                record.release();
            }
        }, "WavRecorder");
        thread.start();
    }

    public void stopRecording() {
        recording = false;
        Thread t = thread;
        if (t != null) {
            try {
                t.join(2000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /** 44-byte RIFF/WAVE header for {@code dataLen} bytes of PCM data. */
    static byte[] header(long dataLen) {
        long byteRate = (long) SAMPLE_RATE * CHANNELS * BITS / 8;
        long riffLen = dataLen + 36;
        byte[] h = new byte[44];
        put(h, 0, "RIFF");
        le32(h, 4, riffLen);
        put(h, 8, "WAVE");
        put(h, 12, "fmt ");
        le32(h, 16, 16);
        le16(h, 20, 1);
        le16(h, 22, CHANNELS);
        le32(h, 24, SAMPLE_RATE);
        le32(h, 28, byteRate);
        le16(h, 32, CHANNELS * BITS / 8);
        le16(h, 34, BITS);
        put(h, 36, "data");
        le32(h, 40, dataLen);
        return h;
    }

    private static void put(byte[] b, int off, String s) {
        for (int i = 0; i < 4; i++) b[off + i] = (byte) s.charAt(i);
    }

    private static void le16(byte[] b, int off, int v) {
        b[off] = (byte) v;
        b[off + 1] = (byte) (v >> 8);
    }

    private static void le32(byte[] b, int off, long v) {
        for (int i = 0; i < 4; i++) b[off + i] = (byte) (v >> (8 * i));
    }
}
