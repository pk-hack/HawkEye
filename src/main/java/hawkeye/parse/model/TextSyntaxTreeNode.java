package hawkeye.parse.model;

import hawkeye.config.model.ControlCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TextSyntaxTreeNode {
    private final Map<Integer, TextSyntaxTreeNode> children = new HashMap<>();
    @Getter
    private Optional<ControlCode> controlCode;

    public TextSyntaxTreeNode() {
        controlCode = Optional.empty();
    }

    public TextSyntaxTreeNode(ControlCode controlCode) {
        this.controlCode = Optional.of(controlCode);
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }

    public Optional<TextSyntaxTreeNode> getChild(int key) {
        return Optional.ofNullable(children.get(key));
    }

    public void addChild(int key, TextSyntaxTreeNode value) {
        children.put(key, value);
    }

    public void setControlCode(ControlCode controlCode) {
        if (this.controlCode.isPresent()) {
            throw new RuntimeException("Cannot reassign control code to a TextSyntaxTreeNode");
        }
        this.controlCode = Optional.of(controlCode);
    }
}