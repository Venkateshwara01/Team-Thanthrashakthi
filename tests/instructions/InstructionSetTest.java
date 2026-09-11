package tests.instructions;

import instruction.InstructionSet;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InstructionSetTest {

    @Test
    public void testSupportedOpcodes() {
        assertTrue(InstructionSet.isSupported("MOV"));
        assertTrue(InstructionSet.isSupported("ADD"));
        assertTrue(InstructionSet.isSupported("ANL"));
        assertTrue(InstructionSet.isSupported("PUSH"));
        assertTrue(InstructionSet.isSupported("POP"));
        assertTrue(InstructionSet.isSupported("ENQ"));
        assertTrue(InstructionSet.isSupported("DEQ"));
        assertFalse(InstructionSet.isSupported("INVALID"));
    }
}
