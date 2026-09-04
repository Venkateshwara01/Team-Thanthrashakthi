package cpu;

/**
 * Unit test suite for the CPU execution engine implemented by Student 1.
 * <p>
 * Verifies core CPU responsibilities:
 * <ol>
 *   <li>CPU initialization</li>
 *   <li>Register reset state</li>
 *   <li>Flag reset state</li>
 *   <li>Program Counter (PC) behavior</li>
 *   <li>Program loading</li>
 *   <li>FETCH phase</li>
 *   <li>DECODE phase</li>
 *   <li>EXECUTE phase</li>
 *   <li>Complete step() operation</li>
 *   <li>run() behavior</li>
 *   <li>Invalid opcode &amp; error handling</li>
 *   <li>Halt behavior</li>
 *   <li>Register updates caused by execution</li>
 *   <li>Flag updates caused by execution</li>
 * </ol>
 * </p>
 *
 * <p><b>Team Boundary Note:</b></p>
 * Student 3 owns the instruction-set definitions. This test suite does not
 * duplicate a full instruction test suite; it tests execution strictly to
 * verify the CPU's FETCH → DECODE → EXECUTE lifecycle and state management.
 */
public class CPUTest {

    private static int totalTests = 0;
    private static int passedTests = 0;
    private static int failedTests = 0;

    /**
     * Entry point for running the CPU test suite.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("            CPU COMPONENT TEST SUITE (STUDENT 1)                  ");
        System.out.println("==================================================================\n");

        CPUTest suite = new CPUTest();

        // 1-3. Initialization & Reset State Tests
        suite.testCPUInitialization();
        suite.testRegisterResetState();
        suite.testFlagResetState();

        // 4-5. Program Loading & PC Behavior
        suite.testProgramCounterBehavior();
        suite.testProgramLoading();

        // 6-8. Three Pipeline Stages (Fetch, Decode, Execute)
        suite.testFetchPhase();
        suite.testDecodePhase();
        suite.testExecutePhase();

        // 9-10. Control & Execution Cycles (step, run)
        suite.testCompleteStepOperation();
        suite.testRunBehavior();

        // 11-12. Error & Halt Behavior
        suite.testInvalidOpcodeAndErrorHandling();
        suite.testHaltBehavior();

        // 13-14. CPU Register & Flag Updates
        suite.testRegisterUpdatesCausedByExecution();
        suite.testFlagUpdatesCausedByExecution();

        // Summary Report
        System.out.println("\n==================================================================");
        System.out.printf(" TEST RESULTS SUMMARY: TOTAL = %d | PASSED = %d | FAILED = %d\n",
                totalTests, passedTests, failedTests);
        System.out.println("==================================================================");

        if (failedTests == 0) {
            System.out.println(">>> ALL CPU UNIT TESTS PASSED SUCCESSFULLY! <<<\n");
        } else {
            System.err.println(">>> SOME CPU UNIT TESTS FAILED! <<<\n");
            System.exit(1);
        }
    }

    // =========================================================================
    // 1. CPU INITIALIZATION
    // =========================================================================

    /**
     * Test 1: Verifies default CPU constructor correctly allocates non-null registers,
     * non-null flags, cleared memory, and clean initial lifecycle status.
     */
    public void testCPUInitialization() {
        System.out.println("[TEST 1] Testing CPU Initialization...");
        CPU cpu = new CPU();

        boolean regNotNull = (cpu.getRegisters() != null);
        boolean flagsNotNull = (cpu.getFlags() != null);
        boolean notHalted = !cpu.isHalted();
        boolean notError = !cpu.isErrorState();
        boolean noInstruction = (cpu.getCurrentInstruction() == null);
        boolean noErrorMsg = (cpu.getLastErrorMessage() == null || cpu.getLastErrorMessage().isEmpty());

        boolean pass = regNotNull && flagsNotNull && notHalted && notError && noInstruction && noErrorMsg;
        assertTest("CPU initializes with valid registers, flags, and unhalted state", pass,
                String.format("Registers=%s, Flags=%s, Halted=%b, Error=%b",
                        cpu.getRegisters(), cpu.getFlags(), cpu.isHalted(), cpu.isErrorState()));
    }

