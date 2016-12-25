package hawkeye.service.filters;

import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DedupGraphFilter implements GraphFilter<ScriptIndexEntry> {
    @Override
    public void applyFilter(List<ScriptIndexEntry> index, List<GraphComparison<String>> graphComparisons) {
        Set<List<List<String>>> uniqueValues = new HashSet<>();
        Iterator<ScriptIndexEntry> indexIterator = index.iterator();
        Iterator<GraphComparison<String>> graphComparisonIterator = graphComparisons.iterator();
        while (graphComparisonIterator.hasNext()) {
            indexIterator.next();
            GraphComparison<String> graphComparison = graphComparisonIterator.next();
            if (uniqueValues.contains(graphComparison.getValues())) {
                indexIterator.remove();
                graphComparisonIterator.remove();
            }
            uniqueValues.add(graphComparison.getValues());
        }
    }
}
