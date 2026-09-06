package tests.cpu;

import cpu.CPU;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class CPUTest {
    private CPU cpu;

    @BeforeEach
    public void setUp() {
        cpu = new CPU();
        cpu.reset();
    }

    @Test
    @DisplayName("TC01: MOV A, #50")
    public void testMovA() {
        cpu.loadProgram(Collections.singletonList("MOV A, #50"));
        cpu.fetch(); cpu.decode(); cpu.execute();
        assertEquals(0x32, cpu.getRegisters().getAcc());
    }

    @Test
    @DisplayName("TC02: MOV B, #30")
    public void testMovB() {
        cpu.loadProgram(Collections.singletonList("MOV B, #30"));
        cpu.fetch(); cpu.decode(); cpu.execute();
        assertEquals(0x1E, cpu.getRegisters().getB());
    }

    @Test
    @DisplayName("TC03: ADD A, B")
    public void testAdd() {
        cpu.loadProgram(Arrays.asList("MOV A, #50", "MOV B, #30", "ADD A, B"));
        for (int i = 0; i < 3; i++) { cpu.fetch(); cpu.decode(); cpu.execute(); }
        assertEquals(0x50, cpu.getRegisters().getAcc());
        assertFalse(cpu.getFlags().isCy());
    }

    @Test
    @DisplayName("TC04: INC A")
    public void testInc() {
        cpu.loadProgram(Arrays.asList("MOV A, #50", "INC A"));
        for (int i = 0; i < 2; i++) { cpu.fetch(); cpu.decode(); cpu.execute(); }
        assertEquals(0x33, cpu.getRegisters().getAcc());
    }

    @Test
    @DisplayName("TC05: ANL A, B")
    public void testAnl() {
        cpu.loadProgram(Arrays.asList("MOV A, #51", "MOV B, #30", "ANL A, B"));
        for (int i = 0; i < 3; i++) { cpu.fetch(); cpu.decode(); cpu.execute(); }
        assertEquals(0x10, cpu.getRegisters().getAcc());
    }

    @Test
    @DisplayName("TC06: END Instruction")
    public void testEnd() {
        cpu.loadProgram(Collections.singletonList("END"));
        cpu.fetch(); cpu.decode(); cpu.execute();
        assertTrue(cpu.isHalted());
    }
}