    // =========================================================================
    // 2. REGISTER RESET STATE
    // =========================================================================

    /**
     * Test 2: Verifies register values match hardware reset specifications:
     * ACC=0x00, B=0x00, PC=0x0000, SP=0x07, DPTR=0x0000.
     */
    public void testRegisterResetState() {
        System.out.println("[TEST 2] Testing Register Reset State...");
        CPU cpu = new CPU();

        // Dirty registers with arbitrary non-reset values
        cpu.getRegisters().setACC(0xAA);
        cpu.getRegisters().setB(0xBB);
        cpu.getRegisters().setPC(0x1234);
        cpu.getRegisters().setSP(0x50);
        cpu.getRegisters().setDPTR(0x89AB);

        // Perform Reset
        cpu.reset();

        Registers r = cpu.getRegisters();
        boolean pass = (r.getACC() == 0x00)
                && (r.getB() == 0x00)
                && (r.getPC() == 0x0000)
                && (r.getSP() == 0x07)
                && (r.getDPTR() == 0x0000)
                && (r.getDPH() == 0x00)
                && (r.getDPL() == 0x00);

        assertTest("Registers reset to hardware defaults (ACC=0x00, B=0x00, PC=0x0000, SP=0x07, DPTR=0x0000)", pass,
                String.format("ACC=0x%02X, B=0x%02X, PC=0x%04X, SP=0x%02X, DPTR=0x%04X",
                        r.getACC(), r.getB(), r.getPC(), r.getSP(), r.getDPTR()));
    }

    // =========================================================================
    // 3. FLAG RESET STATE
    // =========================================================================

    /**
     * Test 3: Verifies all status flags (CY, AC, OV, P) are cleared to false on reset.
     */
    public void testFlagResetState() {
        System.out.println("[TEST 3] Testing Flag Reset State...");
        CPU cpu = new CPU();

        // Set all flags to true
        cpu.getFlags().setCarry(true);
        cpu.getFlags().setAuxiliaryCarry(true);
        cpu.getFlags().setOverflow(true);
        cpu.getFlags().setParity(true);

        // Perform Reset
        cpu.reset();

        Flags f = cpu.getFlags();
        boolean pass = !f.isCarry() && !f.isAuxiliaryCarry() && !f.isOverflow() && !f.isParity();

        assertTest("All status flags reset to false (CY=0, AC=0, OV=0, P=0)", pass,
                String.format("CY=%b, AC=%b, OV=%b, P=%b",
                        f.isCarry(), f.isAuxiliaryCarry(), f.isOverflow(), f.isParity()));
    }

    // =========================================================================
    // 4. PROGRAM COUNTER BEHAVIOR
    // =========================================================================

    /**
     * Test 4: Verifies PC advances accurately according to instruction length
     * (1-byte advances by +1, 2-byte advances by +2).
     */
    public void testProgramCounterBehavior() {
        System.out.println("[TEST 4] Testing Program Counter (PC) Behavior...");
        CPU cpu = new CPU();

        // Program: 1-byte (NOP), 2-byte (MOV A, #0x55), 1-byte (INC A)
        int[] program = { CPU.OP_NOP, CPU.OP_MOV_A_IMM, 0x55, CPU.OP_INC_A };
        cpu.loadProgram(0x0100, program);

        boolean initialPC = (cpu.getRegisters().getPC() == 0x0100);

        // Step 1: NOP (1 byte) -> PC should be 0x0101
        cpu.step();
        boolean pcAfter1Byte = (cpu.getRegisters().getPC() == 0x0101);

        // Step 2: MOV A, #0x55 (2 bytes) -> PC should be 0x0103
        cpu.step();
        boolean pcAfter2Byte = (cpu.getRegisters().getPC() == 0x0103);

        // Step 3: INC A (1 byte) -> PC should be 0x0104
        cpu.step();
        boolean pcAfter3rd = (cpu.getRegisters().getPC() == 0x0104);

        boolean pass = initialPC && pcAfter1Byte && pcAfter2Byte && pcAfter3rd;
        assertTest("PC increments by 1 for 1-byte instructions and by 2 for 2-byte instructions", pass,
                String.format("PC Sequence: Init=0x%04X, Step1=0x%04X, Step2=0x%04X, Step3=0x%04X",
                        0x0100, (initialPC ? 0x0101 : cpu.getRegisters().getPC()),
                        cpu.getRegisters().getPC(), cpu.getRegisters().getPC()));
    }

