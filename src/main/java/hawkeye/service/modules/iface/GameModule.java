package hawkeye.service.modules.iface;

import hawkeye.game.model.Game;
import hawkeye.game.model.ScriptIndexEntry;
import hawkeye.graph.model.GraphNode;
import hawkeye.service.modules.exceptions.ModuleException;

import java.io.File;
import java.util.List;
import java.util.Optional;

public interface GameModule<TGame extends Game, TScriptIndexEntry extends ScriptIndexEntry> {
    TGame getGame(File file) throws ModuleException;
    List<TScriptIndexEntry> getIndex(TGame game) throws ModuleException;
    Optional<TScriptIndexEntry> getUnusedScriptIndexEntry(TGame game);
    GraphNode<String> parse(TGame game, TScriptIndexEntry indexEntry) throws ModuleException;
    GraphNode<String> parseSingleLine(TGame game, TScriptIndexEntry indexEntry) throws ModuleException;
}
