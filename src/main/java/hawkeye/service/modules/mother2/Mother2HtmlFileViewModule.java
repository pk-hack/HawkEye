package hawkeye.service.modules.mother2;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import hawkeye.game.mother2.games.Mother2Game;
import hawkeye.game.mother2.games.Mother2ScriptIndexEntry;
import hawkeye.graph.model.GraphComparison;
import hawkeye.graph.model.GraphNode;
import hawkeye.graph.util.GraphUtils;
import hawkeye.service.modules.exceptions.ModuleException;
import hawkeye.service.modules.iface.ViewModule;
import hawkeye.service.util.LineMatchChecker;
import hawkeye.view.FreemarkerConfiguration;
import lombok.Value;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Mother2HtmlFileViewModule implements ViewModule<Mother2ScriptIndexEntry, Mother2Game> {
    private static final String STATIC_RESOURCES_PATH = "src/main/resources/templates/mother2/static";
    private static final String INDEX_TEMPLATE_PATH = "mother2/index.ftl";
    private static final String LINES_TEMPLATE_PATH = "mother2/lines.ftl";

    private static final Map<String, Set<Mother2ScriptIndexEntry.IndexEntryType>> COMPARISON_PAGES = ImmutableMap
            .<String, Set<Mother2ScriptIndexEntry.IndexEntryType>>builder()
            .put("npcs.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.NPC_1, Mother2ScriptIndexEntry.IndexEntryType.NPC_2))
            .put("index.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.ITEM))
            .put("actions.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.ACTION))
            .put("enemies.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.ENEMY_INTRO, Mother2ScriptIndexEntry.IndexEntryType.ENEMY_DEATH))
            .put("psi.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.PSI_DESCRIPTION))
            .put("phone.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.PHONECALL))
            .put("deliveries.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_SUCCESS, Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_FAILURE))
            .put("doors.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.DOOR))
            .build();

    private static final Map<String, Set<Mother2ScriptIndexEntry.IndexEntryType>> LINES_PAGES = ImmutableMap
            .<String, Set<Mother2ScriptIndexEntry.IndexEntryType>>builder()
            .put("other.html", ImmutableSet.of(Mother2ScriptIndexEntry.IndexEntryType.OTHER))
            .build();

    @Value
    private static class LineViewModel {
        private Mother2ScriptIndexEntry indexEntry;
        private List<String> text;

        public Map<String, Object> toViewModelMap() {
            return ImmutableMap.of("id", Long.toHexString(indexEntry.getEntryIdAsLong()), "text", text);
        }
    }


    private final String outputDirectory;
    private Template indexTemplate;
    private Template linesTemplate;
    private LineMatchChecker lineMatchChecker;

    public Mother2HtmlFileViewModule(String outputDirectory, FreemarkerConfiguration freemarkerConfiguration) throws Exception {
        this.outputDirectory = outputDirectory;

        indexTemplate = freemarkerConfiguration.getConfiguration().getTemplate(INDEX_TEMPLATE_PATH);
        linesTemplate = freemarkerConfiguration.getConfiguration().getTemplate(LINES_TEMPLATE_PATH);
        // TODO use dependency injection for this
        this.lineMatchChecker = new LineMatchChecker();
    }

    @Override
    public void viewComparisons(List<Mother2ScriptIndexEntry> index, List<Mother2Game> games, List<GraphComparison<String>> graphComparisons)
            throws ModuleException {

        copyStaticResources();

        for (Map.Entry<String, Set<Mother2ScriptIndexEntry.IndexEntryType>> page : COMPARISON_PAGES.entrySet()) {
            String pageFilename = page.getKey();
            Set<Mother2ScriptIndexEntry.IndexEntryType> types = page.getValue();

            renderComparisonPage(
                    pageFilename,
                    games,
                    types,
                    index,
                    graphComparisons);
        }
    }

    @Override
    public void viewLines(List<List<Mother2ScriptIndexEntry>> indexes, List<Mother2Game> games, List<List<GraphNode<String>>> lines)
        throws ModuleException {
        for (Map.Entry<String, Set<Mother2ScriptIndexEntry.IndexEntryType>> page : LINES_PAGES.entrySet()) {
            String pageFilename = page.getKey();
            Set<Mother2ScriptIndexEntry.IndexEntryType> types = page.getValue();

            renderLinesPage(
                    pageFilename,
                    games,
                    types,
                    indexes,
                    lines);
        }
    }

    private void copyStaticResources() throws ModuleException {
        try {
            FileUtils.copyDirectory(new File(STATIC_RESOURCES_PATH), new File(outputDirectory, "static"));
        } catch (IOException e) {
            throw new ModuleException(
                    "Unable to copy static resource files from " + STATIC_RESOURCES_PATH + " to " + outputDirectory,
                    e);
        }
    }

    private void renderComparisonPage(
            String pageFilename,
            List<Mother2Game> games,
            Set<Mother2ScriptIndexEntry.IndexEntryType> types,
            List<Mother2ScriptIndexEntry> index,
            List<GraphComparison<String>> graphComparisons)
            throws ModuleException {
        List<Map<String, Object>> viewModelIndex = new ArrayList<>();

        Iterator<Mother2ScriptIndexEntry> indexIterator = index.iterator();
        Iterator<GraphComparison<String>> graphComparisonIterator = graphComparisons.iterator();
        while (indexIterator.hasNext()) {
            Mother2ScriptIndexEntry indexEntry = indexIterator.next();
            GraphComparison<String> graphComparison = graphComparisonIterator.next();

            if (!types.contains(indexEntry.getType())) {
                continue;
            }

            Map<String, Object> viewModelIndexEntry = new HashMap<>();
            viewModelIndexEntry.put("type", indexEntry.getType().getName());
            viewModelIndexEntry.put("id", indexEntry.getEntryId());

            addComparisonToViewModel(viewModelIndexEntry, graphComparison);

            viewModelIndex.add(viewModelIndexEntry);
        }

        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("lastUpdated", new Date());
        viewModel.put("index", viewModelIndex);
        viewModel.put("games", games);

        renderTemplate(indexTemplate, viewModel, outputDirectory + "/" + pageFilename);
    }

    private void addComparisonToViewModel(
            Map<String, Object> viewModel,
            GraphComparison<String> graphComparison) {
        if (graphComparison.isEmpty()) {
            return;
        }

        viewModel.put("isIsomorphic", graphComparison.isIsomorphicComparison());

        if (graphComparison.isIsomorphicComparison()) {
            List<List<String>> lines = invert2dArrayAndRemoveFirstRow(graphComparison.getValues());

            List<Map.Entry<List<String>, Set<Integer>>> sortedLinesTable = createSortedLinesTable(lines);

            List<Map<String, Object>> sortedLinesChangedTable = sortedLinesTable.stream()
                    .map(x -> ImmutableMap.of(
                            "lineNumbers", x.getValue(),
                            "lines", x.getKey(),
                            "isLinesSame", lineMatchChecker.areLinesSame(x.getKey())))
                    .collect(Collectors.toList());

            viewModel.put("linesTable", sortedLinesChangedTable);
        } else {
            List<List<String>> gamesLines = new ArrayList<>(graphComparison.getValues().size());
            for (List<String> gameLines : graphComparison.getValues()) {
                gameLines.remove(0);
                gamesLines.add(gameLines);
            }

            List<List<Map.Entry<String, Set<Integer>>>> sortedGameLinesTables = new ArrayList<>();
            for (List<String> gameLines : gamesLines) {
                List<Map.Entry<String, Set<Integer>>> sortedGameLinesTable = createSortedLinesTable(gameLines);
                sortedGameLinesTables.add(sortedGameLinesTable);
            }

            viewModel.put("linesTables", sortedGameLinesTables);
        }
    }

    private <T> List<Map.Entry<T, Set<Integer>>> createSortedLinesTable(List<T> lines) {
        Map<T, Set<Integer>> linesTable = new HashMap<>();

        int lineIndex = 1;
        for (T line : lines) {
            Set<Integer> lineIndexes = linesTable.get(line);
            if (lineIndexes == null) {
                lineIndexes = new HashSet<>();
                lineIndexes.add(lineIndex);
                linesTable.put(line, lineIndexes);
            } else {
                lineIndexes.add(lineIndex);
            }
            ++lineIndex;
        }

        List<Map.Entry<T, Set<Integer>>> sortedLinesTable = new ArrayList<>();
        sortedLinesTable.addAll(linesTable.entrySet());
        sortedLinesTable.sort((a, b) -> Collections.min(a.getValue()) - Collections.min(b.getValue()));

        return sortedLinesTable;
    }

    private void renderLinesPage(
            String pageFilename, List<Mother2Game> games, Set<Mother2ScriptIndexEntry.IndexEntryType> types,
            List<List<Mother2ScriptIndexEntry>> indexes, List<List<GraphNode<String>>> lines) throws ModuleException {
        List<List<Map>> linesViewModel = getLinesViewModel(types, indexes, lines);

        Map<String, Object> viewModel = new HashMap<>();
        viewModel.put("games", games);
        viewModel.put("lines", linesViewModel);
        viewModel.put("lastUpdated", new Date());

        renderTemplate(linesTemplate, viewModel, outputDirectory + "/" + pageFilename);
    }

    private List<List<Map>> getLinesViewModel(
            Set<Mother2ScriptIndexEntry.IndexEntryType> types,
            List<List<Mother2ScriptIndexEntry>> indexes, List<List<GraphNode<String>>> lines) {
        List<List<Map>> linesPageData = new ArrayList<>();

        Iterator<List<Mother2ScriptIndexEntry>> indexesIterator = indexes.iterator();
        Iterator<List<GraphNode<String>>> linesIterator = lines.iterator();

        while (indexesIterator.hasNext()) {
            List<Mother2ScriptIndexEntry> index = indexesIterator.next();
            List<GraphNode<String>> gameLines = linesIterator.next();

            Iterator<Mother2ScriptIndexEntry> indexIterator = index.iterator();
            Iterator<GraphNode<String>> gameLinesIterator = gameLines.iterator();

            List<Map> lineViewModels = new ArrayList<>();

            while (indexIterator.hasNext()) {
                Mother2ScriptIndexEntry indexEntry = indexIterator.next();
                GraphNode<String> gameLine = gameLinesIterator.next();

                if (!types.contains(indexEntry.getType())) {
                    continue;
                }

                List<String> gameLineValues = GraphUtils.getAllValues(gameLine);
                if (gameLineValues.isEmpty() || ((gameLineValues.size() == 1) && (gameLineValues.get(0) == null))) {
                    continue;
                }
                gameLineValues.remove(0);

                lineViewModels.add(new LineViewModel(indexEntry, gameLineValues).toViewModelMap());
            }

            linesPageData.add(lineViewModels);
        }

        return linesPageData;
    }

    private void renderTemplate(Template template, Object viewModel, String filename) throws ModuleException {
        OutputStream outputStream;
        try {
            outputStream = new FileOutputStream(filename);
        } catch (FileNotFoundException e) {
            throw new ModuleException("Could not open file: " + filename, e);
        }

        Writer out = new OutputStreamWriter(outputStream);
        try {
            template.process(viewModel, out);
        } catch (IOException | TemplateException e) {
            throw new ModuleException("Could not write template", e);
        }
    }

    private <T> List<List<T>> invert2dArrayAndRemoveFirstRow(List<List<T>> list) {
        if (list.isEmpty()) {
            return list;
        }

        int numRows = list.get(0).size() - 1;
        int numColumns = list.size();

        List<List<T>> rows = new ArrayList<>(numRows);
        for (int i = 1; i < numRows+1; ++i) {
            List<T> row = new ArrayList<>(numColumns);
            for (List<T> aList : list) {
                row.add(aList.get(i));
            }
            rows.add(row);
        }

        return rows;
    }
}
