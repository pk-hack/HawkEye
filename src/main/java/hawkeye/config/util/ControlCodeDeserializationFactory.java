package hawkeye.config.util;

import com.google.inject.Inject;
import hawkeye.config.model.ControlCode;

public class ControlCodeDeserializationFactory extends DeserializationFactory<ControlCode> {
    @Inject
    public ControlCodeDeserializationFactory(GsonSingleton gson) {
        super(gson, ControlCode.class);
    }
}
