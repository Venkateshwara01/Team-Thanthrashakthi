package instruction;

import java.util.Arrays;
import java.util.List;

public class InstructionSet {
    public static final List<String> SUPPORTED = Arrays.asList(
        "MOV", "ADD", "SUBB", "ANL", "INC", "SJMP","PUSH", "POP", "ENQ", "DEQ", "END"
    );

    public static boolean isSupported(String mnemonic) {
        return SUPPORTED.contains(mnemonic.toUpperCase());
    }
}
