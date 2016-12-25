package hawkeye.game.mother2.games;

import hawkeye.config.model.ROMType;
import hawkeye.game.mother2.data.doors.DoorDAO;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.exceptions.InvalidTextException;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.parse.util.CodeTreeParser;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ROM;
import hawkeye.rom.util.SnesUtil;

import java.util.List;
import java.util.Optional;

public class Mother2SnesGame extends Mother2Game {
    private static final String NPC_TABLE_OFFSET_KEY = "npc table";
    private static final String ITEM_TABLE_OFFSET_KEY = "item table";
    private static final String ITEM_TABLE_ENTRY_SIZE_KEY = "item table entry size";
    private static final String ITEM_TABLE_HELP_POINTER_OFFSET_KEY = "item table help pointer offset";
    private static final String ACTION_TABLE_OFFSET_KEY = "action table";
    private static final String ENEMY_TABLE_OFFSET_KEY = "enemy table";
    private static final String ENEMY_TABLE_ENTRY_SIZE_KEY = "enemy table entry size";
    private static final String ENEMY_TABLE_INTRO_POINTER_OFFSET_KEY = "enemy table intro pointer offset";
    private static final String ENEMY_TABLE_DEATH_POINTER_OFFSET_KEY = "enemy table death pointer offset";
    private static final String PSI_ABILITY_TABLE_OFFSET_KEY = "psi ability table";
    private static final String PHONECALL_TABLE_OFFSET_KEY = "phonecall table";
    private static final String PHONECALL_TABLE_ENTRY_SIZE_KEY = "phonecall table entry size";
    private static final String PHONECALL_TABLE_TEXT_POINTER_OFFSET_KEY = "phonecall table text pointer offset";
    private static final String DELIVERY_TABLE_OFFSET_KEY = "delivery table";
    private static final String TEXT_BANK_1_START_OFFSET_KEY = "text bank 1 start";
    private static final String TEXT_BANK_1_END_OFFSET_KEY = "text bank 1 end";
    private static final String TEXT_BANK_2_START_OFFSET_KEY = "text bank 2 start";
    private static final String TEXT_BANK_2_END_OFFSET_KEY = "text bank 2 end";

    private static final int NPC_TABLE_ENTRY_SIZE = 17;
    private static final int NPC_TABLE_TEXT_POINTER_1_OFFSET = 9;
    private static final int NPC_TABLE_TEXT_POINTER_2_OFFSET = 13;

    private static final int ACTION_TABLE_ENTRY_SIZE = 12;
    private static final int ACTION_TABLE_TEXT_POINTER_OFFSET = 4;

    private static final int PSI_ABILITY_TABLE_ENTRY_SIZE = 15;
    private static final int PSI_ABILITY_TABLE_DESCRIPTION_POINTER_OFFSET = 11;

    private static final int DELIVERY_TABLE_ENTRY_SIZE = 20;
    private static final int DELIVERY_TABLE_SUCCESS_POINTER_OFFSET = 10;
    private static final int DELIVERY_TABLE_FAILURE_POINTER_OFFSET = 13;

    private static final int POINTER_LENGTH = 3;

    private final CodeTreeParser codeTreeParser;
    private final DoorDAO doorDAO;

    public Mother2SnesGame(ROMType romType, ROM rom, CodeTreeParser codeTreeParser) {
        super(romType, rom);
        this.codeTreeParser = codeTreeParser;
        this.doorDAO = new DoorDAO(this);
    }

    @Override
    public List<Mother2ScriptIndexEntry> getScriptIndex() throws ROMAccessException {
        List<Mother2ScriptIndexEntry> list = getStaticScriptIndex();
        list.addAll(doorDAO.getDoorScriptIndexEntries());
        return list;
    }

