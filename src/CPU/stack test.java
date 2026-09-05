package tests.memory;

import memory.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StackTest {
    private Stack stack;

    @BeforeEach
    public void setUp() {
        stack = new Stack();
    }

    @Test
    public void testPushAndPop() {
        int initialSp = stack.getSp();
        stack.push(0x55);

        assertEquals(initialSp + 1, stack.getSp());
        assertEquals(0x55, stack.pop());
        assertEquals(initialSp, stack.getSp());
    }
}