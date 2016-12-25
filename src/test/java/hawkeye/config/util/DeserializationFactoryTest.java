package hawkeye.config.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.ROMType;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class DeserializationFactoryTest {
    private static final String TEST_ROMTYPES_FILENAME = "src/test/data/test-romtypes.json";
    private static final Collection<ROMType> TEST_ROMTYPES = ImmutableList.of(
            ROMType.builder()
                    .name("Name 1")
                    .shortName("N1")
                    .platform("Platform 1")
                    .region("AA")
                    .md5sum("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")
                    .files(ImmutableMap.of("a", "1a.json", "b", "1b.json"))
                    .offsets(ImmutableMap.of("qq", 11111))
                    .build(),
            ROMType.builder()
                    .name("Name 2")
                    .shortName("N2")
                    .platform("Platform 2")
                    .region("BB")
                    .md5sum("bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb")
                    .files(ImmutableMap.of("z1", "2a.json", "z2", "2b.json"))
                    .offsets(ImmutableMap.of("rr", 22222))
                    .build()
    );

    private static final String TEST_CONTROLCODES_FILENAME = "src/test/data/test-codelist.json";
    private static final String TEST_CONTROLCODES_STRING =
            "[{ \"Identifier\": [1, 2], \"Description\": \"description1\", \"Length\": 5, \"DialogueRepresentation\": \"test\" }]";
    private static final Collection<ControlCode> TEST_CONTORLCODES = ImmutableList.of(
            ControlCode.builder()
                    .identifier(ImmutableList.of(1, 2))
                    .description("description1")
                    .length(Optional.of(5))
                    .isTerminal(false)
                    .referenceSettings(Optional.empty())
                    .dialogueRepresentation(Optional.of("test"))
                    .build()
    );


    private DeserializationFactory<ROMType> romTypeFactory;
    private DeserializationFactory<ControlCode> controlCodeFactory;

    @Before
    public void init() {
        GsonSingleton gson = new GsonSingleton();
        romTypeFactory = new ROMTypeDeserializationFactory(gson);
        controlCodeFactory = new ControlCodeDeserializationFactory(gson);
    }

    @Test(expected = FileNotFoundException.class)
    public void test_ROMType_createListFromFile_DNE() throws Exception {
        romTypeFactory.createCollectionFromFile("does-not-exist.json");
    }

    @Test
    public void test_ROMType_createListFromFile() throws Exception {
        Collection<ROMType> romTypes = romTypeFactory.createCollectionFromFile(TEST_ROMTYPES_FILENAME);
        assertEquals(TEST_ROMTYPES, romTypes);
    }

    @Test
    public void test_ControlCode_createListFromString() throws Exception {
        Collection<ControlCode> controlCodes = controlCodeFactory.createCollectionFromString(TEST_CONTROLCODES_STRING);
        assertEquals(TEST_CONTORLCODES, controlCodes);
    }

    @Test
    public void test_ControlCode_createListFromFile() throws Exception {
        Collection<ControlCode> controlCodes = controlCodeFactory.createCollectionFromFile(TEST_CONTROLCODES_FILENAME);
        assertEquals(TEST_CONTORLCODES, controlCodes);
    }

    @Test(expected = JsonParseException.class)
    public void test_ControlCode_invalidControlCode() throws Exception {
        Collection<ControlCode> controlCodes = controlCodeFactory.createCollectionFromString(
                "[{ \"Identifier\": [1], \"Description\": \"a\", \"IsEnd\": true, \"Length\": 1, "
                        + "\"ReferenceSettings\": { \"ReferencesOffset\": 1, \"ReferenceLength\": 4, \"IsConditional\": false, \"IsGoto\": false } }]"
        );
        assertEquals(TEST_CONTORLCODES, controlCodes);
    }

    @Test(expected = JsonParseException.class)
    public void test_ControlCode_invalidControlCode2() throws Exception {
        Collection<ControlCode> controlCodes = controlCodeFactory.createCollectionFromString(
                "[{ \"Identifier\": [1], \"Description\": \"a\", \"IsEnd\": false, "
                        + "\"ReferenceSettings\": { \"ReferencesOffset\": 1, \"ReferenceLength\": 4, \"IsConditional\": false, \"IsGoto\": false } }]"
        );
        assertEquals(TEST_CONTORLCODES, controlCodes);
    }
}