    @Override
    public Optional<Mother2ScriptIndexEntry> getUnusedScriptIndexEntry() {
        final long textBank1OffsetStart = romType.getOffsets().get(TEXT_BANK_1_START_OFFSET_KEY);
        final long textBank1OffsetEnd = romType.getOffsets().get(TEXT_BANK_1_END_OFFSET_KEY);

        for (long offset = textBank1OffsetStart; offset <= textBank1OffsetEnd; ++offset) {
            if (!rom.hasBeenRead(offset)) {
                Mother2ScriptIndexEntry indexEntry = new Mother2ScriptIndexEntry(
                        Mother2ScriptIndexEntry.IndexEntryType.OTHER, offset);
                return Optional.of(indexEntry);
            }
        }

        final long textBank2OffsetStart = romType.getOffsets().get(TEXT_BANK_2_START_OFFSET_KEY);
        if (textBank2OffsetStart >= 0) {
            final long textBank2OffsetEnd = romType.getOffsets().get(TEXT_BANK_2_END_OFFSET_KEY);

            for (long offset = textBank2OffsetStart; offset <= textBank2OffsetEnd; ++offset) {
                if (!rom.hasBeenRead(offset)) {
                    Mother2ScriptIndexEntry indexEntry = new Mother2ScriptIndexEntry(
                            Mother2ScriptIndexEntry.IndexEntryType.OTHER, offset);
                    return Optional.of(indexEntry);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public Optional<GraphNode<ControlCodeUsage>> parseToGraph(Mother2ScriptIndexEntry scriptIndexEntry)
            throws ROMAccessException, InvalidTextException {
        return parseToGraph(scriptIndexEntry, false);
    }

    @Override
    public Optional<GraphNode<ControlCodeUsage>> parseToGraphSingleLine(Mother2ScriptIndexEntry scriptIndexEntry)
            throws InvalidTextException, ROMAccessException {
        return parseToGraph(scriptIndexEntry, true);
    }

    private Optional<GraphNode<ControlCodeUsage>> parseToGraph(Mother2ScriptIndexEntry scriptIndexEntry, boolean isSingleLine)
            throws ROMAccessException, InvalidTextException {
        long offset;
        if (Mother2ScriptIndexEntry.IndexEntryType.OTHER.equals(scriptIndexEntry.getType())) {
            offset = scriptIndexEntry.getEntryIdAsLong();
        } else {
            long pointerOffset;
            if (Mother2ScriptIndexEntry.IndexEntryType.NPC_1.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(NPC_TABLE_OFFSET_KEY)
                        + NPC_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + NPC_TABLE_TEXT_POINTER_1_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.NPC_2.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(NPC_TABLE_OFFSET_KEY)
                        + NPC_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + NPC_TABLE_TEXT_POINTER_2_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.ITEM.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(ITEM_TABLE_OFFSET_KEY)
                        + romType.getOffsets().get(ITEM_TABLE_ENTRY_SIZE_KEY) * scriptIndexEntry.getEntryIdAsInt()
                        + romType.getOffsets().get(ITEM_TABLE_HELP_POINTER_OFFSET_KEY);
            } else if (Mother2ScriptIndexEntry.IndexEntryType.ACTION.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(ACTION_TABLE_OFFSET_KEY)
                        + ACTION_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + ACTION_TABLE_TEXT_POINTER_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.ENEMY_INTRO.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(ENEMY_TABLE_OFFSET_KEY)
                        + romType.getOffsets().get(ENEMY_TABLE_ENTRY_SIZE_KEY) * scriptIndexEntry.getEntryIdAsInt()
                        + romType.getOffsets().get(ENEMY_TABLE_INTRO_POINTER_OFFSET_KEY);
            } else if (Mother2ScriptIndexEntry.IndexEntryType.ENEMY_DEATH.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(ENEMY_TABLE_OFFSET_KEY)
                        + romType.getOffsets().get(ENEMY_TABLE_ENTRY_SIZE_KEY) * scriptIndexEntry.getEntryIdAsInt()
                        + romType.getOffsets().get(ENEMY_TABLE_DEATH_POINTER_OFFSET_KEY);
            } else if (Mother2ScriptIndexEntry.IndexEntryType.PSI_DESCRIPTION.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(PSI_ABILITY_TABLE_OFFSET_KEY)
                        + PSI_ABILITY_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + PSI_ABILITY_TABLE_DESCRIPTION_POINTER_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.PHONECALL.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(PHONECALL_TABLE_OFFSET_KEY)
                        + romType.getOffsets().get(PHONECALL_TABLE_ENTRY_SIZE_KEY) * scriptIndexEntry.getEntryIdAsInt()
                        + romType.getOffsets().get(PHONECALL_TABLE_TEXT_POINTER_OFFSET_KEY);
            } else if (Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_SUCCESS.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(DELIVERY_TABLE_OFFSET_KEY)
                        + DELIVERY_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + DELIVERY_TABLE_SUCCESS_POINTER_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.DELIVERY_FAILURE.equals(scriptIndexEntry.getType())) {
                pointerOffset = romType.getOffsets().get(DELIVERY_TABLE_OFFSET_KEY)
                        + DELIVERY_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                        + DELIVERY_TABLE_FAILURE_POINTER_OFFSET;
            } else if (Mother2ScriptIndexEntry.IndexEntryType.DOOR.equals(scriptIndexEntry.getType())) {
                pointerOffset = doorDAO.getTextPointerOffset(scriptIndexEntry);
            } else {
                throw new IllegalArgumentException("Unrecognized index type " + scriptIndexEntry.getType());
            }

            Optional<Long> pointer = readPointer(pointerOffset);
            if (!pointer.isPresent()) {
                return Optional.empty();
            }

            offset = pointer.get();
        }

        if (isSingleLine) {
            return Optional.of(codeTreeParser.parseToGraph(this, offset, 0));
        } else {
            return Optional.of(codeTreeParser.parseToGraph(this, offset));
        }
    }

    @Override
    public Optional<Long> readPointer(long offset) throws ROMAccessException {
        long snesPointer = rom.readMultiLittleEndian(offset, POINTER_LENGTH);
        if ((snesPointer == 0) || (snesPointer <= 0xc00000)) {
            return Optional.empty();
        }
        return Optional.of(SnesUtil.convertSnesAddressToRegularOffset(snesPointer));
    }

    @Override
    public Optional<Long> readRelativePointer(long offset, long base) {
        throw new RuntimeException("Relative pointers not supporrted for M2/EB SNES");
    }
}
