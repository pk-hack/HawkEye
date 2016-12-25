package hawkeye.parse.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidSyntaxTreeException extends Exception {
    public InvalidSyntaxTreeException(String message) {
        super(message);
    }

    public InvalidSyntaxTreeException(String message, Exception cause) {
        super(message, cause);
    }
}
