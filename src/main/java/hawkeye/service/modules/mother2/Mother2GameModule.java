package hawkeye.service.modules.mother2;

import com.google.inject.Inject;
import hawkeye.game.mother2.games.Mother2Game;
import hawkeye.game.mother2.games.Mother2ScriptIndexEntry;
import hawkeye.game.mother2.util.Mother2GameFactory;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.parse.util.TextTreeParser;
import hawkeye.rom.util.FileROM;
import hawkeye.rom.util.ROM;
import hawkeye.service.modules.exceptions.ModuleException;
import hawkeye.service.modules.iface.GameModule;
import lombok.AllArgsConstructor;

import java.io.File;
import java.util.List;
import java.util.Optional;

@AllArgsConstructor(onConstructor = @_(@Inject))
public class Mother2GameModule implements GameModule<Mother2Game, Mother2ScriptIndexEntry> {
    private Mother2GameFactory mother2GameFactory;
    private TextTreeParser textTreeParser;

    @Override
    public Mother2Game getGame(File file) throws ModuleException {
        try {
            ROM rom = new FileROM(file);
            return mother2GameFactory.createFromRom(rom);
        } catch (Exception e) {
            throw new ModuleException("Could not create game", e);
        }
    }

    @Override
    public List<Mother2ScriptIndexEntry> getIndex(Mother2Game game) throws ModuleException {
        try {
            return game.getScriptIndex();
        } catch (Exception e) {
            throw new ModuleException("Could not create script index", e);
        }
    }

    @Override
    public Optional<Mother2ScriptIndexEntry> getUnusedScriptIndexEntry(Mother2Game game) {
        return game.getUnusedScriptIndexEntry();
    }

    @Override
    public GraphNode<String> parse(Mother2Game game, Mother2ScriptIndexEntry scriptIndexEntry) throws ModuleException {
        System.out.println("Generating CC graph");
        Optional<GraphNode<ControlCodeUsage>> ccGraph;
        try {
            ccGraph = game.parseToGraph(scriptIndexEntry);
        } catch (Exception e) {
            throw new ModuleException("Could not parse script", e);
        }

        System.out.println("Generating text graph");
        if (ccGraph.isPresent()) {
            return textTreeParser.create(ccGraph.get());
        } else {
            return new GraphNode<>(null);
        }
    }

    @Override
    public GraphNode<String> parseSingleLine(Mother2Game game, Mother2ScriptIndexEntry scriptIndexEntry) throws ModuleException {
        System.out.println("Generating CC graph");
        Optional<GraphNode<ControlCodeUsage>> ccGraph;
        try {
            ccGraph = game.parseToGraphSingleLine(scriptIndexEntry);
        } catch (Exception e) {
            throw new ModuleException("Could not parse script", e);
        }

        System.out.println("Generating text graph");
        if (ccGraph.isPresent()) {
            return textTreeParser.create(ccGraph.get());
        } else {
            return new GraphNode<>(null);
        }
    }
}
