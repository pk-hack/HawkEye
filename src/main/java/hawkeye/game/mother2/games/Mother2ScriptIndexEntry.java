package hawkeye.game.mother2.games;

import hawkeye.game.model.ScriptIndexEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

@Value
@AllArgsConstructor
public class Mother2ScriptIndexEntry extends ScriptIndexEntry {
    public enum IndexEntryType {
        NPC_1("NPC"),
        NPC_2("NPC (Secondary Text)"),
        ITEM("Item"),
        ACTION("Action"),
        ENEMY_INTRO("Enemy Intro"),
        ENEMY_DEATH("Enemy Death"),
        PSI_DESCRIPTION("PSI Description"),
        PHONECALL("Phone Call"),
        DELIVERY_SUCCESS("Delivery Success"),
        DELIVERY_FAILURE("Delivery Failure"),
        DOOR("Door"),
        OTHER("Other");

        @Getter private String name;

        IndexEntryType(String name) {
            this.name = name;
        }
    }

    private IndexEntryType type;
    private String entryId;

    public Mother2ScriptIndexEntry(IndexEntryType type, int entryId) {
        this.type = type;
        this.entryId = String.valueOf(entryId);
    }

    public Mother2ScriptIndexEntry(IndexEntryType type, long entryId) {
        this.type = type;
        this.entryId = String.valueOf(entryId);
    }

    public int getEntryIdAsInt() {
        return Integer.parseInt(entryId);
    }

    public long getEntryIdAsLong() { return Long.parseLong(entryId); }
}
