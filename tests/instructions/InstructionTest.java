package tests.instruction;

import instruction.Instruction;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InstructionTest {

    @Test
    public void testInstructionParsing() {
        Instruction inst = new Instruction("MOV A, #50");
        assertEquals("MOV", inst.getMnemonic());
        assertEquals("A", inst.getOperand1());
        assertEquals("#50", inst.getOperand2());
    }
} 
