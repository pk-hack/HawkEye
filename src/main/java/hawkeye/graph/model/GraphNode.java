package hawkeye.graph.model;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GraphNode<T> {
    private final UUID id;
    @Getter private T value;
    @Getter private List<GraphNode<T>> vertices;

    public GraphNode(T value) {
        id = UUID.randomUUID();
        this.value = value;
        vertices = new ArrayList<>();
    }

    public void addVertex(GraphNode<T> node) {
        vertices.add(node);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (this.hashCode() == obj.hashCode());
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
