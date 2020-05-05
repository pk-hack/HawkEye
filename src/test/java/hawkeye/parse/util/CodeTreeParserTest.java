package hawkeye.parse.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.ROMType;
import hawkeye.config.model.TextTable;
import hawkeye.config.util.ControlCodeDeserializationFactory;
import hawkeye.config.util.GsonSingleton;
import hawkeye.config.util.TextTableFactory;
import hawkeye.game.model.Game;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.exceptions.InvalidTextException;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.parse.model.TextSyntaxTreeNode;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ArrayROM;
import hawkeye.rom.util.ROM;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class CodeTreeParserTest {
    private class TestGame extends Game {
        public TestGame(ROMType romType, ROM rom) {
            super(romType, rom);
        }

        @Override
        public int read(long offset) throws ROMAccessException {
            return rom.read(offset);
        }

        @Override
        public int[] readArray(long offset, int length) throws ROMAccessException {
            return rom.readArray(offset, length);
        }

        @Override
        public Optional<Long> readPointer(long offset) throws ROMAccessException {
            long pointer = rom.readMultiLittleEndian(offset, 3);
            if (pointer == 0) {
                return Optional.empty();
            }
            return Optional.of(pointer);
        }

        @Override
        public Optional<Long> readRelativePointer(long offset, long base) throws ROMAccessException {
            return Optional.empty();
        }
    }
    private static final ROMType TEST_ROMTYPE = ROMType.builder()
            .name("Shadow's Wacky Day")
            .shortName("MTWD")
            .platform("LaserActive Mega LD")
            .region("US")
            .md5sum("testmd5")
            .files(Collections.EMPTY_MAP)
            .offsets(Collections.EMPTY_MAP)
            .build();

    private static final String TEST_TEXTTABLE_FILENAME = "src/test/data/test-char-lookup.json";
    private static final String TEST_CONTROLCODES_FILENAME = "src/test/data/test-codelist2.json";
    private static final String TEST_CONTROLCODES2_FILENAME = "src/test/data/test-codelist3.json";

    private CodeTreeParser codeTreeParser, codeTreeParser2;

    @Before
    public void init() throws Exception {
        GsonSingleton gson = new GsonSingleton();

        TextTable textTable = new TextTableFactory(gson).createFromFile(TEST_TEXTTABLE_FILENAME);

        CodeSyntaxTreeFactory codeSyntaxTreeFactory = new CodeSyntaxTreeFactory();
        Collection<ControlCode> controlCodes = new ControlCodeDeserializationFactory(gson).createCollectionFromFile(TEST_CONTROLCODES_FILENAME);
        TextSyntaxTreeNode syntaxTree = codeSyntaxTreeFactory.createTree(controlCodes, textTable);
        codeTreeParser = new CodeTreeParser(syntaxTree);

        controlCodes = new ControlCodeDeserializationFactory(gson).createCollectionFromFile(TEST_CONTROLCODES2_FILENAME);
        syntaxTree = codeSyntaxTreeFactory.createTree(controlCodes, textTable);
        codeTreeParser2 = new CodeTreeParser(syntaxTree);
    }

    @Test
    public void testParseToGraph_SingleCode() throws Exception{
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_SingleCode_Text() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {0, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(0), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_TwoCodes_Text() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {0, 10, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(0), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(10),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(1), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_SingleParameterCode_Text() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {0, 11, 2, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(0), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(11),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{2},
                graph.getValue().getParameters());

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(1), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_TwoCodesThatStartSimilarly() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{13, 1, 200, 13, 2, 201, 202, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(13, 1),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{200},
                graph.getValue().getParameters());

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(13, 2),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{201, 202},
                graph.getValue().getParameters());

        graph = graph.getVertices().get(0);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_Reference_Unconditional_Call() throws Exception {
        // Expected graph: 17 -> 1 -> END -> 2 -> END
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{17, 7, 0, 0, 0, 2, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(17),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{7, 0, 0, 0},
                graph.getValue().getParameters());
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(2),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    public void testParseToGraph_Reference_Unconditional_Goto() throws Exception {
        // Expected graph: 15 -> 1 -> END
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{15, 7, 0, 0, 0, 2, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(15),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{7, 0, 0, 0},
                graph.getValue().getParameters());
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    public void testParseToGraph_Reference_Conditional_Call() throws Exception {
        // Expected graph: 16 -> 1 -> END -> 2 -> END
        //                  |--> 2 -> END
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{16, 7, 0, 0, 0, 2, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);
        GraphNode<ControlCodeUsage> root = graph;

        assertEquals(2, graph.getVertices().size());
        assertEquals(ImmutableList.of(16),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{7, 0, 0, 0},
                graph.getValue().getParameters());

        // Branch 1
        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(2),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());

        // Branch 2
        graph = root.getVertices().get(1);
        assertEquals(ImmutableList.of(2),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    public void testParseToGraph_Reference_Conditional_Goto() throws Exception {
        // Expected graph: 14 -> 1 -> END
        //                  |--> 2 -> END
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{14, 7, 0, 0, 0, 2, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);
        GraphNode<ControlCodeUsage> root = graph;

        assertEquals(2, graph.getVertices().size());
        assertEquals(ImmutableList.of(14),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{7, 0, 0, 0},
                graph.getValue().getParameters());

        // Branch 1
        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());

        // Branch 2
        graph = root.getVertices().get(1);
        assertEquals(ImmutableList.of(2),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    public void testParseToGraph_Conditional_Goto_VariableLength() throws Exception {
        // Expected graph: 12 -> 0 -> 255
        //                  |--> 1 -> 255
        //                  |--> 2 -> 255
        //                  |--> 255
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{
                12, 3, 15, 0, 0, 0, 17, 0, 0, 0, 19, 0, 0, 0, 255,
                0, 255,
                1, 255,
                2, 255
        }));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(4, graph.getVertices().size());
        assertEquals(ImmutableList.of(12),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{3, 15, 0, 0, 0, 17, 0, 0, 0, 19, 0, 0, 0},
                graph.getValue().getParameters());

        GraphNode<ControlCodeUsage> subgraph = graph.getVertices().get(0);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(0), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);

        subgraph = graph.getVertices().get(1);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(1), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);

        subgraph = graph.getVertices().get(2);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(2), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);

        graph = graph.getVertices().get(3);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_Unconditional_Goto_VariableLength() throws Exception {
        // Expected graph: 18 -> 0 -> 255
        //                  |--> 1 -> 255
        //                  |--> 2 -> 255
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{
                18, 3, 15, 0, 0, 0, 17, 0, 0, 0, 19, 0, 0, 0, 255,
                0, 255,
                1, 255,
                2, 255
        }));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(3, graph.getVertices().size());
        assertEquals(ImmutableList.of(18),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{3, 15, 0, 0, 0, 17, 0, 0, 0, 19, 0, 0, 0},
                graph.getValue().getParameters());

        GraphNode<ControlCodeUsage> subgraph = graph.getVertices().get(0);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(0), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);

        subgraph = graph.getVertices().get(1);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(1), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);

        subgraph = graph.getVertices().get(2);
        assertEquals(1, subgraph.getVertices().size());
        assertEquals(ImmutableList.of(2), subgraph.getValue().getControlCode().getIdentifier());

        subgraph = subgraph.getVertices().get(0);
        assertTrue(subgraph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_Circular_Goto() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{0, 14, 1, 0, 0, 0, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(0),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(2, graph.getVertices().size());
        assertEquals(ImmutableList.of(14),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{1, 0, 0, 0}, graph.getValue().getParameters());

        GraphNode<ControlCodeUsage> subgraph1 = graph.getVertices().get(0);
        assertEquals(subgraph1, graph);

        graph = graph.getVertices().get(1);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_Circular_Call() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{0, 16, 1, 0, 0, 0, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(0),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());
        graph = graph.getVertices().get(0);

        do {
            assertEquals(2, graph.getVertices().size());
            assertEquals(ImmutableList.of(16),
                    graph.getValue().getControlCode().getIdentifier());
            assertArrayEquals(new int[]{1, 0, 0, 0}, graph.getValue().getParameters());
            graph = graph.getVertices().get(0);
        } while (graph.getVertices().size() > 0);

        // Reached maximum recursion depth
        assertEquals(0, graph.getVertices().size());
        assertEquals(ImmutableList.of(-1),
                graph.getValue().getControlCode().getIdentifier());
    }

    @Test
    public void testParseToGraph_Reused_Offset_With_Different_Call_Stack() throws Exception {
        // Expected graph: 16 -> 17 -> 3 -> 255 -> 1 -> 255
        //                  |--> 17 -> 3 -> 255 -> 2 -> 255
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{
                // offset 0
                14, 12, 0, 0, 0, 17, 19, 0, 0, 0, 1, 255,
                // offset 12
                17, 19, 0, 0, 0, 2, 255,
                // offset 19
                3, 255
        }));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(14),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{12, 0, 0, 0}, graph.getValue().getParameters());
        assertEquals(2, graph.getVertices().size());

        // Branch 1
        GraphNode<ControlCodeUsage> subgraph = graph.getVertices().get(1);
        assertEquals(ImmutableList.of(17),
                subgraph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{19, 0, 0, 0}, subgraph.getValue().getParameters());
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(3),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(1),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(0, subgraph.getVertices().size());

        // Branch 2
        subgraph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(17),
                subgraph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{19, 0, 0, 0}, subgraph.getValue().getParameters());
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(3),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(2),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(1, subgraph.getVertices().size());

        subgraph = subgraph.getVertices().get(0);
        assertEquals(ImmutableList.of(255),
                subgraph.getValue().getControlCode().getIdentifier());
        assertEquals(0, subgraph.getValue().getParameters().length);
        assertEquals(0, subgraph.getVertices().size());
    }

    @Test(expected = InvalidTextException.class)
    public void testParseToGraph_UnknownCode() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] { 254 }));
        codeTreeParser.parseToGraph(game, 0);
    }

    @Test(expected = InvalidTextException.class)
    public void testParseToGraph_UnknownControlCode() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] { 13, 3 }));
        codeTreeParser.parseToGraph(game, 0);
    }

    @Test
    public void testParseToGraph_SuppressNextTerminator() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {0, 19, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(0), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(19), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    @Test
    public void testParseToGraph_PartialMatch() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {1, 1, 255, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser2.parseToGraph(game, 0);

        assertEquals(ImmutableList.of(1), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(1, 255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(255), graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
        assertEquals(0, graph.getVertices().size());
    }

    public void testParseToGraph_SingleLine() throws Exception {
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[] {0, 10, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0, 0);

        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(0), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(10),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);

        graph = graph.getVertices().get(0);
        assertEquals(1, graph.getVertices().size());
        assertEquals(ImmutableList.of(1), graph.getValue().getControlCode().getIdentifier());

        graph = graph.getVertices().get(0);
        assertTrue(graph.getVertices().isEmpty());
        assertEquals(ImmutableList.of(255),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getValue().getParameters().length);
    }

    @Test
    public void testParseToGraph_SingleLine_Reference_Unconditional_Goto() throws Exception {
        // Expected graph: 15 -> END
        Game game = new TestGame(TEST_ROMTYPE, new ArrayROM(new int[]{15, 7, 0, 0, 0, 2, 255, 1, 255}));
        GraphNode<ControlCodeUsage> graph = codeTreeParser.parseToGraph(game, 0, 0);

        assertEquals(ImmutableList.of(15),
                graph.getValue().getControlCode().getIdentifier());
        assertArrayEquals(new int[]{7, 0, 0, 0},
                graph.getValue().getParameters());
        assertEquals(1, graph.getVertices().size());

        graph = graph.getVertices().get(0);
        assertEquals(ImmutableList.of(-1),
                graph.getValue().getControlCode().getIdentifier());
        assertEquals(0, graph.getVertices().size());
    }
}
