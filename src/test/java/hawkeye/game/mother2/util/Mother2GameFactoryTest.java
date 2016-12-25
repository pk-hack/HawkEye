package hawkeye.game.mother2.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.ROMType;
import hawkeye.config.model.TextTable;
import hawkeye.config.util.ControlCodeDeserializationFactory;
import hawkeye.config.util.ROMTypeDeserializationFactory;
import hawkeye.config.util.TextTableFactory;
import hawkeye.game.exceptions.UnrecognizedROMException;
import hawkeye.game.mother2.data.doors.Mother2GbaItemMap;
import hawkeye.game.mother2.games.Mother2Game;
import hawkeye.game.mother2.games.Mother2SnesGame;
import hawkeye.parse.model.TextSyntaxTreeNode;
import hawkeye.parse.util.CodeSyntaxTreeFactory;
import hawkeye.parse.util.CodeTreeParser;
import hawkeye.rom.util.ROM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Mother2GameFactoryTest {
    private static final String EARTHBOUND_MD5SUM = "eb checksum";
    private static final String EARTHBOUND_CHARACTER_TABLE_FILENAME = "eb-char-tbl-test.json";
    private static final String EARTHBOUND_CODELIST_FILENAME = "eb-codelist-test.json";
    private static final String MOTHER12_MD5SUM = "m12 checksum";
    private static final String MOTHER12_CHARACTER_TABLE_FILENAME = "m12-char-tbl-test.json";
    private static final String MOTHER12_CODELIST_FILENAME = "m12-codelist-test.json";

    @Mock private ROMTypeDeserializationFactory romTypeDeserializationFactory;
    @Mock private TextTableFactory textTableFactory;
    @Mock private ControlCodeDeserializationFactory controlCodeDeserializationFactory;
    @Mock private CodeSyntaxTreeFactory codeSyntaxTreeFactory;
    @Mock private EBTextCompressionCodesReader ebTextCompressionCodesReader;
    @Mock private Mother2GbaItemMap mother2GbaItemMap;

    @Mock private TextTable textTable;
    @Mock private Collection<ControlCode> controlCodes;
    @Mock private TextSyntaxTreeNode textSyntaxTreeNode;
    @Mock private CodeTreeParser codeTreeParser;

    @Mock private ROM earthboundRom;
    @Mock private ROM mother12Rom;
    @Mock private ROM unknownRom;

    private static final Collection<ROMType> TEST_ROMTYPES = ImmutableList.of(
            ROMType.builder()
                    .name("EarthBound")
                    .shortName("EB")
                    .platform("SNES")
                    .region("US")
                    .md5sum(EARTHBOUND_MD5SUM)
                    .files(ImmutableMap.of(
                            "character table", EARTHBOUND_CHARACTER_TABLE_FILENAME,
                            "codelist", EARTHBOUND_CODELIST_FILENAME))
                    .offsets(ImmutableMap.of("npc table", 123, "door pointer table", 456))
                    .build(),
            ROMType.builder()
                    .name("MOTHER 1+2")
                    .shortName("M1+2")
                    .platform("GBA")
                    .region("JP")
                    .md5sum(MOTHER12_MD5SUM)
                    .files(ImmutableMap.of(
                            "character table", MOTHER12_CHARACTER_TABLE_FILENAME,
                            "codelist", MOTHER12_CODELIST_FILENAME))
                    .offsets(ImmutableMap.of("npc table", 123, "door pointer table", 456))
                    .build()
    );

    private Mother2GameFactory mother2GameFactory;

    @Before
    public void init() throws Exception {
        when(romTypeDeserializationFactory.createCollectionFromFile(any(String.class)))
                .thenReturn(TEST_ROMTYPES);

        mother2GameFactory = new Mother2GameFactory(
                romTypeDeserializationFactory, textTableFactory, controlCodeDeserializationFactory,
                codeSyntaxTreeFactory, ebTextCompressionCodesReader, mother2GbaItemMap);

        when(earthboundRom.getMd5sum()).thenReturn(EARTHBOUND_MD5SUM);
        when(mother12Rom.getMd5sum()).thenReturn(MOTHER12_MD5SUM);
        when(unknownRom.getMd5sum()).thenReturn("unknown gfdsgfd");
    }

    @Test
    public void testCreateFromRom_EarthBound() throws Exception {
        when(textTableFactory.createFromFile(EARTHBOUND_CHARACTER_TABLE_FILENAME))
                .thenReturn(textTable);
        when(controlCodeDeserializationFactory.createCollectionFromFile(EARTHBOUND_CODELIST_FILENAME))
                .thenReturn(controlCodes);
        when(codeSyntaxTreeFactory.createTree(controlCodes, textTable))
                .thenReturn(textSyntaxTreeNode);

        Mother2Game game = mother2GameFactory.createFromRom(earthboundRom);
        assertTrue(game instanceof Mother2SnesGame);
        Mother2Game game2 = mother2GameFactory.createFromRom(earthboundRom);
        assertTrue(game2 instanceof Mother2SnesGame);
    }

    /*@Test
    public void testCreateFromRom_Mother12() throws Exception {
        when(textTableFactory.createFromFile(MOTHER12_CHARACTER_TABLE_FILENAME))
                .thenReturn(textTable);
        when(controlCodeDeserializationFactory.createCollectionFromFile(MOTHER12_CODELIST_FILENAME))
                .thenReturn(controlCodes);
        when(codeSyntaxTreeFactory.createTree(controlCodes, textTable))
                .thenReturn(textSyntaxTreeNode);
        when(textParserFactory.create(textSyntaxTreeNode))
                .thenReturn(codeTreeParser);

        Mother2Game game = mother2GameFactory.createFromRom(mother12Rom, TEST_ROMTYPES);
        assertTrue(game instanceof Mother2GbaGame);
        Mother2Game game2 = mother2GameFactory.createFromRom(mother12Rom, TEST_ROMTYPES);
        assertTrue(game2 instanceof Mother2GbaGame);
    }*/

    @Test(expected = UnrecognizedROMException.class)
    public void testCreateFromRom_Unrecognized() throws Exception {
        mother2GameFactory.createFromRom(unknownRom);
    }
}
