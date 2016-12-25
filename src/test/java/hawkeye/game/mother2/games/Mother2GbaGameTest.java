package hawkeye.game.mother2.games;

import com.google.common.collect.ImmutableMap;
import hawkeye.config.model.ROMType;
import hawkeye.game.mother2.data.doors.Mother2GbaItemMap;
import hawkeye.parse.util.CodeTreeParser;
import hawkeye.rom.util.ROM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class Mother2GbaGameTest {
    private static final String MOTHER12_MD5SUM = "m12 checksum";
    private static final String MOTHER12_CHARACTER_TABLE_FILENAME = "m12-char-tbl-test.json";
    private static final String MOTHER12_CODELIST_FILENAME = "m12-codelist-test.json";
    private static final ROMType GBA_ROMTYPE = ROMType.builder()
            .name("MOTHER 1+2")
            .shortName("M1+2")
            .platform("GBA")
            .region("JP")
            .md5sum(MOTHER12_MD5SUM)
            .files(ImmutableMap.of(
                    "character table", MOTHER12_CHARACTER_TABLE_FILENAME,
                    "codelist", MOTHER12_CODELIST_FILENAME))
            .offsets(ImmutableMap.of("npc table", 123, "door pointer table", 456))
            .build();

    @Mock private ROM rom;
    @Mock private CodeTreeParser codeTreeParser;
    @Mock private Mother2GbaItemMap mother2GbaItemMap;

    private Mother2GbaGame mother2GbaGame;

    @Before
    public void init() throws Exception {
        mother2GbaGame = new Mother2GbaGame(GBA_ROMTYPE, rom, codeTreeParser, mother2GbaItemMap);
    }

    @Test
    public void testReadRelativePointer_Positive() throws Exception {
        when(rom.readMultiLittleEndian(3L, 4))
                .thenReturn(5L);

        Optional<Long> result = mother2GbaGame.readRelativePointer(3L, 10L);
        assertEquals(15L, result.get().longValue());
    }

    @Test
    public void testReadRelativePointer_Negative() throws Exception {
        when(rom.readMultiLittleEndian(3L, 4))
                .thenReturn(0xfffffff2L); // -14

        Optional<Long> result = mother2GbaGame.readRelativePointer(3L, 100L);
        assertEquals(86L, result.get().longValue());
    }
}
