package hawkeye.parse.util;

import hawkeye.config.model.ControlCode;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.model.ControlCodeUsage;
import lombok.Value;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class TextTreeParser {
    @Value
    private class RecursionStackEntry {
        private GraphNode<String> first;
        private GraphNode<ControlCodeUsage> second;
    }

    public GraphNode<String> create(GraphNode<ControlCodeUsage> textGraph) {
        GraphNode<String> root = new GraphNode<>(null);

        Map<GraphNode<ControlCodeUsage>, GraphNode<String>> alreadyVisitedNodes = new HashMap<>();

        Stack<RecursionStackEntry> recursionStack = new Stack<>();
        recursionStack.add(new RecursionStackEntry(root, textGraph));

        while (!recursionStack.isEmpty()) {
            RecursionStackEntry recursionStackEntry = recursionStack.pop();
            GraphNode<String> previousNode = recursionStackEntry.getFirst();
            GraphNode<ControlCodeUsage> ccNode = recursionStackEntry.getSecond();

            GraphNode<String> nextNode = alreadyVisitedNodes.get(ccNode);
            if (nextNode != null) {
                previousNode.addVertex(nextNode);
            } else {
                ControlCode controlCode = ccNode.getValue().getControlCode();
                StringBuilder dialogueBuilder = new StringBuilder();

                while (true) {
                    if (controlCode.getReferenceSettings().isPresent() || (ccNode.getVertices().size() > 1)) {
                        break;
                    }

                    if (controlCode.getDialogueRepresentation().isPresent()) {
                        dialogueBuilder.append(controlCode.getDialogueRepresentation().get());
                    }

                    if (ccNode.getVertices().isEmpty()) {
                        break;
                    }

                    ccNode = ccNode.getVertices().get(0);
                    controlCode = ccNode.getValue().getControlCode();
                }

                String dialogue = dialogueBuilder.toString().trim().replaceAll("\n\\s*\n", "\n");
                if (!StringUtils.isBlank(dialogue)) {
                    nextNode = new GraphNode<>(dialogue);
                    previousNode.addVertex(nextNode);

                    for (GraphNode<ControlCodeUsage> nextCcNode : ccNode.getVertices()) {
                        recursionStack.push(new RecursionStackEntry(nextNode, nextCcNode));
                    }

                    alreadyVisitedNodes.put(recursionStackEntry.getSecond(), nextNode);
                } else {
                    for (GraphNode<ControlCodeUsage> nextCcNode : ccNode.getVertices()) {
                        recursionStack.push(new RecursionStackEntry(previousNode, nextCcNode));
                    }

                    alreadyVisitedNodes.put(recursionStackEntry.getSecond(), previousNode);
                }
            }
        }

        return root;
    }
}
