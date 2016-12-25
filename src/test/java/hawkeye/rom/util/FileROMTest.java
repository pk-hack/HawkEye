package hawkeye.rom.util;

import hawkeye.rom.exceptions.ROMAccessException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class FileROMTest {
    private static final String TEST_ROM_FILENAME = "src/test/data/test-rom.bin";

    private ROM rom;

    @Before
    public void init() throws Exception {
        rom = new FileROM(TEST_ROM_FILENAME);
    }

    @Test
    public void testGetMd5Sum() throws Exception {
        assertEquals("3994e200dc915073cb491ef56f6700c4", rom.getMd5sum());
    }

    @Test
    public void testRead() throws Exception {
        assertEquals(0xab, rom.read(0));
        assertEquals(0xcd, rom.read(1));
        assertEquals(0xef, rom.read(2));
        assertEquals(0xc5, rom.read(0xf));
    }

    @Test(expected = ROMAccessException.class)
    public void testRead_Negative() throws Exception {
        rom.read(-1);
    }

    @Test(expected = ROMAccessException.class)
    public void testRead_TooFar() throws Exception {
        rom.read(1000);
    }

    @Test
    public void testReadMultiLittleEndian() throws Exception {
        assertEquals(0, rom.readMultiLittleEndian(0, 0));
        assertEquals(0xab, rom.readMultiLittleEndian(0, 1));
        assertEquals(0xcdab, rom.readMultiLittleEndian(0, 2));
        assertEquals(0xefcdab, rom.readMultiLittleEndian(0, 3));
        assertEquals(0x08efcd, rom.readMultiLittleEndian(1, 3));
        assertEquals(0xc5ffee, rom.readMultiLittleEndian(0xd, 3));
        assertEquals(0x08efcdabL, rom.readMultiLittleEndian(0, 4));
    }

    @Test(expected = ROMAccessException.class)
    public void testReadMultiLittleEndian_Negative() throws Exception {
        rom.readMultiLittleEndian(-1, 1);
    }

    @Test(expected = ROMAccessException.class)
    public void testReadMultiLittleEndian_TooFar() throws Exception {
        rom.readMultiLittleEndian(0xf, 5);
    }

    @Test
    public void testRead_HasBeenRead() throws Exception {
        assertFalse(rom.hasBeenRead(0L));
        rom.read(0);
        assertTrue(rom.hasBeenRead(0L));
    }

    @Test
    public void testReadMultiLittleEndian_HasBeenRead() throws Exception {
        assertFalse(rom.hasBeenRead(0L));
        assertFalse(rom.hasBeenRead(1L));
        assertFalse(rom.hasBeenRead(2L));
        assertFalse(rom.hasBeenRead(3L));
        assertFalse(rom.hasBeenRead(4L));

        rom.readMultiLittleEndian(1L, 3);

        assertFalse(rom.hasBeenRead(0L));
        assertTrue(rom.hasBeenRead(1L));
        assertTrue(rom.hasBeenRead(2L));
        assertTrue(rom.hasBeenRead(3L));
        assertFalse(rom.hasBeenRead(4L));
    }
}
