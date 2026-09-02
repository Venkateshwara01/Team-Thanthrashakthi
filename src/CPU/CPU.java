package cpu;

/**
 * CPU implementation for an educational 8-bit microcontroller simulator (8051-style).
 * <p>
 * Demonstrates the classic Von Neumann Fetch-Decode-Execute instruction cycle:
 * <pre>
 *        PC (Program Counter)
 *                 ↓
 *              fetch()
 *                 ↓
 *       Current Instruction
 *                 ↓
 *              decode()
 *                 ↓
 *             execute()
 *                 ↓
 *     Registers / Flags Updated
 * </pre>
 * </p>
 *
 * <p><b>Supported Instruction Set:</b></p>
 * <ul>
 *   <li>{@code NOP} (0x00): No Operation (1 byte)</li>
 *   <li>{@code INC A} (0x04): Increment Accumulator (1 byte)</li>
 *   <li>{@code DEC A} (0x14): Decrement Accumulator (1 byte)</li>
 *   <li>{@code ADD A, #immediate} (0x24): Add 8-bit immediate value to Accumulator (2 bytes)</li>
 *   <li>{@code MOV A, #immediate} (0x74): Move 8-bit immediate value into Accumulator (2 bytes)</li>
 *   <li>{@code CLR A} (0xE4): Clear Accumulator to 0x00 (1 byte)</li>
 * </ul>
 */
public class CPU {

    // --- 8051-style Opcode Constants ---
    public static final int OP_NOP          = 0x00; // NOP (1 byte)
    public static final int OP_INC_A        = 0x04; // INC A (1 byte)
    public static final int OP_DEC_A        = 0x14; // DEC A (1 byte)
    public static final int OP_ADD_A_IMM    = 0x24; // ADD A, #immediate (2 bytes)
    public static final int OP_MOV_A_IMM    = 0x74; // MOV A, #immediate (2 bytes)
    public static final int OP_CLR_A        = 0xE4; // CLR A (1 byte)

    // --- Memory Configuration ---
    public static final int MEMORY_SIZE = 65536; // 64 KB addressable memory (0x0000 - 0xFFFF)

    // --- Core CPU Components & State ---
    private final Registers registers;
    private final Flags flags;
    private final int[] memory;
    private Instruction currentInstruction;
    private boolean halted;
    private boolean errorState;
    private String lastErrorMessage;

    /**
     * Encapsulates the instruction fetched and decoded during the cycle.
     */
    public static class Instruction {
        private final int opcode;
        private final int operand;
        private final int address;
        private final int length;
        private String mnemonic;
        private boolean valid;

        public Instruction(int opcode, int operand, int address, int length) {
            this.opcode = opcode & 0xFF;
            this.operand = operand & 0xFF;
            this.address = address & 0xFFFF;
            this.length = length;
            this.mnemonic = "";
            this.valid = false;
        }

        public int getOpcode() {
            return opcode;
        }

        public int getOperand() {
            return operand;
        }

        public int getAddress() {
            return address;
        }

        public int getLength() {
            return length;
        }

        public String getMnemonic() {
            return mnemonic;
        }

        public void setMnemonic(String mnemonic) {
            this.mnemonic = mnemonic;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        @Override
        public String toString() {
            if (mnemonic == null || mnemonic.isEmpty()) {
                if (length == 2) {
                    return String.format("[0x%04X] Opcode=0x%02X, Operand=0x%02X (Undecoded)", address, opcode, operand);
                }
                return String.format("[0x%04X] Opcode=0x%02X (Undecoded)", address, opcode);
            }
            if (length == 2) {
                return String.format("[0x%04X] 0x%02X 0x%02X : %s", address, opcode, operand, mnemonic);
            }
            return String.format("[0x%04X] 0x%02X      : %s", address, opcode, mnemonic);
        }
    }

