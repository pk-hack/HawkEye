package hawkeye.service.filters;

import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;

import java.util.Iterator;
import java.util.List;

public class RemoveEmptyFilter implements GraphFilter<ScriptIndexEntry> {
    @Override
    public void applyFilter(List<ScriptIndexEntry> index, List<GraphComparison<String>> graphComparisons) {
        Iterator<ScriptIndexEntry> indexIterator = index.iterator();
        Iterator<GraphComparison<String>> graphComparisonIterator = graphComparisons.iterator();
        while (graphComparisonIterator.hasNext()) {
            indexIterator.next();
            GraphComparison<String> graphComparison = graphComparisonIterator.next();
            if (graphComparison.isEmpty()) {
                indexIterator.remove();
                graphComparisonIterator.remove();
            }
        }
    }
}
