package hawkeye.parse.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.model.ControlCodeUsage;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class TextTreeParserTest {
    private static final ControlCode LETTER_A_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0x00))
            .description("the letter A")
            .length(Optional.of(1))
            .isTerminal(false)
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.of("A"))
            .build();

    private static final ControlCode LETTER_B_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0x01))
            .description("the letter B")
            .length(Optional.of(1))
            .isTerminal(false)
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.of("B"))
            .build();

    private static final ControlCode LETTER_C_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0x02))
            .description("the letter C")
            .length(Optional.of(1))
            .isTerminal(false)
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.of("C"))
            .build();

    private static final ControlCode PAUSE_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0x10))
            .description("pauses for input")
            .length(Optional.of(1))
            .isTerminal(false)
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.of("[PAUSE]"))
            .build();

    private static final ControlCode BRANCH_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0x11))
            .description("branch")
            .length(Optional.of(5))
            .isTerminal(true)
            .referenceSettings(Optional.of(ControlCode.ReferenceSettings.builder()
                    .referencesOffset(1)
                    .countOffset(Optional.empty())
                    .isConditional(true)
                    .isGoto(true)
                    .build()))
            .dialogueRepresentation(Optional.empty())
            .build();

    private static final ControlCode END_CONTROL_CODE = ControlCode.builder()
            .identifier(ImmutableList.of(0xff))
            .description("pauses for input")
            .length(Optional.of(1))
            .isTerminal(true)
            .referenceSettings(Optional.empty())
            .dialogueRepresentation(Optional.of("[END]"))
            .build();

    private TextTreeParser textTreeParser;

    @Before
    public void init() throws Exception {
        textTreeParser = new TextTreeParser();
    }

    @Test
    public void testCreate_Empty() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[] {}));
        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        assertTrue(result.getVertices().get(0).getVertices().isEmpty());
        assertEquals("[END]", result.getVertices().get(0).getValue());
    }

    @Test
    public void testCreate_SingleLetter() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[] {}));
        source.addVertex(
                new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[]{})));
        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        assertTrue(result.getVertices().get(0).getVertices().isEmpty());
        assertEquals("A[END]", result.getVertices().get(0).getValue());
    }

    @Test
    public void testCreate_TwoLetters() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[] {}));
        source.addVertex(
                new GraphNode<>(new ControlCodeUsage(LETTER_B_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[]{})));
        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        result = result.getVertices().get(0);
        assertEquals(0, result.getVertices().size());
        assertEquals("AB[END]", result.getValue());
    }

    @Test
    public void testCreate_TwoStrings() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[] {}));
        source.addVertex(
                new GraphNode<>(new ControlCodeUsage(LETTER_B_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(PAUSE_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).getVertices().get(0).getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[]{})));
        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        result = result.getVertices().get(0);
        assertEquals(0, result.getVertices().size());
        assertEquals("AB[PAUSE]A[END]", result.getValue());
    }

    @Test
    public void testCreate_Branch() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[] {}));
        source.addVertex(
                new GraphNode<>(new ControlCodeUsage(BRANCH_CONTROL_CODE, new int[]{})));

        source.getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(LETTER_B_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[]{})));

        source.getVertices().get(0).addVertex(
                new GraphNode<>(new ControlCodeUsage(LETTER_C_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).getVertices().get(1).addVertex(
                new GraphNode<>(new ControlCodeUsage(END_CONTROL_CODE, new int[]{})));

        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        result = result.getVertices().get(0);
        assertEquals(2, result.getVertices().size());
        assertEquals("A", result.getValue());

        GraphNode<String> branch1 = result.getVertices().get(0);
        assertEquals(0, branch1.getVertices().size());
        assertEquals("C[END]", branch1.getValue());

        GraphNode<String> branch2 = result.getVertices().get(1);
        assertEquals(0, branch2.getVertices().size());
        assertEquals("B[END]", branch2.getValue());
    }

    @Test
    public void testCreate_Loop() throws Exception {
        GraphNode<ControlCodeUsage> source = new GraphNode<>(new ControlCodeUsage(LETTER_A_CONTROL_CODE, new int[] {}));
        source.addVertex(
                new GraphNode<>(new ControlCodeUsage(BRANCH_CONTROL_CODE, new int[]{})));
        source.getVertices().get(0).addVertex(source);

        GraphNode<String> result = textTreeParser.create(source);

        assertNull(result.getValue());
        assertEquals(1, result.getVertices().size());

        result = result.getVertices().get(0);
        assertEquals(1, result.getVertices().size());
        assertEquals("A", result.getValue());
        assertEquals(result, result.getVertices().get(0));
    }
}
