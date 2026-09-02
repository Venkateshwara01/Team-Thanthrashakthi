package cpu;

/**
 * Test suite for verifying the CPU component of an educational 8-bit microcontroller simulator.
 * <p>
 * Tests verify:
 * 1. CPU Reset behavior
 * 2. Program Loading into memory
 * 3. fetch() stage and Program Counter (PC) increment
 * 4. decode() stage and mnemonic resolution
 * 5. execute() stage and ALU register manipulation
 * 6. Full Fetch-Decode-Execute cycle with step()
 * 7. Individual instructions: NOP, MOV A #imm, ADD A #imm, INC A, DEC A, CLR A
 * 8. Status flag updates: Carry (CY), Overflow (OV), Parity (P), Zero (Z)
 * </p>
 */
public class CPUTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("          8-BIT MICROCONTROLLER CPU TEST SUITE (WEEK 2)           ");
        System.out.println("==================================================================\n");

        CPUTest suite = new CPUTest();

        // 1. Core Lifecycle & Cycle Tests
        suite.testResetBehavior();
        suite.testLoadProgram();
        suite.testFetch();
        suite.testDecode();
        suite.testExecute();
        suite.testStepCycle();

        // 2. Instruction Specific & Flag Verification Tests
        suite.testInstructionNOP();
        suite.testInstructionMovImmediate();
        suite.testInstructionAddImmediate();
        suite.testInstructionAddWithCarry();
        suite.testInstructionAddWithSignedOverflow();
        suite.testInstructionIncA();
        suite.testInstructionDecA();
        suite.testInstructionClrA();

        // Final Test Summary
        System.out.println("\n==================================================================");
        System.out.printf(" TEST RESULTS SUMMARY: TOTAL = %d | PASSED = %d | FAILED = %d\n",
                totalTests, passedTests, failedTests);
        System.out.println("==================================================================");

        if (failedTests == 0) {
            System.out.println(">>> ALL CPU UNIT TESTS PASSED SUCCESSFULLY! <<<\n");
        } else {
            System.out.println(">>> SOME CPU UNIT TESTS FAILED. PLEASE CHECK THE LOG ABOVE. <<<\n");
        }
    }

    // =========================================================================
    //                            TEST CASES
    // =========================================================================

    /**
     * Requirement 1: Test CPU reset behavior.
     */
    public void testResetBehavior() {
        System.out.println("[TEST 1] Testing CPU Reset Behavior...");
        CPU cpu = new CPU();

        // Dirty the CPU state
        cpu.getRegisters().setA(0x7F);
        cpu.getRegisters().setB(0x20);
        cpu.getRegisters().setPC(0x0100);
        cpu.getFlags().setCarry(true);
        cpu.getFlags().setOverflow(true);
        cpu.getFlags().setParity(true);
        cpu.getFlags().setZero(true);

        // Perform Reset
        cpu.reset();

        // Verify registers and flags are cleared
        boolean pcZero = (cpu.getRegisters().getPC() == 0x0000);
        boolean accZero = (cpu.getRegisters().getA() == 0x00);
        boolean bZero = (cpu.getRegisters().getB() == 0x00);
        boolean cyZero = !cpu.getFlags().isCarry();
        boolean ovZero = !cpu.getFlags().isOverflow();
        boolean pZero = !cpu.getFlags().isParity();
        boolean zZero = !cpu.getFlags().isZero();
        boolean noInstr = (cpu.getCurrentInstruction() == null);
        boolean notHalted = !cpu.isHalted();

        boolean pass = pcZero && accZero && bZero && cyZero && ovZero && pZero && zZero && noInstr && notHalted;
        assertCondition("Reset clears PC, A, B, flags, and instruction state", pass,
                String.format("PC=0x%04X, A=0x%02X, CY=%b, OV=%b, P=%b, Z=%b",
                        cpu.getRegisters().getPC(), cpu.getRegisters().getA(),
                        cpu.getFlags().isCarry(), cpu.getFlags().isOverflow(),
                        cpu.getFlags().isParity(), cpu.getFlags().isZero()));
    }

    /**
     * Requirement 2: Test loading a program into program memory.
     */
    public void testLoadProgram() {
        System.out.println("[TEST 2] Testing Program Loading into Memory...");
        CPU cpu = new CPU();

        int[] sampleProgram = { CPU.OP_MOV_A_IMM, 0x33, CPU.OP_INC_A, CPU.OP_NOP };
        cpu.loadProgram(0x0000, sampleProgram);

        boolean pcSet = (cpu.getRegisters().getPC() == 0x0000);
        boolean b0 = (cpu.getMemoryByte(0x0000) == CPU.OP_MOV_A_IMM);
        boolean b1 = (cpu.getMemoryByte(0x0001) == 0x33);
        boolean b2 = (cpu.getMemoryByte(0x0002) == CPU.OP_INC_A);
        boolean b3 = (cpu.getMemoryByte(0x0003) == CPU.OP_NOP);

        boolean pass = pcSet && b0 && b1 && b2 && b3;
        assertCondition("Program bytes loaded correctly into program memory and PC set to entry point", pass,
                String.format("Memory=[0x%02X, 0x%02X, 0x%02X, 0x%02X], PC=0x%04X",
                        cpu.getMemoryByte(0), cpu.getMemoryByte(1), cpu.getMemoryByte(2), cpu.getMemoryByte(3),
                        cpu.getRegisters().getPC()));
    }

    /**
     * Requirement 3: Test fetch() stage and Program Counter (PC) increments.
     */
    public void testFetch() {
        System.out.println("[TEST 3] Testing fetch() Stage and PC Increments...");
        CPU cpu = new CPU();

        // 1-byte instruction (INC A: 0x04) followed by 2-byte instruction (MOV A, #0x55: 0x74 0x55)
        int[] program = { CPU.OP_INC_A, CPU.OP_MOV_A_IMM, 0x55 };
        cpu.loadProgram(program);

        // Fetch 1: 1-byte instruction
        cpu.fetch();
        CPU.Instruction instr1 = cpu.getCurrentInstruction();
        boolean fetch1Pass = (instr1 != null) && (instr1.getOpcode() == CPU.OP_INC_A)
                && (instr1.getAddress() == 0x0000) && (instr1.getLength() == 1)
                && (cpu.getRegisters().getPC() == 0x0001);

        // Fetch 2: 2-byte instruction
        cpu.fetch();
        CPU.Instruction instr2 = cpu.getCurrentInstruction();
        boolean fetch2Pass = (instr2 != null) && (instr2.getOpcode() == CPU.OP_MOV_A_IMM)
                && (instr2.getOperand() == 0x55) && (instr2.getAddress() == 0x0001)
                && (instr2.getLength() == 2) && (cpu.getRegisters().getPC() == 0x0003);

        assertCondition("fetch() fetches opcode/operand and increments PC correctly (1-byte: PC+1, 2-byte: PC+2)",
                fetch1Pass && fetch2Pass,
                String.format("Instr1=[Op:0x%02X, PC:0x%04X], Instr2=[Op:0x%02X, Opnd:0x%02X, PC:0x%04X]",
                        instr1 != null ? instr1.getOpcode() : -1,
                        cpu.getRegisters().getPC(),
                        instr2 != null ? instr2.getOpcode() : -1,
                        instr2 != null ? instr2.getOperand() : -1,
                        cpu.getRegisters().getPC()));
    }

    /**
     * Requirement 4: Test decode() stage and mnemonic identification.
     */
    public void testDecode() {
        System.out.println("[TEST 4] Testing decode() Stage...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_MOV_A_IMM, 0x42 };
        cpu.loadProgram(program);

        cpu.fetch();
        cpu.decode();

        CPU.Instruction instr = cpu.getCurrentInstruction();
        boolean pass = (instr != null) && instr.isValid()
                && instr.getMnemonic().contains("MOV A")
                && instr.getMnemonic().contains("42");

        assertCondition("decode() identifies opcode and produces valid mnemonic string", pass,
                String.format("Valid=%b, Mnemonic=\"%s\"",
                        instr != null && instr.isValid(),
                        instr != null ? instr.getMnemonic() : "null"));
    }

    /**
     * Requirement 5: Test execute() stage.
     */
    public void testExecute() {
        System.out.println("[TEST 5] Testing execute() Stage...");
        CPU cpu = new CPU();

        // Initial A = 10, ADD A, #15 -> A should become 25
        cpu.getRegisters().setA(10);
        int[] program = { CPU.OP_ADD_A_IMM, 15 };
        cpu.loadProgram(program);

        cpu.getRegisters().setA(10); // Maintain accumulator value
        cpu.fetch();
        cpu.decode();
        cpu.execute();

        boolean pass = (cpu.getRegisters().getA() == 25);
        assertCondition("execute() performs ALU operation and updates Accumulator A", pass,
                String.format("Expected A=25, Actual A=%d", cpu.getRegisters().getA()));
    }

    /**
     * Requirement 6: Test complete Fetch-Decode-Execute cycle via step().
     */
    public void testStepCycle() {
        System.out.println("[TEST 6] Testing Full Instruction Cycle via step()...");
        CPU cpu = new CPU();

        int[] program = {
            CPU.OP_MOV_A_IMM, 0x05, // Step 1: A = 5
            CPU.OP_INC_A,           // Step 2: A = 6
            CPU.OP_ADD_A_IMM, 0x04  // Step 3: A = 10
        };
        cpu.loadProgram(program);

        boolean s1 = cpu.step(); // MOV A, #0x05
        boolean a1 = (cpu.getRegisters().getA() == 0x05) && (cpu.getRegisters().getPC() == 0x0002);

        boolean s2 = cpu.step(); // INC A
        boolean a2 = (cpu.getRegisters().getA() == 0x06) && (cpu.getRegisters().getPC() == 0x0003);

        boolean s3 = cpu.step(); // ADD A, #0x04
        boolean a3 = (cpu.getRegisters().getA() == 0x0A) && (cpu.getRegisters().getPC() == 0x0005);

        boolean pass = s1 && a1 && s2 && a2 && s3 && a3;
        assertCondition("step() successfully advances through multiple instruction cycles", pass,
                String.format("Final A=0x%02X (Expected 0x0A), PC=0x%04X (Expected 0x0005)",
                        cpu.getRegisters().getA(), cpu.getRegisters().getPC()));
    }

    /**
     * Requirement 7a: Test NOP instruction.
     */
    public void testInstructionNOP() {
        System.out.println("[TEST 7] Testing Instruction: NOP (0x00)...");
        CPU cpu = new CPU();

        cpu.getRegisters().setA(0x42);
        int[] program = { CPU.OP_NOP };
        cpu.loadProgram(program);

        cpu.step();

        boolean pass = (cpu.getRegisters().getA() == 0x42)
                && (cpu.getRegisters().getPC() == 0x0001)
                && !cpu.isErrorState();

        assertCondition("NOP executes with 1-byte advance, leaving A and flags intact", pass,
                String.format("A=0x%02X, PC=0x%04X", cpu.getRegisters().getA(), cpu.getRegisters().getPC()));
    }

    /**
     * Requirement 7b: Test MOV A, #immediate instruction.
     */
    public void testInstructionMovImmediate() {
        System.out.println("[TEST 8] Testing Instruction: MOV A, #immediate (0x74)...");
        CPU cpu = new CPU();

        // 0x55 = 01010101b (4 set bits -> Even Parity P = false)
        // 0x07 = 00000111b (3 set bits -> Odd Parity P = true)
        int[] program = {
            CPU.OP_MOV_A_IMM, 0x55,
            CPU.OP_MOV_A_IMM, 0x07
        };
        cpu.loadProgram(program);

        cpu.step(); // MOV A, #0x55
        boolean pass1 = (cpu.getRegisters().getA() == 0x55) && !cpu.getFlags().isParity() && !cpu.getFlags().isZero();

        cpu.step(); // MOV A, #0x07
        boolean pass2 = (cpu.getRegisters().getA() == 0x07) && cpu.getFlags().isParity() && !cpu.getFlags().isZero();

        assertCondition("MOV A, #imm loads value into A and correctly updates Parity & Zero flags",
                pass1 && pass2,
                String.format("Pass1(A=0x55, P=%b), Pass2(A=0x07, P=%b)",
                        cpu.getFlags().isParity(), cpu.getFlags().isParity()));
    }

    /**
     * Requirement 7c: Test ADD A, #immediate (Basic Addition).
     */
    public void testInstructionAddImmediate() {
        System.out.println("[TEST 9] Testing Instruction: ADD A, #immediate (0x24)...");
        CPU cpu = new CPU();

        int[] program = {
            CPU.OP_MOV_A_IMM, 0x20, // A = 32
            CPU.OP_ADD_A_IMM, 0x15  // A = 32 + 21 = 53 (0x35 = 00110101b -> 4 set bits -> P = false)
        };
        cpu.loadProgram(program);
        cpu.run();

        boolean pass = (cpu.getRegisters().getA() == 0x35)
                && !cpu.getFlags().isCarry()
                && !cpu.getFlags().isOverflow()
                && !cpu.getFlags().isParity()
                && !cpu.getFlags().isZero();

        assertCondition("ADD A, #imm performs 8-bit unsigned addition without carry/overflow", pass,
                String.format("A=0x%02X (Expected 0x35), CY=%b, OV=%b, P=%b, Z=%b",
                        cpu.getRegisters().getA(), cpu.getFlags().isCarry(),
                        cpu.getFlags().isOverflow(), cpu.getFlags().isParity(), cpu.getFlags().isZero()));
    }

    /**
     * Requirement 10a: Test ADD A, #immediate with Carry (CY) Flag.
     */
    public void testInstructionAddWithCarry() {
        System.out.println("[TEST 10] Testing ADD A, #imm Carry Flag (CY) Generation...");
        CPU cpu = new CPU();

        // 0xFF + 0x01 = 0x100 -> A = 0x00, CY = true, Zero = true, Parity = false
        int[] program = {
            CPU.OP_MOV_A_IMM, 0xFF,
            CPU.OP_ADD_A_IMM, 0x01
        };
        cpu.loadProgram(program);
        cpu.run();

        boolean pass = (cpu.getRegisters().getA() == 0x00)
                && cpu.getFlags().isCarry()
                && cpu.getFlags().isZero()
                && !cpu.getFlags().isParity();

        assertCondition("ADD A, #imm sets Carry (CY) flag and Zero (Z) flag on 8-bit overflow (> 255)", pass,
                String.format("A=0x%02X (Expected 0x00), CY=%b (Expected true), Z=%b (Expected true)",
                        cpu.getRegisters().getA(), cpu.getFlags().isCarry(), cpu.getFlags().isZero()));
    }

    /**
     * Requirement 10b: Test ADD A, #immediate with Signed Overflow (OV) Flag.
     */
    public void testInstructionAddWithSignedOverflow() {
        System.out.println("[TEST 11] Testing ADD A, #imm Signed Overflow Flag (OV)...");
        CPU cpu = new CPU();

        // Signed addition: +80 (0x50) + +80 (0x50) = +160 (> +127, overflows to -96 = 0xA0 in 8-bit signed)
        // Expected: OV = true, CY = false
        int[] program = {
            CPU.OP_MOV_A_IMM, 0x50,
            CPU.OP_ADD_A_IMM, 0x50
        };
        cpu.loadProgram(program);
        cpu.run();

        boolean pass = (cpu.getRegisters().getA() == 0xA0)
                && cpu.getFlags().isOverflow()
                && !cpu.getFlags().isCarry();

        assertCondition("ADD A, #imm sets Overflow (OV) flag when signed result exceeds [-128, +127]", pass,
                String.format("A=0x%02X, OV=%b (Expected true), CY=%b (Expected false)",
                        cpu.getRegisters().getA(), cpu.getFlags().isOverflow(), cpu.getFlags().isCarry()));
    }

    /**
     * Requirement 7d: Test INC A instruction.
     */
    public void testInstructionIncA() {
        System.out.println("[TEST 12] Testing Instruction: INC A (0x04)...");
        CPU cpu = new CPU();

        int[] program = {
            CPU.OP_MOV_A_IMM, 0xFE,
            CPU.OP_INC_A, // A becomes 0xFF (8 bits -> P = false)
            CPU.OP_INC_A  // A becomes 0x00 (wrap around -> Z = true, P = false, CY unaffected)
        };
        cpu.loadProgram(program);

        cpu.step(); // MOV A, #0xFE
        cpu.step(); // INC A -> 0xFF
        boolean pass1 = (cpu.getRegisters().getA() == 0xFF) && !cpu.getFlags().isParity();

        cpu.step(); // INC A -> 0x00
        boolean pass2 = (cpu.getRegisters().getA() == 0x00) && cpu.getFlags().isZero();

        assertCondition("INC A increments accumulator with 8-bit wrap-around (0xFF -> 0x00)", pass1 && pass2,
                String.format("A=0x%02X (Expected 0x00), Z=%b (Expected true)",
                        cpu.getRegisters().getA(), cpu.getFlags().isZero()));
    }

    /**
     * Requirement 7e: Test DEC A instruction.
     */
    public void testInstructionDecA() {
        System.out.println("[TEST 13] Testing Instruction: DEC A (0x14)...");
        CPU cpu = new CPU();

        int[] program = {
            CPU.OP_MOV_A_IMM, 0x01,
            CPU.OP_DEC_A, // A becomes 0x00 -> Z = true
            CPU.OP_DEC_A  // A becomes 0xFF (underflow wrap around -> Z = false)
        };
        cpu.loadProgram(program);

        cpu.step(); // MOV A, #0x01
        cpu.step(); // DEC A -> 0x00
        boolean pass1 = (cpu.getRegisters().getA() == 0x00) && cpu.getFlags().isZero();

        cpu.step(); // DEC A -> 0xFF
        boolean pass2 = (cpu.getRegisters().getA() == 0xFF) && !cpu.getFlags().isZero();

        assertCondition("DEC A decrements accumulator with 8-bit underflow wrap (0x00 -> 0xFF)", pass1 && pass2,
                String.format("A=0x%02X (Expected 0xFF), Z=%b (Expected false)",
                        cpu.getRegisters().getA(), cpu.getFlags().isZero()));
    }

    /**
     * Requirement 7f: Test CLR A instruction.
     */
    public void testInstructionClrA() {
        System.out.println("[TEST 14] Testing Instruction: CLR A (0xE4)...");
        CPU cpu = new CPU();

        cpu.getRegisters().setA(0xAB);
        int[] program = { CPU.OP_CLR_A };
        cpu.loadProgram(program);

        cpu.step();

        boolean pass = (cpu.getRegisters().getA() == 0x00)
                && cpu.getFlags().isZero()
                && !cpu.getFlags().isParity();

        assertCondition("CLR A clears accumulator to 0x00, updates Zero flag (true) and Parity (false)", pass,
                String.format("A=0x%02X, Z=%b, P=%b",
                        cpu.getRegisters().getA(), cpu.getFlags().isZero(), cpu.getFlags().isParity()));
    }

    // =========================================================================
    //                        TEST HELPER / ASSERTION
    // =========================================================================

    private static void assertCondition(String testName, boolean condition, String details) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            failedTests++;
            System.err.println("  [FAIL] " + testName);
            System.err.println("         Details: " + details);
        }
    }
}