    /**
     * Constructs a CPU instance with newly initialized registers, flags, and memory.
     */
    public CPU() {
        this.registers = new Registers();
        this.flags = new Flags();
        this.memory = new int[MEMORY_SIZE];
        this.currentInstruction = null;
        this.halted = false;
        this.errorState = false;
        this.lastErrorMessage = "";
        reset();
    }

    /**
     * Constructs a CPU instance with specific Registers and Flags instances.
     *
     * @param registers the Registers instance to use
     * @param flags     the Flags instance to use
     */
    public CPU(Registers registers, Flags flags) {
        this.registers = (registers != null) ? registers : new Registers();
        this.flags = (flags != null) ? flags : new Flags();
        this.memory = new int[MEMORY_SIZE];
        this.currentInstruction = null;
        this.halted = false;
        this.errorState = false;
        this.lastErrorMessage = "";
        reset();
    }

    /**
     * Resets the CPU to its initial power-on state:
     * - Resets all registers (PC = 0x0000, ACC = 0x00, B = 0x00, SP = 0x07, DPTR = 0x0000)
     * - Resets all 8051 flags (CY = false, AC = false, OV = false, P = false)
     * - Clears current instruction state and error flags.
     */
    public void reset() {
        registers.reset();
        flags.reset();
        currentInstruction = null;
        halted = false;
        errorState = false;
        lastErrorMessage = "";
    }

    /**
     * Loads a machine code program into memory starting at address 0x0000
     * and initializes the Program Counter (PC) to 0x0000.
     *
     * @param program Array of 8-bit machine code bytes (0x00 - 0xFF).
     */
    public void loadProgram(int[] program) {
        loadProgram(0x0000, program);
    }

    /**
     * Loads a machine code program into memory starting at the specified address,
     * and sets the Program Counter (PC) to the start address.
     *
     * @param startAddress Memory address offset to begin loading bytecode (0x0000 - 0xFFFF).
     * @param program      Array of 8-bit machine code bytes (0x00 - 0xFF).
     */
    public void loadProgram(int startAddress, int[] program) {
        if (startAddress < 0 || startAddress >= MEMORY_SIZE) {
            throw new IllegalArgumentException(
                String.format("Invalid start address: 0x%04X (Memory range: 0x0000 - 0x%04X)", startAddress, MEMORY_SIZE - 1)
            );
        }
        if (program == null) {
            throw new IllegalArgumentException("Program array cannot be null.");
        }
        if (startAddress + program.length > MEMORY_SIZE) {
            throw new IllegalArgumentException(
                String.format("Program of size %d bytes exceeds available memory starting at 0x%04X", program.length, startAddress)
            );
        }

        for (int i = 0; i < program.length; i++) {
            this.memory[startAddress + i] = program[i] & 0xFF;
        }

        registers.setPC(startAddress);
        this.halted = false;
        this.errorState = false;
        this.lastErrorMessage = "";
        this.currentInstruction = null;
    }

    // =========================================================================
    //                      FETCH - DECODE - EXECUTE CYCLE
    // =========================================================================

    /**
     * 1. FETCH PHASE:
     * - Uses the Program Counter (PC) to read the next instruction from memory.
     * - Validates memory bounds.
     * - For 2-byte instructions (MOV A, #imm or ADD A, #imm), fetches the immediate operand.
     * - Updates the Program Counter using existing PC methods from Registers.
     * - Stores fetched information into {@code currentInstruction}.
     */
    public void fetch() {
        int pc = registers.getPC();

        if (pc < 0 || pc >= MEMORY_SIZE) {
            setError(String.format("Fetch Error: PC out of bounds (0x%04X)", pc));
            return;
        }

        int opcode = memory[pc];
        int instructionAddress = pc;

        boolean isTwoByte = isTwoByteOpcode(opcode);
        int length = isTwoByte ? 2 : 1;
        int operand = 0;

        // Advance PC past the opcode
        registers.setPC((pc + 1) & 0xFFFF);

        if (isTwoByte) {
            int operandAddress = registers.getPC();
            if (operandAddress < 0 || operandAddress >= MEMORY_SIZE) {
                setError(String.format("Fetch Error: Operand address out of bounds (0x%04X)", operandAddress));
                return;
            }
            operand = memory[operandAddress];
            // Advance PC past the operand byte
            registers.setPC((operandAddress + 1) & 0xFFFF);
        }

        this.currentInstruction = new Instruction(opcode, operand, instructionAddress, length);
    }

