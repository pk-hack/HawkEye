package hawkeye.service.modules.exceptions;

public class ModuleException extends Exception {

    public ModuleException(String message, Exception cause) {
        super(message, cause);
    }
}
