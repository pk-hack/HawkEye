package hawkeye.rom.util;

public class SnesUtil {
    public static long convertSnesAddressToRegularOffset(long snesAddress) {
        if (snesAddress >= 0xc00000) {
            return snesAddress - 0xc00000;
        } else {
            return snesAddress;
        }
    }
}
