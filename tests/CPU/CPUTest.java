package cpu;

/**
 * CPUTest
 *
 * Tests the CPU, Registers and Flags classes.
 *
 * Tested CPU operations:
 * 1. Reset
 * 2. Program loading
 * 3. Fetch
 * 4. Decode
 * 5. Execute
 * 6. Step cycle
 * 7. NOP
 * 8. MOV A,#immediate
 * 9. ADD A,#immediate
 * 10. ADD with Carry
 * 11. ADD signed overflow
 * 12. INC A
 * 13. DEC A
 * 14. CLR A
 *
 * Note:
 * This project follows the current CPU/Registers/Flags implementation.
 * The 8051-style flags available are:
 * CY - Carry
 * AC - Auxiliary Carry
 * OV - Overflow
 * P  - Parity
 *
 * There is NO Zero flag in the current Flags class.
 */
public class CPUTest {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       CPU TEST PROGRAM");
        System.out.println("======================================");

        testResetBehavior();
        testProgramLoading();
        testFetch();
        testDecode();
        testExecute();
        testStepCycle();
        testInstructionNOP();
        testInstructionMovImmediate();
        testInstructionAddImmediate();
        testInstructionAddWithCarry();
        testInstructionAddWithSignedOverflow();
        testInstructionIncA();
        testInstructionDecA();
        testInstructionClrA();

        System.out.println();
        System.out.println("======================================");
        System.out.println("             TEST SUMMARY");
        System.out.println("======================================");
        System.out.println("Tests Passed : " + passed);
        System.out.println("Tests Failed : " + failed);
        System.out.println("Total Tests  : " + (passed + failed));

