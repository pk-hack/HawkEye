package hawkeye.parse.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidTextException extends Exception {
    public InvalidTextException(String message) {
        super(message);
    }

    public InvalidTextException(String message, Exception cause) {
        super(message, cause);
    }
}
