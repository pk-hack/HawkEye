package hawkeye.game.mother2.games;

import com.google.common.collect.ImmutableSet;
import hawkeye.config.model.ROMType;
import hawkeye.game.mother2.data.doors.Mother2GbaItemMap;
import hawkeye.graph.model.GraphNode;
import hawkeye.parse.exceptions.InvalidTextException;
import hawkeye.parse.model.ControlCodeUsage;
import hawkeye.parse.util.CodeTreeParser;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.GbaUtil;
import hawkeye.rom.util.ROM;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Mother2GbaGame extends Mother2Game {
    private static final int POINTER_LENGTH = 4;

    private static final int NPC_TABLE_OFFSET = 0x8EB14;
    private static final int NPC_TABLE_ENTRY_SIZE = 20;
    private static final int NPC_TABLE_TEXT_POINTER_1_OFFSET = 12;
    private static final int NPC_TABLE_TEXT_POINTER_2_OFFSET = 16;

    private static final Set<Integer> NPC_ENTRIES_WITH_NEGATIVE_3_OFFSET = ImmutableSet.of(
            0xB9, 0xBF, 0xC0, 0x164, 0x169,
            0x16D, 0x16F, 0x171, 0x1B0, 0x1B1,
            0x1B3, 0x1C1, 0x233, 0x239, 0x247,
            0x286, 0x2AD, 0x2AE, 0x2AF, 0x2E7,
            0x30A, 0x318, 0x3E1, 0x458, 0x45D,
            0x48C, 0x48D, 0x514, 0x515, 0x516,
            0x57D
    );

    private static final int ITEM_TABLE_OFFSET = 0xB1D62C;
    private static final int ITEM_TABLE_ENTRY_SIZE = 20;
    private static final int ITEM_TABLE_POINTER_OFFSET = 16;
    private static final int ACTION_TABLE_OFFSET = 0xB204E4;
    private static final int ACTION_TABLE_ENTRY_SIZE = 12;
    private static final int ACTION_TABLE_TEXT_POINTER_OFFSET = 4;
    private static final int PHONECALL_TABLE_OFFSET = 0xB1B3B8;
    private static final int PHONECALL_TABLE_ENTRY_SIZE = 8;
    private static final int PHONECALL_TABLE_TEXT_POINTER_OFFSET = 4;

    private final CodeTreeParser codeTreeParser;
    private final Mother2GbaItemMap mother2GbaItemMap;

    public Mother2GbaGame(ROMType romType, ROM rom, CodeTreeParser codeTreeParser, Mother2GbaItemMap mother2GbaItemMap) {
        super(romType, rom);
        this.codeTreeParser = codeTreeParser;
        this.mother2GbaItemMap = mother2GbaItemMap;
    }

    @Override
    public Optional<GraphNode<ControlCodeUsage>> parseToGraph(Mother2ScriptIndexEntry scriptIndexEntry) throws InvalidTextException, ROMAccessException {
        int pointerOffset;
        if (Mother2ScriptIndexEntry.IndexEntryType.NPC_1.equals(scriptIndexEntry.getType())) {
            pointerOffset = NPC_TABLE_OFFSET
                    + NPC_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                    + NPC_TABLE_TEXT_POINTER_1_OFFSET;

            if (NPC_ENTRIES_WITH_NEGATIVE_3_OFFSET.contains(scriptIndexEntry.getEntryIdAsInt())) {
                pointerOffset -= 3;
            }
        } else if (Mother2ScriptIndexEntry.IndexEntryType.NPC_2.equals(scriptIndexEntry.getType())) {
            int type = rom.read(NPC_TABLE_OFFSET + NPC_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt());
            if (type == 2) {
                return Optional.empty();
            }
            pointerOffset = NPC_TABLE_OFFSET
                    + NPC_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                    + NPC_TABLE_TEXT_POINTER_2_OFFSET;

            if (NPC_ENTRIES_WITH_NEGATIVE_3_OFFSET.contains(scriptIndexEntry.getEntryIdAsInt())) {
                pointerOffset -= 3;
            }
        } else if (Mother2ScriptIndexEntry.IndexEntryType.ITEM.equals(scriptIndexEntry.getType())) {
            int sfcItemNumber = scriptIndexEntry.getEntryIdAsInt();
            Optional<Integer> gbaItemNumber = mother2GbaItemMap.getGbaItemNumber(sfcItemNumber);

            if (gbaItemNumber.isPresent()) {
                pointerOffset = ITEM_TABLE_OFFSET
                        + ITEM_TABLE_ENTRY_SIZE * gbaItemNumber.get()
                        + ITEM_TABLE_POINTER_OFFSET;
            } else {
                throw new IllegalArgumentException("Unrecognized item number " + sfcItemNumber);
            }
        } else if (Mother2ScriptIndexEntry.IndexEntryType.ACTION.equals(scriptIndexEntry.getType())) {
            pointerOffset = ACTION_TABLE_OFFSET
                    + ACTION_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                    + ACTION_TABLE_TEXT_POINTER_OFFSET;
        } else if (Mother2ScriptIndexEntry.IndexEntryType.PHONECALL.equals(scriptIndexEntry.getType())) {
            pointerOffset = PHONECALL_TABLE_OFFSET
                    + PHONECALL_TABLE_ENTRY_SIZE * scriptIndexEntry.getEntryIdAsInt()
                    + PHONECALL_TABLE_TEXT_POINTER_OFFSET;
        } else {
            throw new IllegalArgumentException("Unrecognized index type " + scriptIndexEntry.getType());
        }

        Optional<Long> pointer = readPointer(pointerOffset);
        if (!pointer.isPresent()) {
            return Optional.empty();
        }

        return Optional.of(codeTreeParser.parseToGraph(this, pointer.get()));
    }

    @Override
    public Optional<GraphNode<ControlCodeUsage>> parseToGraphSingleLine(Mother2ScriptIndexEntry scriptIndexEntry) throws InvalidTextException, ROMAccessException {
        throw new NotImplementedException();
    }

    @Override
    public List<Mother2ScriptIndexEntry> getScriptIndex() {
        return getStaticScriptIndex();
    }

    @Override
    public Optional<Mother2ScriptIndexEntry> getUnusedScriptIndexEntry() {
        throw new NotImplementedException();
    }

    @Override
    public Optional<Long> readPointer(long offset) throws ROMAccessException {
        long gbaPointer = rom.readMultiLittleEndian(offset, POINTER_LENGTH);
        if ((gbaPointer == 0) || (gbaPointer <= 0x8000000)) {
            return Optional.empty();
        }
        return Optional.of(GbaUtil.convertGbaAddressToRegularOffset(gbaPointer));
    }

    @Override
    public Optional<Long> readRelativePointer(long offset, long base) throws ROMAccessException {
        long relativeOffset = rom.readMultiLittleEndian(offset, POINTER_LENGTH);
        if ((relativeOffset & 0x80000000) != 0) {
            relativeOffset ^= 0xffffffffL;
            relativeOffset = -relativeOffset - 1L;
        }
        return Optional.of(base + relativeOffset);
    }
}
