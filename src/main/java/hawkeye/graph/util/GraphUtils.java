package hawkeye.graph.util;

import hawkeye.graph.model.GraphNode;

import java.util.*;

public class GraphUtils {
    public static <T> List<T> getAllValues(GraphNode<T> root) {
        List<T> result = new ArrayList<>();

        Set<GraphNode<T>> alreadyVisitedNodes = new HashSet<>();
        Stack<GraphNode<T>> recursionStack = new Stack<>();
        recursionStack.add(root);

        while (!recursionStack.isEmpty()) {
            GraphNode<T> node = recursionStack.pop();
            if (alreadyVisitedNodes.contains(node)) {
                continue;
            }
            alreadyVisitedNodes.add(node);

            result.add(node.getValue());

            recursionStack.addAll(node.getVertices());
        }

        return result;
    }
}
