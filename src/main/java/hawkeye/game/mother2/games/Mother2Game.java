package hawkeye.game.mother2.games;

import com.google.common.collect.ImmutableSet;
import hawkeye.config.model.ROMType;
import hawkeye.game.model.Game;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.exceptions.InvalidTextException;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ROM;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public abstract class Mother2Game extends Game<Mother2ScriptIndexEntry> {
    private static final int NUM_ITEMS = 253;
    private static final int NUM_NPCS = 1584;
    private static final int NUM_ACTIONS = 318;
    private static final int NUM_ENEMIES = 231;
    private static final int NUM_PSI_ABILITIES = 53;
    private static final int NUM_PHONECALLS = 5;
    private static final int NUM_DELIVERIES = 10;

    private static final Set<Integer> NPCS_TO_SKIP = ImmutableSet.of(
            // Shops
            9, 40, 291, 292, 295, 296, 311, 316, 318, 338, 403, 404, 504, 505, 509, 536, 563, 606, 619, 708, 713, 714,
            715, 747, 749, 769, 775, 853, 922, 923, 925, 926, 932, 933, 934, 936, 959, 1008, 1011, 1025, 1061, 1098,
            1123, 1125, 1139, 1140, 1146, 1147, 1156, 1157, 1232, 1282, 1312, 1374
    );

    public Mother2Game(ROMType romType, ROM rom) {
        super(romType, rom);
    }

    public abstract Optional<GraphNode<ControlCodeUsage>> parseToGraph(Mother2ScriptIndexEntry scriptIndexEntry)
            throws InvalidTextException, ROMAccessException;

    public abstract Optional<GraphNode<ControlCodeUsage>> parseToGraphSingleLine(Mother2ScriptIndexEntry scriptIndexEntry)
            throws InvalidTextException, ROMAccessException;

    protected static List<Mother2ScriptIndexEntry> getStaticScriptIndex() {
        List<Mother2ScriptIndexEntry> scriptIndex = new ArrayList<>();
        for (int i = 0; i < NUM_ITEMS; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.ITEM, i));
        }
        for (int i = 0; i < NUM_NPCS; ++i) {
            if (NPCS_TO_SKIP.contains(i)) {
                continue;
            }
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.NPC_1, i));
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.NPC_2, i));
        }
        for (int i = 0; i < NUM_ACTIONS; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.ACTION, i));
        }
        for (int i = 0; i < NUM_ENEMIES; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.ENEMY_INTRO, i));
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.ENEMY_DEATH, i));
        }
        for (int i = 0; i < NUM_PSI_ABILITIES; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.PSI_DESCRIPTION, i));
        }
        for (int i = 0; i < NUM_PHONECALLS; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.PHONECALL, i));
        }
        for (int i = 0; i < NUM_DELIVERIES; ++i) {
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_SUCCESS, i));
            scriptIndex.add(new Mother2ScriptIndexEntry(Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_FAILURE, i));
        }
        return scriptIndex;
    }

    public abstract List<Mother2ScriptIndexEntry> getScriptIndex() throws ROMAccessException;

    public abstract Optional<Mother2ScriptIndexEntry> getUnusedScriptIndexEntry();

    @Override
    public int read(long offset) throws ROMAccessException {
        return rom.read(offset);
    }

    @Override
    public int[] readArray(long offset, int length) throws ROMAccessException {
        return rom.readArray(offset, length);
    }
}
