package hawkeye.service.util;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class LineMatchCheckerTest {
    private LineMatchChecker lineMatchChecker;

    @Before
    public void init() {
        this.lineMatchChecker = new LineMatchChecker();
    }

    @Test
    public void testAreLinesSame_Basic() throws Exception {
        assertTrue(lineMatchChecker.areLinesSame(ImmutableList.of("abc", "abc")));
    }

    @Test
    public void testAreLinesSame_Whitespace() throws Exception {
        assertTrue(lineMatchChecker.areLinesSame(ImmutableList.of("abc def", "abcdef")));
    }

    @Test
    public void testAreLinesSame_Japanese() throws Exception {
        assertTrue(lineMatchChecker.areLinesSame(ImmutableList.of(
                "＠どうしても　ツーソンに\n" +
                        "　いくつもりなのかな？\n" +
                        "　　はい　　いいえ",
                "＠どうしても　ツーソンに\n" +
                        "　いくつもりなのかな？\n" +
                        "はい　いいえ")));
    }
}
