package hawkeye.rom.util;

import hawkeye.rom.exceptions.ROMAccessException;
import lombok.Getter;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileROM implements ROM {
    private RandomAccessFile randomAccessFile;
    private final long size;
    private boolean[] hasBeenRead;

    @Getter
    private final String md5sum;

    public FileROM(String filename) throws IOException {
        File file = new File(filename);
        size = file.length();
        md5sum = calculateMd5sum(file);
        randomAccessFile = new RandomAccessFile(file, "r");
        hasBeenRead = new boolean[(int) size];
    }

    public FileROM(File file) throws IOException {
        size = file.length();
        md5sum = calculateMd5sum(file);
        randomAccessFile = new RandomAccessFile(file, "r");
        hasBeenRead = new boolean[(int) size];
    }

    private static String calculateMd5sum(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return DigestUtils.md5Hex(fis);
    }

    @Override
    public int read(long offset) throws ROMAccessException {
        if ((offset < 0) || (offset >= size)) {
            throw new ROMAccessException(
                    String.format("Cannot read out-of-range offset 0x%X in ROM of size 0x%X", offset, size));
        }

        try {
            randomAccessFile.seek(offset);
            hasBeenRead[(int) offset] = true;
            return randomAccessFile.read();
        } catch (IOException e) {
            hasBeenRead[(int) offset] = false;
            throw new ROMAccessException(
                    String.format("Error while reading offset 0x%X in ROM of size 0x%X", offset, size), e);
        }
    }

    @Override
    public long readMultiLittleEndian(long offset, int length) throws ROMAccessException {
        if ((offset < 0) || (offset + length - 1 >= size)) {
            throw new ROMAccessException(
                    String.format("Cannot read out-of-range offset 0x%X in ROM of size 0x%X", offset + length - 1, size));
        }

        long result = 0;
        try {
            randomAccessFile.seek(offset);
            for (int i = 0; i < length; ++i) {
                long z = ((long) randomAccessFile.read()) << (8 * i);
                result |= z;
            }
        } catch (IOException e) {
            throw new ROMAccessException(
                    String.format("Error while reading offset 0x%X in ROM of size 0x%X", offset, size), e);
        }

        for (int i = 0; i < length; ++i) {
            hasBeenRead[(int) (i+offset)] = true;
        }

        return result;
    }

    @Override
    public int[] readArray(long offset, int length) throws ROMAccessException {
        if ((offset < 0) || (offset + length - 1 >= size)) {
            throw new ROMAccessException(
                    String.format("Cannot read out-of-range offset 0x%X in ROM of size 0x%X", offset + length - 1, size));
        }

        int[] result = new int[length];
        try {
            randomAccessFile.seek(offset);
            for (int i = 0; i < length; ++i) {
                result[i] = randomAccessFile.read();
            }
        } catch (IOException e) {
            throw new ROMAccessException(
                    String.format("Error while reading offset 0x%X in ROM of size 0x%X", offset, size), e);
        }

        for (int i = 0; i < length; ++i) {
            hasBeenRead[(int) (i+offset)] = true;
        }

        return result;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public boolean hasBeenRead(long offset) {
        return hasBeenRead[(int) offset];
    }
}
