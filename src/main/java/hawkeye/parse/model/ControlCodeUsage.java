package hawkeye.parse.model;

import hawkeye.config.model.ControlCode;
import lombok.Value;

@Value
public class ControlCodeUsage {
    private ControlCode controlCode;
    private int[] parameters;
}
