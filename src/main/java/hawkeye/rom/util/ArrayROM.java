package hawkeye.rom.util;

import hawkeye.rom.exceptions.ROMAccessException;

import java.util.Arrays;

public class ArrayROM implements ROM {
    private final int[] data;
    private final String md5sum;
    private final boolean[] hasBeenRead;

    public ArrayROM(int[] data) {
        this.data = data;
        this.hasBeenRead = new boolean[data.length];
        this.md5sum = "md5sum todo";
    }

    @Override
    public int read(long offset) throws ROMAccessException {
        int result =  data[(int) offset];
        hasBeenRead[(int) offset] = true;
        return result;
    }

    @Override
    public long readMultiLittleEndian(long offset, int length) throws ROMAccessException {
        long result = 0;
        for (int i = 0; i < length; ++i) {
            result |= ((long) data[(int) (i+offset)]) << (8 * i);
        }

        for (int i = 0; i < length; ++i) {
            hasBeenRead[(int) (i+offset)] = true;
        }

        return result;
    }

    @Override
    public int[] readArray(long offset, int length) throws ROMAccessException {
        int[] result =  Arrays.copyOfRange(data, (int) offset, (int) (offset+length));

        for (int i = 0; i < length; ++i) {
            hasBeenRead[(int) (i+offset)] = true;
        }

        return result;
    }

    @Override
    public String getMd5sum() {
        return md5sum;
    }

    @Override
    public long size() {
        return this.data.length;
    }

    @Override
    public boolean hasBeenRead(long offset) {
        return hasBeenRead[(int) offset];
    }
}