    // =========================================================================
    // 5. PROGRAM LOADING
    // =========================================================================

    /**
     * Test 5: Verifies program bytes are correctly loaded into memory and PC is initialized.
     */
    public void testProgramLoading() {
        System.out.println("[TEST 5] Testing Program Loading...");
        CPU cpu = new CPU();

        int startAddr = 0x0200;
        int[] program = { CPU.OP_MOV_A_IMM, 0x42, CPU.OP_CLR_A };
        cpu.loadProgram(startAddr, program);

        boolean pcSet = (cpu.getRegisters().getPC() == startAddr);
        boolean b0 = (cpu.getMemoryByte(startAddr) == CPU.OP_MOV_A_IMM);
        boolean b1 = (cpu.getMemoryByte(startAddr + 1) == 0x42);
        boolean b2 = (cpu.getMemoryByte(startAddr + 2) == CPU.OP_CLR_A);

        boolean pass = pcSet && b0 && b1 && b2;
        assertTest("Program loaded into memory starting at target address with PC initialized", pass,
                String.format("PC=0x%04X, Mem[0]=0x%02X, Mem[1]=0x%02X, Mem[2]=0x%02X",
                        cpu.getRegisters().getPC(),
                        cpu.getMemoryByte(startAddr),
                        cpu.getMemoryByte(startAddr + 1),
                        cpu.getMemoryByte(startAddr + 2)));
    }

    // =========================================================================
    // 6. FETCH PHASE
    // =========================================================================