    /**
     * 2. DECODE PHASE:
     * - Inspects the fetched opcode to identify the instruction.
     * - Assigns human-readable mnemonic string and marks instruction validity.
     */
    public void decode() {
        if (currentInstruction == null || errorState) {
            return;
        }

        int opcode = currentInstruction.getOpcode();
        int operand = currentInstruction.getOperand();

        switch (opcode) {
            case OP_NOP:
                currentInstruction.setMnemonic("NOP");
                currentInstruction.setValid(true);
                break;

            case OP_MOV_A_IMM:
                currentInstruction.setMnemonic(String.format("MOV A, #0x%02X", operand));
                currentInstruction.setValid(true);
                break;

            case OP_ADD_A_IMM:
                currentInstruction.setMnemonic(String.format("ADD A, #0x%02X", operand));
                currentInstruction.setValid(true);
                break;

            case OP_INC_A:
                currentInstruction.setMnemonic("INC A");
                currentInstruction.setValid(true);
                break;

            case OP_DEC_A:
                currentInstruction.setMnemonic("DEC A");
                currentInstruction.setValid(true);
                break;

            case OP_CLR_A:
                currentInstruction.setMnemonic("CLR A");
                currentInstruction.setValid(true);
                break;

            default:
                currentInstruction.setMnemonic(String.format("UNKNOWN (0x%02X)", opcode));
                currentInstruction.setValid(false);
                setError(String.format("Decode Error: Unrecognized opcode 0x%02X at address 0x%04X",
                    opcode, currentInstruction.getAddress()));
                break;
        }
    }

    /**
     * 3. EXECUTE PHASE:
     * - Executes the operation represented by {@code currentInstruction}.
     * - Updates Accumulator (ACC) using getACC() and setACC().
     * - Updates 8051 flags (CY, AC, OV, P) appropriately.
     */
    public void execute() {
        if (currentInstruction == null || !currentInstruction.isValid() || errorState) {
            return;
        }

        int opcode = currentInstruction.getOpcode();
        int operand = currentInstruction.getOperand();

        switch (opcode) {
            case OP_NOP:
                // No Operation: CPU does nothing during this cycle.
                break;

            case OP_MOV_A_IMM:
                // MOV A, #immediate: Load immediate operand into Accumulator
                registers.setACC(operand);
                flags.updateParity(registers.getACC());
                break;

            case OP_ADD_A_IMM:
                // ADD A, #immediate: Perform 8-bit addition with full flag updates
                executeAdd(registers.getACC(), operand);
                break;

            case OP_INC_A:
                // INC A: Increment accumulator by 1 (wrapped to 8-bit: 0x00 - 0xFF)
                int incResult = (registers.getACC() + 1) & 0xFF;
                registers.setACC(incResult);
                flags.updateParity(incResult);
                break;

            case OP_DEC_A:
                // DEC A: Decrement accumulator by 1 (wrapped to 8-bit: 0x00 - 0xFF)
                int decResult = (registers.getACC() - 1) & 0xFF;
                registers.setACC(decResult);
                flags.updateParity(decResult);
                break;

            case OP_CLR_A:
                // CLR A: Clear accumulator to 0x00
                registers.setACC(0x00);
                flags.updateParity(0x00);
                break;

            default:
                setError(String.format("Execute Error: Unsupported opcode 0x%02X", opcode));
                break;
        }
    }

