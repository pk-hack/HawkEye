package hawkeye.graph.util;

import com.google.common.collect.ImmutableList;
import hawkeye.graph.model.GraphComparison;
import hawkeye.graph.model.GraphNode;
import lombok.Value;

import java.util.*;
import java.util.stream.Collectors;

public class GraphComparer {
    @Value
    private static class RecursionStackEntry<T> {
        private GraphNode<Integer> previous;
        private GraphNode<T> next1, next2;
    }

    public <T> GraphComparison<T> compare(List<GraphNode<T>> graphs) {
        if (graphs.size() != 2) {
            throw new IllegalArgumentException("Can only compare 2 graphs");
        }

        boolean anyEmptyGraphs = false;
        boolean allEmptyGraphs = true;
        for (GraphNode<T> graph : graphs) {
            boolean isEmpty = (graph.getValue() == null) && (graph.getVertices().size() == 0);
            anyEmptyGraphs |= isEmpty;
            allEmptyGraphs &= isEmpty;
        }

        if (allEmptyGraphs) {
            return new GraphComparison<>(true, false, Optional.empty(), Collections.EMPTY_LIST);
        } else if (anyEmptyGraphs) {
            return compareNonisomorphic(graphs);
        }

        return compare(graphs.get(0), graphs.get(1));
    }

    private <T> GraphComparison<T> compare(GraphNode<T> graph1, GraphNode<T> graph2) {
        int entryId = 0;
        List<T> values1 = new ArrayList<>();
        List<T> values2 = new ArrayList<>();
        GraphNode<Integer> structure = new GraphNode<>(-1);

        Map<GraphNode<T>, GraphNode<Integer>> alreadyVisitedNodes1 = new HashMap<>();
        Map<GraphNode<T>, GraphNode<Integer>> alreadyVisitedNodes2 = new HashMap<>();

        Stack<RecursionStackEntry<T>> recursionStack = new Stack<>();
        recursionStack.add(new RecursionStackEntry(structure, graph1, graph2));

        while (!recursionStack.isEmpty()) {
            RecursionStackEntry<T> recursionStackEntry = recursionStack.pop();
            GraphNode<Integer> previous = recursionStackEntry.getPrevious();
            GraphNode<T> next1 = recursionStackEntry.getNext1();
            GraphNode<T> next2 = recursionStackEntry.getNext2();

            GraphNode<Integer> alreadyVisitedNode1 = alreadyVisitedNodes1.get(next1);
            GraphNode<Integer> alreadyVisitedNode2 = alreadyVisitedNodes2.get(next2);
            if ((alreadyVisitedNode1 == null) ^ (alreadyVisitedNode2 == null)) {
                // If we've already visited next1 but not next2 or vice versa, that implies that the graphs are not isomorphic
                return compareNonisomorphic(ImmutableList.of(graph1, graph2));
            } else if (alreadyVisitedNode1 != null) {
                // If we've visited both nodes, and...
                if (alreadyVisitedNode1.equals(alreadyVisitedNode2)) {
                    // ...they're the same, link the previous node to the already visited node
                    previous.addVertex(alreadyVisitedNode1);
                } else {
                    // ...they're not the same, this implies that the graphs are ont isomorphic
                    return compareNonisomorphic(ImmutableList.of(graph1, graph2));
                }
            } else {
                // If we've visited neither next1 nor next2 already, then...

                if (next1.getVertices().size() != next2.getVertices().size()) {
                    // ...if they have different numbers of vertices, this implies the graphs are not isomorphic
                    return compareNonisomorphic(ImmutableList.of(graph1, graph2));
                }

                // ...if they have the sane number of vertices, continue with the recursion
                GraphNode<Integer> nextNode = new GraphNode<>(entryId);
                ++entryId;
                previous.addVertex(nextNode);

                values1.add(next1.getValue());
                values2.add(next2.getValue());

                alreadyVisitedNodes1.put(next1, nextNode);
                alreadyVisitedNodes2.put(next2, nextNode);

                Iterator<GraphNode<T>> iterator1 = next1.getVertices().iterator();
                Iterator<GraphNode<T>> iterator2 = next2.getVertices().iterator();
                while (iterator1.hasNext() && iterator2.hasNext()) {
                    recursionStack.add(new RecursionStackEntry<>(nextNode, iterator1.next(), iterator2.next()));
                }
            }
        }

        return new GraphComparison<>(false, true, Optional.of(structure), ImmutableList.of(values1, values2));
    }

    private <T> GraphComparison<T> compareNonisomorphic(List<GraphNode<T>> graphs) {
        List<List<T>> values = graphs.stream().map(GraphUtils::getAllValues).collect(Collectors.toList());
        return new GraphComparison<>(false, false, Optional.empty(), values);
    }
}
