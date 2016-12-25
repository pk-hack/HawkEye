package hawkeye.rom.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ROMAccessException extends Exception {
    public ROMAccessException(String message) {
        super(message);
    }

    public ROMAccessException(String message, Exception cause) {
        super(message, cause);
    }
}
