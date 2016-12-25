package hawkeye.game.mother2.data.doors;

import com.google.common.collect.ImmutableSet;
import hawkeye.game.mother2.games.Mother2ScriptIndexEntry;
import hawkeye.game.mother2.games.Mother2SnesGame;
import hawkeye.rom.exceptions.ROMAccessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class DoorDAO {
    private static final String DOOR_POINTER_TABLE_OFFSET_KEY = "door pointer table";
    private static final int NUM_MAP_AREAS = 1280;
    private static final Set<Integer> DOOR_TYPES_WITH_TEXT = ImmutableSet.of(2, 5, 6);

    private final Mother2SnesGame game;
    private final long POINTER_TABLE_OFFSET;

    public DoorDAO(Mother2SnesGame game) {
        this.game = game;
        this.POINTER_TABLE_OFFSET = game.getRomType().getOffsets().get(DOOR_POINTER_TABLE_OFFSET_KEY);
    }

    public List<Mother2ScriptIndexEntry> getDoorScriptIndexEntries() throws ROMAccessException {
        List<Mother2ScriptIndexEntry> output = new ArrayList<>();
        for (int i = 0; i < NUM_MAP_AREAS; ++i) {
            Optional<Long> pointer = game.readPointer(POINTER_TABLE_OFFSET + i * 4);
            if (!pointer.isPresent()) {
                continue;
            }

            long doorOffset = pointer.get();
            long numDoors;
            try {
                numDoors = game.getRom().readMultiLittleEndian(doorOffset, 2);
            } catch (ROMAccessException e) {
                continue;
            }
            doorOffset += 2;

            for (int j = 0; j < numDoors; ++j) {
                int doorType = game.read(doorOffset + 2);
                if (DOOR_TYPES_WITH_TEXT.contains(doorType)) {
                    output.add(createEntry(i, j));
                }
                doorOffset += 5;
            }
        }

        return output;
    }

    public long getTextPointerOffset(Mother2ScriptIndexEntry scriptIndexEntry) throws ROMAccessException {
        String entryId = scriptIndexEntry.getEntryId();
        String[] entrySplit = entryId.split(":");
        int mapAreaId = Integer.parseInt(entrySplit[0]);
        int index = Integer.parseInt(entrySplit[1]);

        Optional<Long> pointer = game.readPointer(POINTER_TABLE_OFFSET + mapAreaId * 4);
        if (!pointer.isPresent()) {
            throw new RuntimeException("Unexpected empty pointer");
        }

        long doorOffset = pointer.get() + 2 + index * 5;

        return game.getRom().readMultiLittleEndian(doorOffset + 3, 2) | 0xF0000;
    }

    private Mother2ScriptIndexEntry createEntry(int mapAreaId, int index) {
        return new Mother2ScriptIndexEntry(
                Mother2ScriptIndexEntry.IndexEntryType.DOOR,
                String.format("%d:%d", mapAreaId, index));
    }
}
