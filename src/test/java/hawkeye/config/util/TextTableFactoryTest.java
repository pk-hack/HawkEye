package hawkeye.config.util;

import hawkeye.config.model.TextTable;
import org.junit.Before;
import org.junit.Test;

import java.io.FileNotFoundException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TextTableFactoryTest {
    private static final String TEST_TEXTTABLE_FILENAME = "src/test/data/test-char-lookup.json";
    private static final TextTable TEST_TEXT_TABLE = new TextTable(
            new String[] {"a", "b", "c", "d", "e", "f"}
    );

    private TextTableFactory factory;

    @Before
    public void init() {
        factory = new TextTableFactory(new GsonSingleton());
    }

    @Test(expected = FileNotFoundException.class)
    public void testCreateListFromFile_DNE() throws Exception {
        factory.createFromFile("does-not-exist.json");
    }

    @Test
    public void testCreateFromFile() throws Exception {
        TextTable table = factory.createFromFile(TEST_TEXTTABLE_FILENAME);
        assertEquals("a", table.get(0).get());
        assertEquals("b", table.get(1).get());
        assertEquals("c", table.get(2).get());
        assertEquals("d", table.get(3).get());
        assertEquals("e", table.get(4).get());
        assertEquals("f", table.get(5).get());
        assertFalse(table.get(6).isPresent());
    }
}
