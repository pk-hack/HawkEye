package hawkeye.game.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class UnrecognizedROMException extends Exception {
    public UnrecognizedROMException(String message) {
        super(message);
    }

    public UnrecognizedROMException(String message, Exception cause) {
        super(message, cause);
    }
}

