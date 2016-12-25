package hawkeye.service;

import hawkeye.game.model.Game;
import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;
import hawkeye.graph.model.GraphNode;
import hawkeye.graph.util.GraphComparer;
import hawkeye.service.filters.GraphFilter;
import hawkeye.service.modules.exceptions.ModuleException;
import hawkeye.service.modules.iface.GameModule;
import hawkeye.service.modules.iface.ViewModule;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class ComparisonService<TGame extends Game, TScriptIndexEntry extends ScriptIndexEntry> {
    protected GameModule<TGame, TScriptIndexEntry> gameModule;
    protected GraphComparer graphComparer;
    protected List<GraphFilter> graphFilters;
    protected ViewModule<TScriptIndexEntry, TGame> viewModule;

    public void compare(List<String> filenames) throws ModuleException {
        List<File> files = getFiles(filenames);

        List<TGame> games = getGames(files);

        viewComparisons(games);

        viewLines(games);
    }

    private void viewComparisons(List<TGame> games) throws ModuleException {
        List<TScriptIndexEntry> index = gameModule.getIndex(games.get(0));

        List<GraphComparison<String>> graphComparisons = new ArrayList<>(index.size());
        for (TScriptIndexEntry indexEntry : index) {
            System.out.println("Comparing " + indexEntry);
            List<GraphNode<String>> graphs = getGraphs(games, indexEntry);

            GraphComparison<String> graphComparison = graphComparer.compare(graphs);

            graphComparisons.add(graphComparison);
        }

        for (GraphFilter graphFilter : graphFilters) {
            graphFilter.applyFilter(index, graphComparisons);
        }

        viewModule.viewComparisons(index, games, graphComparisons);
    }

    private void viewLines(List<TGame> games) throws ModuleException {
        List<List<TScriptIndexEntry>> allIndexes = new ArrayList<>(games.size());
        List<List<GraphNode<String>>> allLines = new ArrayList<>(games.size());

        for (TGame game : games) {
            List<TScriptIndexEntry> index = new ArrayList<>();
            List<GraphNode<String>> lines = new ArrayList<>();

            while (true) {
                Optional<TScriptIndexEntry> unusedScriptIndexEntry = gameModule.getUnusedScriptIndexEntry(game);
                if (!unusedScriptIndexEntry.isPresent()) {
                    break;
                }
                System.out.println("Comparing " + unusedScriptIndexEntry.get());

                try {
                    GraphNode<String> line = gameModule.parseSingleLine(game, unusedScriptIndexEntry.get());

                    index.add(unusedScriptIndexEntry.get());
                    lines.add(line);
                } catch (ModuleException e) {
                    continue;
                }
            }

            allIndexes.add(index);
            allLines.add(lines);
        }

        viewModule.viewLines(allIndexes, games, allLines);
    }

    private List<File> getFiles(List<String> filenames) {
        return filenames.stream()
                .map(File::new)
                .collect(Collectors.toList());
    }

    private List<TGame> getGames(List<File> files) throws ModuleException {
        List<TGame> games = new ArrayList<>(files.size());
        for (File file : files) {
            games.add(gameModule.getGame(file));
        }
        return games;
    }

    private List<GraphNode<String>> getGraphs(List<TGame> games, TScriptIndexEntry scriptIndexEntry) throws ModuleException {
        List<GraphNode<String>> graphs = new ArrayList<>(games.size());
        for (TGame game : games) {
            graphs.add(gameModule.parse(game, scriptIndexEntry));
        }
        return graphs;
    }
}
