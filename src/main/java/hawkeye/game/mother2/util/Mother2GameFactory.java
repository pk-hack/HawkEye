package hawkeye.game.mother2.util;

import com.google.inject.Inject;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.ROMType;
import hawkeye.config.model.TextTable;
import hawkeye.config.util.ControlCodeDeserializationFactory;
import hawkeye.config.util.ROMTypeDeserializationFactory;
import hawkeye.config.util.TextTableFactory;
import hawkeye.game.exceptions.UnrecognizedROMException;
import hawkeye.game.mother2.data.doors.Mother2GbaItemMap;
import hawkeye.game.mother2.games.Mother2Game;
import hawkeye.game.mother2.games.Mother2GbaGame;
import hawkeye.game.mother2.games.Mother2SnesGame;
import hawkeye.parse.exceptions.InvalidSyntaxTreeException;
import hawkeye.parse.model.TextSyntaxTreeNode;
import hawkeye.parse.util.CodeSyntaxTreeFactory;
import hawkeye.parse.util.CodeTreeParser;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ROM;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Mother2GameFactory {
    private static final String ROMTYPES_FILENAME = "config/romtypes.json";
    private static final String CHARACTER_TABLE_FILENAME_KEY = "character table";
    private static final String CODELIST_FILENAME_KEY = "codelist";

    private TextTableFactory textTableFactory;
    private ControlCodeDeserializationFactory controlCodeDeserializationFactory;
    private CodeSyntaxTreeFactory codeSyntaxTreeFactory;
    private EBTextCompressionCodesReader ebTextCompressionCodesReader;
    private Mother2GbaItemMap mother2GbaItemMap;

    private Collection<ROMType> romTypes;
    private final Map<String, CodeTreeParser> romTypeTextParsers = new HashMap<>();

    @Inject
    public Mother2GameFactory(
            ROMTypeDeserializationFactory romTypeDeserializationFactory,
            TextTableFactory textTableFactory,
            ControlCodeDeserializationFactory controlCodeDeserializationFactory,
            CodeSyntaxTreeFactory codeSyntaxTreeFactory,
            EBTextCompressionCodesReader ebTextCompressionCodesReader,
            Mother2GbaItemMap mother2GbaItemMap) throws Exception {
        romTypes = romTypeDeserializationFactory.createCollectionFromFile(ROMTYPES_FILENAME);
        this.textTableFactory = textTableFactory;
        this.controlCodeDeserializationFactory = controlCodeDeserializationFactory;
        this.codeSyntaxTreeFactory = codeSyntaxTreeFactory;
        this.ebTextCompressionCodesReader = ebTextCompressionCodesReader;
        this.mother2GbaItemMap = mother2GbaItemMap;
    }

    public Mother2Game createFromRom(ROM rom)
            throws UnrecognizedROMException, InvalidSyntaxTreeException, IOException, ROMAccessException {
        ROMType romType = null;
        for (ROMType tmpRomType : romTypes) {
            if (rom.getMd5sum().equals(tmpRomType.getMd5sum())) {
                romType = tmpRomType;
                break;
            }
        }
        if (romType == null) {
            throw new UnrecognizedROMException("Could not recognize ROM with md5 " + rom.getMd5sum());
        }

        CodeTreeParser codeTreeParser = getParserForRomType(rom, romType);

        String romTypeName = romType.getName();
        if ("EarthBound".equals(romTypeName) || "MOTHER 2".equals(romTypeName)) {
            return new Mother2SnesGame(romType, rom, codeTreeParser);
        } else if ("MOTHER 1+2".equals(romTypeName)) {
            return new Mother2GbaGame(romType, rom, codeTreeParser, mother2GbaItemMap);
        } else {
            throw new UnrecognizedROMException("Could not initialize game whose ROM has name: " + romTypeName);
        }
    }

    private CodeTreeParser getParserForRomType(ROM rom, ROMType romType) throws FileNotFoundException, UnsupportedEncodingException, InvalidSyntaxTreeException, ROMAccessException {
        CodeTreeParser cachedParser = romTypeTextParsers.get(romType.getMd5sum());
        if (cachedParser != null) {
            return cachedParser;
        }

        TextTable textTable = textTableFactory.createFromFile(
                "config/" + romType.getFiles().get(CHARACTER_TABLE_FILENAME_KEY));
        Collection<ControlCode> controlCodes = controlCodeDeserializationFactory.createCollectionFromFile(
                "config/" + romType.getFiles().get(CODELIST_FILENAME_KEY));
        if ("EarthBound".equals(romType.getName())) {
            Collection<ControlCode> textCompressionControlCodes = ebTextCompressionCodesReader.
                    getTextCompressionControlCodes(rom, textTable);
            controlCodes.addAll(textCompressionControlCodes);
        }

        TextSyntaxTreeNode syntaxTree = codeSyntaxTreeFactory.createTree(controlCodes, textTable);
        CodeTreeParser newParser = new CodeTreeParser(syntaxTree);
        romTypeTextParsers.put(romType.getMd5sum(), newParser);
        return newParser;
    }
}
