package hawkeye.graph.util;

import com.google.common.collect.ImmutableList;
import hawkeye.graph.model.GraphComparison;
import hawkeye.graph.model.GraphNode;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GraphComparerTest {
    private GraphComparer graphComparer;

    @Before
    public void init() throws Exception {
        this.graphComparer = new GraphComparer();
    }

    @Test
    public void testCompare_DifferentNumberOfVertices() {
        GraphNode<String> graph1 = new GraphNode<>("A");
        GraphNode<String> graph2 = new GraphNode<>("B");
        graph2.addVertex(new GraphNode<>("C"));

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertFalse(result.isIsomorphicComparison());
    }

    @Test
    public void testCompare_DifferentLoop() {
        GraphNode<String> graph1 = new GraphNode<>("A");
        graph1.addVertex(graph1);
        GraphNode<String> graph2 = new GraphNode<>("B");
        graph2.addVertex(new GraphNode<>("B1"));
        graph2.getVertices().get(0).addVertex(graph2);

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertFalse(result.isIsomorphicComparison());
    }

    @Test
    public void testCompare_DifferentLoop2() {
        GraphNode<String> graph1 = new GraphNode<>("A");
        graph1.addVertex(new GraphNode<>("A1"));
        graph1.getVertices().get(0).addVertex(graph1);
        GraphNode<String> graph2 = new GraphNode<>("B");
        graph2.addVertex(new GraphNode<>("B1"));
        graph2.getVertices().get(0).addVertex(graph2.getVertices().get(0));

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertFalse(result.isIsomorphicComparison());
    }

    @Test
    public void testCompare_SingleNode() {
        GraphNode<String> graph1 = new GraphNode<>("A");
        GraphNode<String> graph2 = new GraphNode<>("B");

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertTrue(result.isIsomorphicComparison());

        GraphNode<Integer> structure = result.getStructure().get();
        assertEquals(-1, structure.getValue().intValue());
        assertEquals(1, structure.getVertices().size());

        structure = structure.getVertices().get(0);
        assertEquals(0, structure.getValue().intValue());
        assertEquals(0, structure.getVertices().size());

        assertEquals(ImmutableList.of("A"), result.getValues().get(0));
        assertEquals(ImmutableList.of("B"), result.getValues().get(1));
    }

    @Test
    public void testCompare_SingleNodeLoop() {
        GraphNode<String> graph1 = new GraphNode<>("A");
        graph1.addVertex(graph1);
        GraphNode<String> graph2 = new GraphNode<>("B");
        graph2.addVertex(graph2);

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertTrue(result.isIsomorphicComparison());

        GraphNode<Integer> structure = result.getStructure().get();
        assertEquals(-1, structure.getValue().intValue());
        assertEquals(1, structure.getVertices().size());

        structure = structure.getVertices().get(0);
        assertEquals(0, structure.getValue().intValue());
        assertEquals(1, structure.getVertices().size());
        assertEquals(structure, structure.getVertices().get(0));

        assertEquals(ImmutableList.of("A"), result.getValues().get(0));
        assertEquals(ImmutableList.of("B"), result.getValues().get(1));
    }

    @Test
    public void testCompare_DialogueTree() {
        GraphNode<String> tmp;

        GraphNode<String> graph1 = new GraphNode<>("Do you like apples?");
        graph1.addVertex(new GraphNode<>("You do? I'm glad."));
        graph1.addVertex(new GraphNode<>("Seriously? They're delicious."));
        tmp = new GraphNode<>("Please answer the question.");
        tmp.addVertex(graph1);
        graph1.addVertex(tmp);

        GraphNode<String> graph2 = new GraphNode<>("りんごがすきですか?");
        graph2.addVertex(new GraphNode<>("そうですか? よかった."));
        graph2.addVertex(new GraphNode<>("ほんとうに? おいしいですよ."));
        tmp = new GraphNode<>("おしえてください.");
        tmp.addVertex(graph2);
        graph2.addVertex(tmp);

        GraphComparison<String> result = graphComparer.compare(ImmutableList.of(graph1, graph2));
        assertTrue(result.isIsomorphicComparison());

        GraphNode<Integer> structure = result.getStructure().get();
        List<String> strings1 = result.getValues().get(0);
        List<String> strings2 = result.getValues().get(1);
        int stringId;
        assertEquals(-1, structure.getValue().intValue());
        assertEquals(1, structure.getVertices().size());

        structure = structure.getVertices().get(0);
        stringId = structure.getValue().intValue();
        assertEquals("Do you like apples?", strings1.get(stringId));
        assertEquals("りんごがすきですか?", strings2.get(stringId));
        assertEquals(3, structure.getVertices().size());

        stringId = structure.getVertices().get(2).getValue().intValue();
        assertEquals("You do? I'm glad.", strings1.get(stringId));
        assertEquals("そうですか? よかった.", strings2.get(stringId));
        assertEquals(0, structure.getVertices().get(2).getVertices().size());

        stringId = structure.getVertices().get(1).getValue().intValue();
        assertEquals("Seriously? They're delicious.", strings1.get(stringId));
        assertEquals("ほんとうに? おいしいですよ.", strings2.get(stringId));
        assertEquals(0, structure.getVertices().get(1).getVertices().size());

        stringId = structure.getVertices().get(0).getValue().intValue();
        assertEquals("Please answer the question.", strings1.get(stringId));
        assertEquals("おしえてください.", strings2.get(stringId));
        assertEquals(1, structure.getVertices().get(0).getVertices().size());
        assertEquals(structure, structure.getVertices().get(0).getVertices().get(0));
    }
}