        if (failed == 0) {
            System.out.println();
            System.out.println("ALL TESTS PASSED!");
        } else {
            System.out.println();
            System.out.println("SOME TESTS FAILED!");
        }
    }

    /**
     * Test 1:
     * Check whether CPU reset correctly initializes registers,
     * flags and CPU state.
     */
    private static void testResetBehavior() {

        System.out.println();
        System.out.println("Test 1: Reset Behavior");

        CPU cpu = new CPU();

        cpu.getRegisters().setACC(0x7F);
        cpu.getRegisters().setB(0x55);
        cpu.getRegisters().setPC(0x1000);
        cpu.getRegisters().setSP(0x20);
        cpu.getRegisters().setDPTR(0x1234);

        cpu.getFlags().setCY(true);
        cpu.getFlags().setAC(true);
        cpu.getFlags().setOV(true);
        cpu.getFlags().setP(true);

        cpu.reset();

        assertCondition(
                cpu.getRegisters().getACC() == 0,
                "ACC should reset to 0"
        );

        assertCondition(
                cpu.getRegisters().getB() == 0,
                "B should reset to 0"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 0,
                "PC should reset to 0"
        );

        assertCondition(
                cpu.getRegisters().getSP() == 0x07,
                "SP should reset to 0x07"
        );

        assertCondition(
                cpu.getRegisters().getDPTR() == 0,
                "DPTR should reset to 0"
        );

        assertCondition(
                !cpu.getFlags().isCY(),
                "CY should reset to false"
        );

        assertCondition(
                !cpu.getFlags().isAC(),
                "AC should reset to false"
        );

        assertCondition(
                !cpu.getFlags().isOV(),
                "OV should reset to false"
        );

        assertCondition(
                !cpu.getFlags().isP(),
                "P should reset to false"
        );

        assertCondition(
                cpu.getCurrentInstruction() == null,
                "Current instruction should reset to null"
        );

        assertCondition(
                !cpu.isHalted(),
                "CPU should not be halted after reset"
        );

        assertCondition(
                !cpu.isErrorState(),
                "CPU should not be in error state after reset"
        );
    }

    /**
     * Test 2:
     * Check whether a program can be loaded correctly.
     */
    private static void testProgramLoading() {

        System.out.println();
        System.out.println("Test 2: Program Loading");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_NOP,
                CPU.OP_INC_A,
                CPU.OP_DEC_A
        };

        cpu.loadProgram(program);

        assertCondition(
                cpu.getRegisters().getPC() == 0,
                "PC should start at 0 after program loading"
        );

        assertCondition(
                cpu.getProgramMemory()[0] == CPU.OP_NOP,
                "First instruction should be loaded correctly"
        );

        assertCondition(
                cpu.getProgramMemory()[1] == CPU.OP_INC_A,
                "Second instruction should be loaded correctly"
        );

        assertCondition(
                cpu.getProgramMemory()[2] == CPU.OP_DEC_A,
                "Third instruction should be loaded correctly"
        );

        assertCondition(
                !cpu.isHalted(),
                "CPU should not be halted after loading program"
        );

        assertCondition(
                !cpu.isErrorState(),
                "CPU should not be in error state after loading program"
        );
    }

    /**
     * Test 3:
     * Check FETCH operation.
     *
     * Program:
     * 0x04              -> INC A
     * 0x74 0x55         -> MOV A,#0x55
     */
    private static void testFetch() {

        System.out.println();
        System.out.println("Test 3: Fetch");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_INC_A,
                CPU.OP_MOV_A_IMM,
                0x55
        };

        cpu.loadProgram(program);

        // Fetch INC A
        cpu.fetch();

        CPU.Instruction instruction1 = cpu.getCurrentInstruction();

        assertCondition(
                instruction1 != null,
                "Fetched instruction should not be null"
        );

        assertCondition(
                instruction1.getOpcode() == CPU.OP_INC_A,
                "Fetched opcode should be INC A"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 1,
                "PC should become 1 after fetching INC A"
        );

        // Fetch MOV A,#0x55
        cpu.fetch();

        CPU.Instruction instruction2 = cpu.getCurrentInstruction();

        assertCondition(
                instruction2 != null,
                "Second fetched instruction should not be null"
        );

        assertCondition(
                instruction2.getOpcode() == CPU.OP_MOV_A_IMM,
                "Second opcode should be MOV A,#immediate"
        );

        assertCondition(
                instruction2.getOperand() == 0x55,
                "MOV immediate operand should be 0x55"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 3,
                "PC should become 3 after fetching MOV A,#0x55"
        );
    }

    /**
     * Test 4:
     * Check DECODE operation.
     */
    private static void testDecode() {

        System.out.println();
        System.out.println("Test 4: Decode");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                0x42
        };

        cpu.loadProgram(program);

        cpu.fetch();

        CPU.Instruction instruction = cpu.getCurrentInstruction();

        assertCondition(
                instruction != null,
                "Instruction should be fetched before decode"
        );

        cpu.decode();

        assertCondition(
                instruction.isValid(),
                "MOV instruction should be valid after decode"
        );

        assertCondition(
                instruction.getMnemonic() != null,
                "Mnemonic should not be null"
        );

        assertCondition(
                instruction.getMnemonic().contains("MOV A"),
                "Mnemonic should contain MOV A"
        );

        assertCondition(
                instruction.getMnemonic().contains("42"),
                "Mnemonic should contain operand 42"
        );
    }

    /**
     * Test 5:
     * Check EXECUTE operation.
     *
     * MOV A,#10 should place 10 into ACC.
     */
    private static void testExecute() {

        System.out.println();
        System.out.println("Test 5: Execute");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                10
        };

        cpu.loadProgram(program);

        cpu.fetch();
        cpu.decode();
        cpu.execute();

        assertCondition(
                cpu.getRegisters().getACC() == 10,
                "ACC should contain 10 after execution"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 2,
                "PC should be 2 after MOV A,#10"
        );
    }

    /**
     * Test 6:
     * Check the complete FETCH -> DECODE -> EXECUTE cycle
     * using step().
     */
    private static void testStepCycle() {

        System.out.println();
        System.out.println("Test 6: Step Cycle");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                25,
                CPU.OP_INC_A
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "First step should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 25,
                "ACC should become 25 after first step"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 2,
                "PC should become 2 after first step"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "Second step should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 26,
                "ACC should become 26 after INC A"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 3,
                "PC should become 3 after second step"
        );
    }

    /**
     * Test 7:
     * NOP should not modify ACC.
     */
    private static void testInstructionNOP() {

        System.out.println();
        System.out.println("Test 7: NOP Instruction");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_NOP
        };

        cpu.loadProgram(program);

        cpu.getRegisters().setACC(0x55);

        boolean result = cpu.step();

        assertCondition(
                result,
                "NOP should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x55,
                "NOP should not change ACC"
        );

        assertCondition(
                cpu.getRegisters().getPC() == 1,
                "PC should increase by 1 after NOP"
        );
    }

    /**
     * Test 8:
     * MOV A,#immediate.
     */
    private static void testInstructionMovImmediate() {

        System.out.println();
        System.out.println("Test 8: MOV A,#Immediate");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                0x55
        };

        cpu.loadProgram(program);

        boolean result = cpu.step();

        assertCondition(
                result,
                "MOV A,#immediate should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x55,
                "ACC should become 0x55"
        );

        /*
         * 0x55 = 01010101
         * Number of 1 bits = 4
         * 8051 parity flag P = 0 for even parity.
         */
        assertCondition(
                !cpu.getFlags().isP(),
                "Parity flag should be false for 0x55"
        );
    }

    /**
     * Test 9:
     * Basic ADD A,#immediate.
     *
     * 10 + 20 = 30
     */
    private static void testInstructionAddImmediate() {

        System.out.println();
        System.out.println("Test 9: ADD A,#Immediate");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                10,
                CPU.OP_ADD_A_IMM,
                20
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#10 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 10,
                "ACC should be 10"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "ADD A,#20 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 30,
                "10 + 20 should produce 30"
        );

        assertCondition(
                !cpu.getFlags().isCY(),
                "Carry flag should be false"
        );

        assertCondition(
                !cpu.getFlags().isAC(),
                "Auxiliary Carry should be false for 10 + 20"
        );

        assertCondition(
                !cpu.getFlags().isOV(),
                "Overflow flag should be false"
        );

        /*
         * 30 = 0x1E = 00011110
         * Number of 1 bits = 4
         * Therefore P = 0.
         */
        assertCondition(
                !cpu.getFlags().isP(),
                "Parity should be false for result 30"
        );
    }

    /**
     * Test 10:
     * ADD operation producing a carry.
     *
     * 0xFF + 0x01 = 0x00 with Carry = 1.
     */
    private static void testInstructionAddWithCarry() {

        System.out.println();
        System.out.println("Test 10: ADD with Carry");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                0xFF,
                CPU.OP_ADD_A_IMM,
                0x01
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#0xFF should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0xFF,
                "ACC should be 0xFF"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "ADD A,#0x01 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x00,
                "0xFF + 0x01 should wrap to 0x00"
        );

        assertCondition(
                cpu.getFlags().isCY(),
                "Carry flag should be true"
        );

        assertCondition(
                cpu.getFlags().isAC(),
                "Auxiliary Carry should be true"
        );

        assertCondition(
                !cpu.getFlags().isOV(),
                "Overflow should be false for 0xFF + 0x01"
        );

        assertCondition(
                !cpu.getFlags().isP(),
                "Parity should be false for 0x00"
        );
    }

    /**
     * Test 11:
     * Signed overflow.
     *
     * 0x7F + 0x01 = 0x80
     *
     * In signed 8-bit:
     * 127 + 1 = -128
     *
     * Therefore OV = 1.
     */
    private static void testInstructionAddWithSignedOverflow() {

        System.out.println();
        System.out.println("Test 11: ADD Signed Overflow");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                0x7F,
                CPU.OP_ADD_A_IMM,
                0x01
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#0x7F should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x7F,
                "ACC should be 0x7F"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "ADD A,#0x01 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x80,
                "0x7F + 0x01 should produce 0x80"
        );

        assertCondition(
                cpu.getFlags().isOV(),
                "Overflow flag should be true"
        );

        assertCondition(
                !cpu.getFlags().isCY(),
                "Carry should be false for 0x7F + 0x01"
        );

        /*
         * 0x80 = 10000000
         * Number of 1 bits = 1
         * Therefore P = 1.
         */
        assertCondition(
                cpu.getFlags().isP(),
                "Parity should be true for 0x80"
        );
    }

    /**
     * Test 12:
     * INC A.
     *
     * 5 + 1 = 6
     */
    private static void testInstructionIncA() {

        System.out.println();
        System.out.println("Test 12: INC A");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                5,
                CPU.OP_INC_A
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#5 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 5,
                "ACC should be 5"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "INC A should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 6,
                "ACC should become 6"
        );

        /*
         * 6 = 00000110
         * Number of 1 bits = 2
         * P = 0.
         */
        assertCondition(
                !cpu.getFlags().isP(),
                "Parity should be false for 6"
        );
    }

    /**
     * Test 13:
     * DEC A.
     *
     * 5 - 1 = 4
     */
    private static void testInstructionDecA() {

        System.out.println();
        System.out.println("Test 13: DEC A");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                5,
                CPU.OP_DEC_A
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#5 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 5,
                "ACC should be 5"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "DEC A should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 4,
                "ACC should become 4"
        );

        /*
         * 4 = 00000100
         * Number of 1 bits = 1
         * P = 1.
         */
        assertCondition(
                cpu.getFlags().isP(),
                "Parity should be true for 4"
        );
    }

    /**
     * Test 14:
     * CLR A.
     *
     * Any value should become 0.
     */
    private static void testInstructionClrA() {

        System.out.println();
        System.out.println("Test 14: CLR A");

        CPU cpu = new CPU();

        int[] program = {
                CPU.OP_MOV_A_IMM,
                0x55,
                CPU.OP_CLR_A
        };

        cpu.loadProgram(program);

        boolean result1 = cpu.step();

        assertCondition(
                result1,
                "MOV A,#0x55 should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0x55,
                "ACC should be 0x55"
        );

        boolean result2 = cpu.step();

        assertCondition(
                result2,
                "CLR A should execute successfully"
        );

        assertCondition(
                cpu.getRegisters().getACC() == 0,
                "ACC should become 0 after CLR A"
        );

        assertCondition(
                !cpu.getFlags().isP(),
                "Parity should be false for 0x00"
        );
    }

    /**
     * Common assertion method.
     */
    private static void assertCondition(boolean condition, String message) {

        if (condition) {
            System.out.println("[PASS] " + message);
            passed++;
        } else {
            System.out.println("[FAIL] " + message);
            failed++;
        }
    }
}