package hawkeye.service.filters;

import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;

import java.util.List;

public interface GraphFilter<TScriptIndexEntry extends ScriptIndexEntry> {
    void applyFilter(List<TScriptIndexEntry> index, List<GraphComparison<String>> graphComparisons);
}
