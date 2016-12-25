package hawkeye.graph.model;

import lombok.Value;

import java.util.List;
import java.util.Optional;

@Value
public class GraphComparison<T> {
    private boolean isEmpty;
    private boolean isIsomorphicComparison;
    private Optional<GraphNode<Integer>> structure;
    private List<List<T>> values;
}
