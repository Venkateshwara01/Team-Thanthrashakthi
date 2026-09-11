package tests.memory;

import memory.DataMemory;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DataMemoryTest {
    private DataMemory dataMemory;

    @Before
    public void setUp() {
        dataMemory = new DataMemory();
    }

    @Test
    public void testWriteAndReadMemory() {
        dataMemory.write(0x10, 0xAB);
        assertEquals(0xAB, dataMemory.read(0x10));
    }

    @Test
    public void testMemoryReset() {
        dataMemory.write(0x05, 0xFF);
        dataMemory.reset();
        assertEquals(0x00, dataMemory.read(0x05));
    }

    @Test
    public void testMemoryMasking() {
        // Ensures values are properly masked to 8-bit (0xFF)
        dataMemory.write(0x00, 0x1FF); 
        assertEquals(0xFF, dataMemory.read(0x00));
    }

    @Test
    public void testAddressMasking() {
        // Ensures addresses above 255 wrap within 256-byte RAM boundary
        dataMemory.write(0x100, 0x42); // Address 256 wraps to Address 0
        assertEquals(0x42, dataMemory.read(0x00));
    }
}
