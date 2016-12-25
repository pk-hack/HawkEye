package hawkeye.config.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import hawkeye.config.model.ControlCode;
import lombok.AllArgsConstructor;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@AllArgsConstructor
public class ControlCodeDeserializer implements JsonDeserializer<ControlCode> {

    public ControlCode deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
            throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();

        Optional<ControlCode.ReferenceSettings> referenceSettings = deserializeReferenceSettings(object);

        List<Integer> identifier;
        try {
            identifier = StreamSupport.stream(object.getAsJsonArray("Identifier").getAsJsonArray().spliterator(), true)
                    .map(JsonElement::getAsInt)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
            identifier = ImmutableList.of(object.getAsJsonPrimitive("Identifier").getAsInt());
        }

        Optional<Integer> length;
        if (object.has("Length")) {
            length = Optional.of(object.get("Length").getAsInt());
        } else {
            length = Optional.empty();
        }

        Optional<String> dialogueRepresentation;
        if (object.has("DialogueRepresentation")) {
            dialogueRepresentation = Optional.of(object.get("DialogueRepresentation").getAsString());
        } else {
            dialogueRepresentation = Optional.empty();
        }

        boolean isTerminal = object.has("IsEnd") && object.get("IsEnd").getAsBoolean();

        boolean isSuppressNextTerminator = object.has("SuppressNextTerminator") && object.get("SuppressNextTerminator").getAsBoolean();

        // If it's terminal, then the reference settings have to have isGoto = true and isConditional = false
        if (isTerminal && referenceSettings.isPresent()
                && !(referenceSettings.get().isGoto() && !referenceSettings.get().isConditional())) {
            throw new JsonParseException("Cannot parse Control Code that IsEnd, but does not have IsGoto and !IsConditional");
        }

        // If the length isn't present, it must have a countoffset
        if (!length.isPresent() && referenceSettings.isPresent()
                && !referenceSettings.get().getCountOffset().isPresent()) {
            throw new JsonParseException("Cannot parse Control Code that has no Length but also no CountOffset");
        }

        return ControlCode.builder()
                .identifier(identifier)
                .description(object.get("Description").getAsString())
                .length(length)
                .isTerminal(isTerminal)
                .isSuppressNextTerminator(isSuppressNextTerminator)
                .referenceSettings(referenceSettings)
                .dialogueRepresentation(dialogueRepresentation)
                .build();
    }

    private Optional<ControlCode.ReferenceSettings> deserializeReferenceSettings(JsonObject object) {
        if (!object.has("ReferenceSettings")) {
            return Optional.empty();
        }

        JsonObject rsObject = object.getAsJsonObject("ReferenceSettings");

        int referenceOffset = rsObject.get("ReferencesOffset").getAsInt();
        int referenceLength = rsObject.get("ReferenceLength").getAsInt();

        Optional<Integer> countOffset;
        if (rsObject.has("CountOffset")) {
            countOffset = Optional.of(rsObject.get("CountOffset").getAsInt());
        } else {
            countOffset = Optional.empty();
        }

        boolean isConditional = rsObject.get("IsConditional").getAsBoolean();
        boolean isGoto = rsObject.get("IsGoto").getAsBoolean();
        boolean isAbsoluteAddressing;
        if (!rsObject.has("IsAbsoluteAddressing")) {
            isAbsoluteAddressing = true;
        } else {
            isAbsoluteAddressing = rsObject.get("IsAbsoluteAddressing").getAsBoolean();
        }

        return Optional.of(ControlCode.ReferenceSettings.builder()
                .referencesOffset(referenceOffset)
                .referenceLength(referenceLength)
                .countOffset(countOffset)
                .isConditional(isConditional)
                .isGoto(isGoto)
                .isAbsoluteAddressing(isAbsoluteAddressing)
                .build());

    }
}
