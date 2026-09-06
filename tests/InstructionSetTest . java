package tests.instruction;

import instruction.InstructionSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InstructionSetTest {

    @Test
    public void testSupportedOpcodes() {
        assertTrue(InstructionSet.isSupported("MOV"));
        assertTrue(InstructionSet.isSupported("ADD"));
        assertTrue(InstructionSet.isSupported("ANL"));
        assertFalse(InstructionSet.isSupported("INVALID"));
    }
}
