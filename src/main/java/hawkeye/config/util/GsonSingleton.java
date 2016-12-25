package hawkeye.config.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import hawkeye.config.model.ControlCode;
import lombok.Getter;

public class GsonSingleton {
    @Getter
    private Gson gson;

    public GsonSingleton() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(ControlCode.class, new ControlCodeDeserializer());
        gson = gsonBuilder.create();
    }
}
