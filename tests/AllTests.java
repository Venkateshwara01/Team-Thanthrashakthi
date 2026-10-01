package tests;

import memory.DataMemory;
import memory.Stack;
import memory.FifoQueue;
import cpu.CPU;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AllTests {

    private DataMemory dataMemory;
    private Stack stack;
    private FifoQueue queue;
    private CPU cpu;

    @BeforeEach
    public void setUp() {
        dataMemory = new DataMemory();
        stack = new Stack(dataMemory);
        queue = new FifoQueue(3);
        cpu = new CPU();
    }

    // ==========================================
    // DATA MEMORY TESTS
    // ==========================================
    @Test
    public void testMemoryWriteAndRead() {
        dataMemory.write(0x10, 0xAB);
        assertEquals(0xAB, dataMemory.read(0x10));
    }

    @Test
    public void testMemoryReset() {
        dataMemory.write(0x05, 0xFF);
        dataMemory.reset();
        assertEquals(0x00, dataMemory.read(0x05));
    }

    @Test
    public void testMemoryMasking() {
        dataMemory.write(0x00, 0x1FF);
        assertEquals(0xFF, dataMemory.read(0x00));
    }

    // ==========================================
    // STACK TESTS
    // ==========================================
    @Test
    public void testStackPushAndPop() {
        int sp = 0x07;
        sp = stack.push(sp, 0x55);
        assertEquals(0x08, sp);
        assertEquals(0x55, dataMemory.read(0x08));

        int[] popped = new int[1];
        sp = stack.pop(sp, popped);
        assertEquals(0x07, sp);
        assertEquals(0x55, popped[0]);
    }

    @Test
    public void testStackMultipleOperations() {
        int sp = 0x07;
        sp = stack.push(sp, 0x10);
        sp = stack.push(sp, 0x20);

        assertEquals(0x09, sp);
        
        int[] popped = new int[1];
        sp = stack.pop(sp, popped);
        assertEquals(0x20, popped[0]);
        sp = stack.pop(sp, popped);
        assertEquals(0x10, popped[0]);
        assertEquals(0x07, sp);
    }

    // ==========================================
    // FIFO QUEUE TESTS
    // ==========================================
    @Test
    public void testQueueOrdering() {
        assertTrue(queue.enqueue(0x10));
        assertTrue(queue.enqueue(0x20));
        assertEquals(0x10, queue.dequeue());
        assertEquals(0x20, queue.dequeue());
    }

    @Test
    public void testQueueOverflowAndUnderflow() {
        assertTrue(queue.isEmpty());
        assertEquals(-1, queue.dequeue()); // Underflow

        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);
        
        assertTrue(queue.isFull());
        assertFalse(queue.enqueue(4)); // Overflow
    }

    // ==========================================
    // CPU INTEGRATION TESTS
    // ==========================================
    @Test
    public void testCpuPushAndPopExecution() {
        List<String> program = Arrays.asList(
            "MOV A, #55",
            "PUSH A",
            "MOV A, #00",
            "POP A",
            "END"
        );
        cpu.loadProgram(program);

        while (!cpu.isHalted()) {
            cpu.fetch();
            cpu.decode();
            cpu.execute();
        }

        assertEquals(55, cpu.getRegisters().getAcc());
        assertEquals(0x07, cpu.getRegisters().getSp());
    }

    @Test
    public void testCpuQueueExecution() {
        List<String> program = Arrays.asList(
            "MOV A, #99", // Corrected instruction value from #55 to #99
            "ENQ A",
            "MOV A, #00",
            "DEQ",
            "END"
        );
        cpu.loadProgram(program);

        while (!cpu.isHalted()) {
            cpu.fetch();
            cpu.decode();
            cpu.execute();
        }

        assertEquals(99, cpu.getRegisters().getAcc());
        assertTrue(cpu.getFifoQueue().isEmpty());
    }
}