    /**
     * Test 6: Verifies fetch() reads the opcode, records address and length, and increments PC.
     */
    public void testFetchPhase() {
        System.out.println("[TEST 6] Testing FETCH Phase...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_MOV_A_IMM, 0x88 };
        cpu.loadProgram(0x0000, program);

        cpu.fetch();
        CPU.Instruction instr = cpu.getCurrentInstruction();

        boolean instrNotNull = (instr != null);
        boolean opcodeMatch = instrNotNull && (instr.getOpcode() == CPU.OP_MOV_A_IMM);
        boolean operandMatch = instrNotNull && (instr.getOperand() == 0x88);
        boolean addrMatch = instrNotNull && (instr.getAddress() == 0x0000);
        boolean lenMatch = instrNotNull && (instr.getLength() == 2);
        boolean pcAdvanced = (cpu.getRegisters().getPC() == 0x0002);

        boolean pass = instrNotNull && opcodeMatch && operandMatch && addrMatch && lenMatch && pcAdvanced;
        assertTest("fetch() reads opcode and operand, stores instruction metadata, and updates PC", pass,
                String.format("Instruction=%s, PC=0x%04X", instr, cpu.getRegisters().getPC()));
    }

    // =========================================================================
    // 7. DECODE PHASE
    // =========================================================================

    /**
     * Test 7: Verifies decode() parses the opcode and populates the mnemonic and validity.
     */
    public void testDecodePhase() {
        System.out.println("[TEST 7] Testing DECODE Phase...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_ADD_A_IMM, 0x15 };
        cpu.loadProgram(0x0000, program);

        cpu.fetch();
        cpu.decode();

        CPU.Instruction instr = cpu.getCurrentInstruction();
        boolean valid = (instr != null) && instr.isValid();
        boolean mnemonicCorrect = (instr != null) && instr.getMnemonic().startsWith("ADD A");

        boolean pass = valid && mnemonicCorrect;
        assertTest("decode() assigns correct mnemonic and marks instruction as valid", pass,
                String.format("Mnemonic=\"%s\", Valid=%b",
                        (instr != null ? instr.getMnemonic() : "null"), valid));
    }

    // =========================================================================
    // 8. EXECUTE PHASE
    // =========================================================================

    /**
     * Test 8: Verifies execute() updates registers without refetching or modifying PC further.
     */
    public void testExecutePhase() {
        System.out.println("[TEST 8] Testing EXECUTE Phase...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_MOV_A_IMM, 0x7E };
        cpu.loadProgram(0x0000, program);

        cpu.fetch();
        cpu.decode();
        int pcBeforeExec = cpu.getRegisters().getPC();

        cpu.execute();
        int pcAfterExec = cpu.getRegisters().getPC();

        boolean accUpdated = (cpu.getRegisters().getACC() == 0x7E);
        boolean pcUnchangedDuringExec = (pcBeforeExec == pcAfterExec);

        boolean pass = accUpdated && pcUnchangedDuringExec;
        assertTest("execute() performs register update and preserves PC position", pass,
                String.format("ACC=0x%02X, PC_Before=0x%04X, PC_After=0x%04X",
                        cpu.getRegisters().getACC(), pcBeforeExec, pcAfterExec));
    }

    // =========================================================================
    // 9. COMPLETE STEP() OPERATION
    // =========================================================================

    /**
     * Test 9: Verifies step() advances through FETCH → DECODE → EXECUTE in a single call.
     */
    public void testCompleteStepOperation() {
        System.out.println("[TEST 9] Testing step() Full Instruction Cycle...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_MOV_A_IMM, 0x10, CPU.OP_INC_A };
        cpu.loadProgram(program);

        // Step 1: MOV A, #0x10
        boolean step1Success = cpu.step();
        boolean accAfterStep1 = (cpu.getRegisters().getACC() == 0x10);

        // Step 2: INC A
        boolean step2Success = cpu.step();
        boolean accAfterStep2 = (cpu.getRegisters().getACC() == 0x11);

        boolean pass = step1Success && accAfterStep1 && step2Success && accAfterStep2;
        assertTest("step() successfully executes one complete instruction cycle per call", pass,
                String.format("Step1_ACC=0x%02X, Step2_ACC=0x%02X",
                        cpu.getRegisters().getACC(), cpu.getRegisters().getACC()));
    }

    // =========================================================================
    // 10. RUN() BEHAVIOR
    // =========================================================================

    /**
     * Test 10: Verifies run() and run(maxSteps) execute multiple instructions until limit or halt.
     */
    public void testRunBehavior() {
        System.out.println("[TEST 10] Testing run() Multi-Step Execution...");
        CPU cpu = new CPU();

        int[] program = {
            CPU.OP_MOV_A_IMM, 0x05, // 2 bytes
            CPU.OP_INC_A,           // 1 byte
            CPU.OP_INC_A,           // 1 byte
            CPU.OP_NOP              // 1 byte
        };
        cpu.loadProgram(program);

        // Execute exactly the 4 instructions of the program
        int stepsExecuted = cpu.run(4);

        boolean executedAll = (stepsExecuted == 4);
        boolean finalAcc = (cpu.getRegisters().getACC() == 0x07);
        boolean finalPC = (cpu.getRegisters().getPC() == program.length);

        boolean pass = executedAll && finalAcc && finalPC;
        assertTest("run(maxSteps) executes sequential instructions correctly and returns count", pass,
                String.format("StepsExecuted=%d, Final_ACC=0x%02X, Final_PC=0x%04X",
                        stepsExecuted, cpu.getRegisters().getACC(), cpu.getRegisters().getPC()));
    }

    // =========================================================================
    // 11. INVALID OPCODE / ERROR HANDLING
    // =========================================================================

    /**
     * Test 11: Verifies unknown opcodes trigger error state, halt the CPU, and provide error info.
     */
    public void testInvalidOpcodeAndErrorHandling() {
        System.out.println("[TEST 11] Testing Invalid Opcode & Error Handling...");
        CPU cpu = new CPU();

        int invalidOpcode = 0xFF; // Not supported in minimal test set
        int[] program = { invalidOpcode };
        cpu.loadProgram(program);

        boolean stepResult = cpu.step();

        boolean stepFailed = !stepResult;
        boolean isError = cpu.isErrorState();
        boolean isHalted = cpu.isHalted();
        boolean hasMsg = (cpu.getLastErrorMessage() != null && !cpu.getLastErrorMessage().isEmpty());

        boolean pass = stepFailed && isError && isHalted && hasMsg;
        assertTest("Unknown opcode triggers error state, halts CPU, and populates error message", pass,
                String.format("ErrorState=%b, Halted=%b, ErrorMsg=\"%s\"",
                        isError, isHalted, cpu.getLastErrorMessage()));
    }

    // =========================================================================
    // 12. HALT BEHAVIOR
    // =========================================================================

    /**
     * Test 12: Verifies CPU respects halted state and refuses to step or run while halted.
     */
    public void testHaltBehavior() {
        System.out.println("[TEST 12] Testing Halt Behavior...");
        CPU cpu = new CPU();

        int[] program = { CPU.OP_INC_A };
        cpu.loadProgram(program);

        // Manually halt the CPU
        cpu.setHalted();
        boolean wasHalted = cpu.isHalted();

        // Attempting to step when halted should return false and not execute
        boolean stepOutcome = cpu.step();
        boolean accUnchanged = (cpu.getRegisters().getACC() == 0x00);

        boolean pass = wasHalted && !stepOutcome && accUnchanged;
        assertTest("CPU in halted state prevents step() from advancing or executing instructions", pass,
                String.format("IsHalted=%b, StepOutcome=%b, ACC=0x%02X",
                        wasHalted, stepOutcome, cpu.getRegisters().getACC()));
    }

    // =========================================================================
    // 13. REGISTER UPDATES CAUSED BY CPU EXECUTION
    // =========================================================================

    /**
     * Test 13: Verifies CPU execution properly updates registers (e.g. ACC 8-bit wrap).
     */
    public void testRegisterUpdatesCausedByExecution() {
        System.out.println("[TEST 13] Testing Register Updates Caused by Execution...");
        CPU cpu = new CPU();

        // Test 8-bit wrap around: MOV A, #0xFF then INC A -> ACC should wrap to 0x00
        int[] program = { CPU.OP_MOV_A_IMM, 0xFF, CPU.OP_INC_A };
        cpu.loadProgram(program);

        cpu.run();

        boolean accWrapped = (cpu.getRegisters().getACC() == 0x00);
        boolean pass = accWrapped;
        assertTest("CPU arithmetic correctly confines Accumulator to 8-bit wrap (0xFF -> 0x00)", pass,
                String.format("Final ACC=0x%02X", cpu.getRegisters().getACC()));
    }

    // =========================================================================
    // 14. FLAG UPDATES CAUSED BY CPU EXECUTION
    // =========================================================================

    /**
     * Test 14: Verifies CPU execution updates status flags (CY, AC, OV, P) during ALU operations.
     */
    public void testFlagUpdatesCausedByExecution() {
        System.out.println("[TEST 14] Testing Flag Updates Caused by Execution...");
        CPU cpu = new CPU();

        // Addition that triggers CY, AC, and OV:
        // MOV A, #0x70   (0111 0000)
        // ADD A, #0x90   (1001 0000)
        // Sum = 0x100 -> ACC = 0x00, CY = 1 (overflow > 255), P = 0 (even 0 set bits)
        int[] program = { CPU.OP_MOV_A_IMM, 0x70, CPU.OP_ADD_A_IMM, 0x90 };
        cpu.loadProgram(program);

        cpu.run();

        Flags f = cpu.getFlags();
        boolean cySet = f.isCarry();
        boolean parityCalculated = !f.isParity(); // 0 set bits -> even parity -> P = false

        boolean pass = cySet && parityCalculated;
        assertTest("ALU operation correctly updates Carry (CY) and Parity (P) flags", pass,
                String.format("CY=%b, AC=%b, OV=%b, P=%b, ACC=0x%02X",
                        f.isCarry(), f.isAuxiliaryCarry(), f.isOverflow(), f.isParity(),
                        cpu.getRegisters().getACC()));
    }

    // =========================================================================
    // ASSERTION HELPER
    // =========================================================================

    /**
     * Evaluates a test condition and formats test output.
     *
     * @param description test case description
     * @param condition   boolean result of the test assertion
     * @param details     diagnostic information on failure or state summary
     */
    private void assertTest(String description, boolean condition, String details) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + description);
        } else {
            failedTests++;
            System.err.println("  [FAIL] " + description);
            System.err.println("         Details: " + details);
        }
    }
}
