package hawkeye.game.mother2.util;

import com.google.common.collect.ImmutableList;
import hawkeye.config.model.ControlCode;
import hawkeye.config.model.TextTable;
import hawkeye.rom.exceptions.ROMAccessException;
import hawkeye.rom.util.ROM;
import hawkeye.rom.util.SnesUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class EBTextCompressionCodesReader {
    private static final int POINTER_TABLE_OFFSET = 0x8cded;
    private static final List<Integer> CONTROL_CODES = ImmutableList.of(21, 22, 23);
    private static final int NUM_PARAMETERS = 256;

    public Collection<ControlCode> getTextCompressionControlCodes(ROM rom, TextTable textTable)
            throws ROMAccessException {
        List<ControlCode> output = new ArrayList<>(CONTROL_CODES.size() * NUM_PARAMETERS);

        int pointerOffset = POINTER_TABLE_OFFSET;
        for (int opcode : CONTROL_CODES) {
            for (int parameter = 0; parameter < NUM_PARAMETERS; ++parameter) {
                long offset = SnesUtil.convertSnesAddressToRegularOffset(
                        rom.readMultiLittleEndian(pointerOffset, 4));
                pointerOffset += 4;

                String string = readNullTerminatedString(rom, textTable, offset);
                ControlCode controlCode = ControlCode.builder()
                        .identifier(ImmutableList.of(opcode, parameter))
                        .description("comp " + string)
                        .length(Optional.of(2))
                        .isTerminal(false)
                        .dialogueRepresentation(Optional.of(string))
                        .referenceSettings(Optional.empty())
                        .build();
                output.add(controlCode);
            }
        }

        return output;
    }

    private String readNullTerminatedString(ROM rom, TextTable textTable, long offset) throws ROMAccessException {
        StringBuilder stringBuilder = new StringBuilder();
        int data;
        while (true) {
            data = rom.read(offset);
            offset++;
            if (data == 0) {
                break;
            }
            stringBuilder.append(textTable.get(data).get());
        }
        return stringBuilder.toString();
    }
}
