package tests.memory;

import memory.FifoQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class FifoQueueTest {
    private FifoQueue queue;

    @BeforeEach
    public void setUp() {
        queue = new FifoQueue(3);
    }

    @Test
    public void testFifoOrder() {
        assertTrue(queue.enqueue(0x10));
        assertTrue(queue.enqueue(0x20));
        assertEquals(0x10, queue.dequeue());
        assertEquals(0x20, queue.dequeue());
    }

    @Test
    public void testOverflowAndUnderflow() {
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        assertFalse(queue.enqueue(4)); // Overflow

        queue.dequeue();
        queue.dequeue();
        queue.dequeue();
        assertEquals(-1, queue.dequeue()); // Underflow
    }
}
