package hawkeye.rom.util;

public class GbaUtil {
    public static long convertGbaAddressToRegularOffset(long gbaAddress) {
        if (gbaAddress >= 0x8000000) {
            return gbaAddress - 0x8000000;
        } else {
            return gbaAddress;
        }
    }
}
