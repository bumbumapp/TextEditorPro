package net.gsantner.opoc.ui;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/** Voice notes: om-recorder (jcenter only) was replaced by WavRecorder; the WAV header must stay valid. */
public class WavRecorderTest {

    @Test
    public void headerDescribesStereoPcm16() {
        byte[] h = WavRecorder.header(1000);
        ByteBuffer b = ByteBuffer.wrap(h).order(ByteOrder.LITTLE_ENDIAN);
        assertEquals(44, h.length);
        assertEquals("RIFF", new String(h, 0, 4));
        assertEquals(1036, b.getInt(4));
        assertEquals("WAVE", new String(h, 8, 4));
        assertEquals(1, b.getShort(20));
        assertEquals(2, b.getShort(22));
        assertEquals(44100, b.getInt(24));
        assertEquals(44100 * 4, b.getInt(28));
        assertEquals(16, b.getShort(34));
        assertEquals("data", new String(h, 36, 4));
        assertEquals(1000, b.getInt(40));
    }
}
