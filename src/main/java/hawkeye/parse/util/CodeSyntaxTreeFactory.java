package hawkeye.parse.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.TextTable;
import hawkeye.parse.exceptions.InvalidSyntaxTreeException;
import hawkeye.parse.model.TextSyntaxTreeNode;

import java.util.Collection;
import java.util.Optional;

public class CodeSyntaxTreeFactory {
    public TextSyntaxTreeNode createTree(Collection<ControlCode> controlCodes, TextTable textTable)
            throws InvalidSyntaxTreeException {
        TextSyntaxTreeNode root = new TextSyntaxTreeNode();

        for (int i = 0; i < TextTable.NUM_ENTRIES; ++i) {
            Optional<String> entry = textTable.get(i);
            if (entry.isPresent()) {
                ControlCode controlCode = ControlCode.builder()
                        .identifier(ImmutableList.of(i))
                        .description("Prints '" + entry.get() + "'")
                        .length(Optional.of(1))
                        .isTerminal(false)
                        .referenceSettings(Optional.empty())
                        .dialogueRepresentation(Optional.of(entry.get()))
                        .build();
                addControlCodeToSyntaxTree(root, controlCode);
            }
        }

        for (ControlCode controlCode : controlCodes) {
            addControlCodeToSyntaxTree(root, controlCode);
        }

        return root;
    }

    private void addControlCodeToSyntaxTree(TextSyntaxTreeNode node, ControlCode controlCode) throws InvalidSyntaxTreeException {
        int i;
        int key;
        for (i = 0; i < controlCode.getIdentifier().size() - 1; ++i) {
            key = controlCode.getIdentifier().get(i);

            Optional<TextSyntaxTreeNode> child = node.getChild(key);
            if (!child.isPresent()) {
                TextSyntaxTreeNode nextNode = new TextSyntaxTreeNode();
                node.addChild(key, nextNode);
                node = nextNode;
            } else {
                node = child.get();
            }
        }

        key = controlCode.getIdentifier().get(i);
        Optional<TextSyntaxTreeNode> child = node.getChild(key);
        if (!child.isPresent()) {
            node.addChild(key, new TextSyntaxTreeNode(controlCode));
        } else if (!child.get().getControlCode().isPresent()) {
            child.get().setControlCode(controlCode);
        } else {
            throw new InvalidSyntaxTreeException(
                    "More than one control code exists with identifier " + controlCode.getIdentifier());
        }
    }
}