    /**
     * Executes 8-bit addition (ADD A, operand) and updates:
     * <ul>
     *   <li>Accumulator (ACC): Lower 8 bits of sum.</li>
     *   <li>Carry Flag (CY): Set if addition exceeds 8 bits (&gt; 255 / carry out of bit 7).</li>
     *   <li>Auxiliary Carry (AC): Set if carry occurs from lower nibble (bit 3 to bit 4).</li>
     *   <li>Overflow Flag (OV): Set if 2's complement signed addition overflows (-128 to +127).</li>
     *   <li>Parity Flag (P): Updated according to 8051 odd parity rule.</li>
     * </ul>
     */
    private void executeAdd(int acc, int operand) {
        int sum = acc + operand;
        int result8 = sum & 0xFF;

        // 1. Update Accumulator
        registers.setACC(result8);

        // 2. Carry Flag (CY): Carry out of bit 7
        boolean carry = (sum > 0xFF);
        flags.setCarry(carry);

        // 3. Auxiliary Carry (AC): Carry out of bit 3 (lower 4 bits)
        boolean ac = ((acc & 0x0F) + (operand & 0x0F)) > 0x0F;
        flags.setAuxiliaryCarry(ac);

        // 4. Overflow Flag (OV):
        // Signed overflow occurs if operands of same sign produce a result of opposite sign.
        boolean overflow = (((acc ^ result8) & (operand ^ result8) & 0x80) != 0);
        flags.setOverflow(overflow);

        // 5. Parity Flag (P): 8051 odd-parity rule
        flags.updateParity(result8);
    }

    // =========================================================================
    //                        CONTROL & EXECUTION FLOW
    // =========================================================================

    /**
     * Performs a single complete instruction cycle: fetch() -> decode() -> execute().
     *
     * @return {@code true} if instruction executed successfully; {@code false} if halted or error.
     */
    public boolean step() {
        if (halted || errorState) {
            return false;
        }

        fetch();
        if (errorState) return false;

        decode();
        if (errorState) return false;

        execute();
        return !errorState;
    }

    /**
     * Runs the CPU continuously until it halts, encounters an error, or reaches maxSteps.
     *
     * @param maxSteps Maximum number of instructions to execute.
     * @return Total number of instructions executed.
     */
    public int run(int maxSteps) {
        int stepsExecuted = 0;
        while (stepsExecuted < maxSteps && !halted && !errorState) {
            boolean success = step();
            if (!success) {
                break;
            }
            stepsExecuted++;
        }
        return stepsExecuted;
    }

    /**
     * Runs the CPU with a default safe threshold of 1000 steps.
     *
     * @return Total number of instructions executed.
     */
    public int run() {
        return run(1000);
    }

    // =========================================================================
    //                          HELPER & GETTER METHODS
    // =========================================================================

    /**
     * Checks whether an opcode is a 2-byte instruction requiring an immediate operand.
     */
    private boolean isTwoByteOpcode(int opcode) {
        return opcode == OP_MOV_A_IMM || opcode == OP_ADD_A_IMM;
    }

    private void setError(String message) {
        this.errorState = true;
        this.halted = true;
        this.lastErrorMessage = message;
    }

    public Registers getRegisters() {
        return registers;
    }

    public Flags getFlags() {
        return flags;
    }

    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    public int[] getProgramMemory() {
        return memory;
    }

    public int[] getMemory() {
        return memory;
    }

    public int getMemoryByte(int address) {
        if (address >= 0 && address < MEMORY_SIZE) {
            return memory[address] & 0xFF;
        }
        return 0;
    }

    public void setMemoryByte(int address, int value) {
        if (address >= 0 && address < MEMORY_SIZE) {
            memory[address] = value & 0xFF;
        }
    }

    public boolean isHalted() {
        return halted;
    }

    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    public boolean isErrorState() {
        return errorState;
    }

    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public String toString() {
        return String.format(
            "CPU State:\n  %s\n  %s\n  Current Instruction: %s\n  Halted: %b, Error: %b %s",
            registers.toString(),
            flags.toString(),
            (currentInstruction != null ? currentInstruction.toString() : "None"),
            halted,
            errorState,
            (errorState ? ("(" + lastErrorMessage + ")") : "")
        );
    }
}
