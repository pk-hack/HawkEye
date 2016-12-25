package hawkeye.service.filters;

import com.google.inject.Inject;
import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;
import hawkeye.service.util.LineMatchChecker;
import lombok.AllArgsConstructor;

import java.util.Iterator;
import java.util.List;

@AllArgsConstructor(onConstructor = @_(@Inject))
public class DifferencesOnlyFilter implements GraphFilter<ScriptIndexEntry> {
    private LineMatchChecker lineMatchChecker;

    @Override
    public void applyFilter(List<ScriptIndexEntry> index, List<GraphComparison<String>> graphComparisons) {
        Iterator<ScriptIndexEntry> indexIterator = index.iterator();
        Iterator<GraphComparison<String>> graphComparisonIterator = graphComparisons.iterator();
        while (graphComparisonIterator.hasNext()) {
            indexIterator.next();
            GraphComparison<String> graphComparison = graphComparisonIterator.next();
            if (!lineMatchChecker.hasDifferences(graphComparison)) {
                indexIterator.remove();
                graphComparisonIterator.remove();
            }
        }
    }
}
