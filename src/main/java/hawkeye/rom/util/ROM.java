package hawkeye.rom.util;

import hawkeye.rom.exceptions.ROMAccessException;

public interface ROM {
    int read(long offset) throws ROMAccessException;
    long readMultiLittleEndian(long offset, int length) throws ROMAccessException;
    int[] readArray(long offset, int length) throws ROMAccessException;
    String getMd5sum();
    long size();
    boolean hasBeenRead(long offset);
}
