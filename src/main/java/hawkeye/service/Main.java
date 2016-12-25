package hawkeye.service;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import hawkeye.game.mother2.games.Mother2Game;
import hawkeye.game.mother2.games.Mother2ScriptIndexEntry;
import hawkeye.graph.util.GraphComparer;
import hawkeye.service.filters.DedupGraphFilter;
import hawkeye.service.filters.DifferencesOnlyFilter;
import hawkeye.service.filters.GraphFilter;
import hawkeye.service.filters.RemoveEmptyFilter;
import hawkeye.service.modules.mother2.Mother2GameModule;
import hawkeye.service.modules.mother2.Mother2HtmlFileViewModule;
import hawkeye.view.FreemarkerConfiguration;
import org.apache.commons.cli.*;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        Options options = new Options();
        options.addOption(Option.builder("r")
                .argName("roms")
                .hasArgs()
                .valueSeparator(',')
                .desc("List of ROM files separated by commas")
                .required()
                .build());
        options.addOption(Option.builder("o")
                .argName("outputDir")
                .hasArg()
                .desc("Output directory")
                .required()
                .build());

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);

            String[] roms = line.getOptionValues('r');
            if (roms.length != 2) {
                System.out.println("Currently only running this program with exactly 2 ROM files is supported");
                System.exit(0);
            }
            String outputDir = line.getOptionValue('o');

            generateComparison(roms[0], roms[1], outputDir);

        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp( "GameScriptComparer", options );
        }
    }

    private static void generateComparison(String romFilename1, String romFilename2, String outputPath) throws Exception {
        Injector injector = Guice.createInjector(new Module());

        Mother2GameModule mother2GameModule = injector.getInstance(Mother2GameModule.class);
        GraphComparer graphComparer = injector.getInstance(GraphComparer.class);
        FreemarkerConfiguration freemarkerConfiguration = injector.getInstance(FreemarkerConfiguration.class);

        Mother2HtmlFileViewModule mother2HtmlFileViewModule = new Mother2HtmlFileViewModule(outputPath, freemarkerConfiguration);

        List<GraphFilter> graphFilters = ImmutableList.of(new RemoveEmptyFilter(), new DedupGraphFilter(), injector.getInstance(DifferencesOnlyFilter.class));

        ComparisonService<Mother2Game, Mother2ScriptIndexEntry> comparisonService =
                new ComparisonService(mother2GameModule, graphComparer, graphFilters, mother2HtmlFileViewModule);

        comparisonService.compare(ImmutableList.of(romFilename1, romFilename2));
    }
}
