package tests.memory;

import memory.ProgramMemory;
import org.junit.jupiter.api.Test;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class MemoryTest {

    @Test
    public void testProgramMemoryLoading() {
        ProgramMemory memory = new ProgramMemory();
        memory.load(Arrays.asList("MOV A, #10", "; comment line", "ADD A, B"));
        
        assertEquals(2, memory.size(), "Comment lines should be ignored during loading");
        assertEquals("MOV", memory.getInstruction(0).getMnemonic());
        assertEquals("ADD", memory.getInstruction(1).getMnemonic());
    }
}
