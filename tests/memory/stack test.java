package tests.memory;

import memory.DataMemory;
import memory.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackTest {
    private Stack stack;
    private DataMemory dataMemory;

    @BeforeEach
    public void setUp() {
        dataMemory = new DataMemory();
        stack = new Stack();
    }

    @Test
    public void testPushAndPop() {
        int sp = 0x07;
        
        // Push 0x55 onto stack
        sp = stack.push(sp, 0x55);
        assertEquals(0x08, sp);
        assertEquals(0x55, dataMemory.read(0x08)); // Verify value written to DataMemory

        // Pop value back from stack
        int[] popped = new int[1];
        sp = stack.pop(sp, popped);
        assertEquals(0x07, sp);
        assertEquals(0x55, popped[0]);
    }

    @Test
    public void testMultiplePushAndPop() {
        int sp = 0x07;

        // Sequential PUSH operations
        sp = stack.push(sp, 0x10); // Written to RAM[0x08]
        sp = stack.push(sp, 0x20); // Written to RAM[0x09]
        sp = stack.push(sp, 0x30); // Written to RAM[0x0A]

        assertEquals(0x0A, sp);
        assertEquals(0x10, dataMemory.read(0x08));
        assertEquals(0x20, dataMemory.read(0x09));
        assertEquals(0x30, dataMemory.read(0x0A));

        // Sequential POP operations (LIFO Order)
        int[] popped = new int[1];

        sp = stack.pop(sp, popped);
        assertEquals(0x09, sp);
        assertEquals(0x30, popped[0]);

        sp = stack.pop(sp, popped);
        assertEquals(0x08, sp);
        assertEquals(0x20, popped[0]);

        sp = stack.pop(sp, popped);
        assertEquals(0x07, sp);
        assertEquals(0x10, popped[0]);
    }
}