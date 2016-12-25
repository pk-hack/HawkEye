package hawkeye.config.model;

import lombok.NoArgsConstructor;

import java.util.Optional;

@NoArgsConstructor
public class TextTable {
    public static final int NUM_ENTRIES = 256;

    private String[] characterTable = new String[256];

    public TextTable(String[] characterTable) {
        System.arraycopy(characterTable, 0, this.characterTable, 0, Math.min(characterTable.length, NUM_ENTRIES));
    }

    public Optional<String> get(int n) {
        return Optional.ofNullable(characterTable[n]);
    }
}
