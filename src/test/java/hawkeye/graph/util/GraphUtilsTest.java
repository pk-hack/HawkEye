package hawkeye.graph.util;

import com.google.common.collect.ImmutableList;
import hawkeye.graph.model.GraphNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class GraphUtilsTest {
    @Test
    public void testGetAllValues_OneNode() throws Exception {
        GraphNode<String> graph = new GraphNode<>("A");
        List<String> values = GraphUtils.getAllValues(graph);
        assertEquals(ImmutableList.of("A"), values);
    }

    @Test
    public void testGetAllValues_OneNode_Cycle() throws Exception {
        GraphNode<String> graph = new GraphNode<>("A");
        graph.addVertex(graph);

        List<String> values = GraphUtils.getAllValues(graph);
        assertEquals(ImmutableList.of("A"), values);
    }
}
