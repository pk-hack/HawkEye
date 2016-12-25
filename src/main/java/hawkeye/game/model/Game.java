package hawkeye.game.model;

import hawkeye.config.model.ROMType;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ROM;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;

@AllArgsConstructor
public abstract class Game<TScriptIndexEntry extends ScriptIndexEntry> {
    @Getter
    protected ROMType romType;

    @Getter
    protected ROM rom;

    public String getDisplayName() {
        return romType.getShortName() + " (" + romType.getPlatform() + ")";
    }

    public abstract int read(long offset) throws ROMAccessException;
    public abstract int[] readArray(long offset, int length) throws ROMAccessException;
    public abstract Optional<Long> readPointer(long offset) throws ROMAccessException;
    public abstract Optional<Long> readRelativePointer(long offset, long base) throws ROMAccessException;
}
