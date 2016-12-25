package hawkeye.config.model;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
@Builder
public class ControlCode {
    @Value
    @Builder
    public static class ReferenceSettings {
        @NonNull
        private int referencesOffset;
        @NonNull
        private int referenceLength;
        @NonNull
        private boolean isAbsoluteAddressing;
        @NonNull
        private Optional<Integer> countOffset;
        @NonNull
        private boolean isConditional;
        @NonNull
        private boolean isGoto;
    }

    @NonNull private List<Integer> identifier;
    @NonNull private String description;
    @NonNull private Optional<Integer> length;
    private boolean isTerminal;
    private boolean isSuppressNextTerminator;
    @NonNull private Optional<ReferenceSettings> referenceSettings;
    @NonNull private Optional<String> dialogueRepresentation;
}
