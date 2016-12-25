package hawkeye.service.modules.iface;

import hawkeye.game.model.Game;
import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;
import hawkeye.graph.model.GraphNode;
import hawkeye.service.modules.exceptions.ModuleException;

import java.util.List;

public interface ViewModule <TIndexEntry extends ScriptIndexEntry, TGame extends Game> {
    void viewComparisons(List<TIndexEntry> index, List<TGame> games, List<GraphComparison<String>> graphComparisons)
            throws ModuleException;
    void viewLines(List<List<TIndexEntry>> indexes, List<TGame> games, List<List<GraphNode<String>>> lines) throws ModuleException;
}
