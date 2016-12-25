package hawkeye.config.util;

import com.google.inject.Inject;
import hawkeye.config.model.ROMType;

public class ROMTypeDeserializationFactory  extends DeserializationFactory<ROMType>  {
    @Inject
    public ROMTypeDeserializationFactory(GsonSingleton gson) {
        super(gson, ROMType.class);
    }
}
