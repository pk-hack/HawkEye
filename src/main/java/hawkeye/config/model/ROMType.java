package hawkeye.config.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class ROMType  {
    @NonNull private String name;
    @SerializedName("short name") @NonNull private String shortName;
    @NonNull private String platform;
    @NonNull private String region;
    @NonNull private String md5sum;
    @NonNull private Map<String, String> files;
    @NonNull private Map<String, Integer> offsets;
}
