package cpu;

/**
 * CPU execution engine for an educational 8-bit microcontroller simulator (8051 / STC89C52 style).
 * <p>
 * Implements the classic Von Neumann Fetch-Decode-Execute instruction cycle:
 * <pre>
 *        PC (Program Counter)
 *                 ↓
 *              fetch()
 *                 ↓
 *        Current Instruction
 *                 ↓
 *              decode()
 *                 ↓
 *             execute()
 *                 ↓
 *     Registers / Flags Updated
 * </pre>
 * </p>
 *
 * <p><b>Team Boundary Notes (Student 1):</b></p>
 * <ul>
 *   <li><b>Student 1 (CPU & Execution Engine):</b> Maintains CPU state (Registers, Flags),
 *       executes the cycle (fetch, decode, execute, step, run), handles error/halt states.</li>
 *   <li><b>Student 2 (Memory & Stack):</b> Provides ProgramMemory / DataMemory.
 *       The CPU accesses memory through {@link #readMemoryByte(int)} and {@link #writeMemoryByte(int, int)}
 *       abstraction hooks so Student 2's implementation can be integrated seamlessly.</li>
 *   <li><b>Student 3 (Instruction Set):</b> Defines instruction opcodes, lengths, and mnemonics.
 *       The CPU uses the {@link Instruction} integration point and {@link #getInstructionLength(int)}
 *       to avoid hardcoding instruction sets across the CPU logic.</li>
 * </ul>
 */
public class CPU {

    // --- Standard 8051-style Opcode Constants (Integration Points with Student 3) ---
    public static final int OP_NOP          = 0x00; // NOP (1 byte)
    public static final int OP_INC_A        = 0x04; // INC A (1 byte)
    public static final int OP_DEC_A        = 0x14; // DEC A (1 byte)
    public static final int OP_ADD_A_IMM    = 0x24; // ADD A, #immediate (2 bytes)
    public static final int OP_MOV_A_IMM    = 0x74; // MOV A, #immediate (2 bytes)
    public static final int OP_CLR_A        = 0xE4; // CLR A (1 byte)

    // --- Default Memory Configuration (Abstracted for Student 2) ---
    public static final int MEMORY_SIZE = 65536; // 64 KB addressable memory (0x0000 - 0xFFFF)

    // --- Core CPU State ---
    private final Registers registers;
    private final Flags flags;
    private final int[] memory;
    private Instruction currentInstruction;
    private boolean halted;
    private boolean errorState;
    private String lastErrorMessage;

    /**
     * Minimal clean integration class representing the fetched and decoded instruction.
     * Serves as the clean bridge to Student 3's Instruction / InstructionSet implementation.
     */
    public static class Instruction {
        private final int opcode;
        private final int operand;
        private final int address;
        private final int length;
        private String mnemonic;
        private boolean valid;

        /**
         * Constructs a new Instruction instance.
         *
         * @param opcode  8-bit operation code
         * @param operand 8-bit immediate operand (or 0 if none)
         * @param address memory address where the instruction started
         * @param length  length of the instruction in bytes
         */
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

    // =========================================================================
    //                            CONSTRUCTORS
    // =========================================================================

    /**
     * Default constructor.
     * Initializes the CPU with new Registers, Flags, and internal memory space.
     */
    public CPU() {
        this(new Registers(), new Flags());
    }

    /**
     * Parameterized constructor using existing Registers and Flags instances.
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

    // =========================================================================
    //                        LIFECYCLE & RESET
    // =========================================================================

    /**
     * Resets the CPU to its initial power-on state:
     * <ul>
     *   <li>Registers: PC=0x0000, ACC=0x00, B=0x00, SP=0x07, DPTR=0x0000</li>
     *   <li>Flags: CY=false, AC=false, OV=false, P=false</li>
     *   <li>Cycle: Clears current instruction, halts, and error flags</li>
     * </ul>
     */
    public void reset() {
        registers.reset();
        flags.reset();
        currentInstruction = null;
        halted = false;
        errorState = false;
        lastErrorMessage = "";
    }

    // =========================================================================
    //                      FETCH - DECODE - EXECUTE CYCLE
    // =========================================================================

    /**
     * 1. FETCH PHASE:
     * <ul>
     *   <li>Reads the instruction opcode from memory at the current Program Counter (PC).</li>
     *   <li>Queries instruction length from instruction definitions (via {@link #getInstructionLength(int)}).</li>
     *   <li>Fetches any operand bytes required according to the instruction length.</li>
     *   <li>Advances the Program Counter (PC) appropriately according to total instruction length.</li>
     *   <li>Stores the fetched instruction details in {@code currentInstruction}.</li>
     *   <li>Safely detects and reports invalid or out-of-bounds memory addresses.</li>
     * </ul>
     */
    public void fetch() {
        if (halted || errorState) {
            return;
        }

        int pc = registers.getPC();
        int instructionAddress = pc;

        // Verify valid memory address
        if (!isValidAddress(pc)) {
            setError(String.format("Fetch Error: PC out of bounds (0x%04X)", pc));
            return;
        }

        int opcode = readMemoryByte(pc);
        int length = getInstructionLength(opcode);
        int operand = 0;

        // Advance PC past opcode
        int nextPC = (pc + 1) & 0xFFFF;

        // Fetch operand if instruction length is 2 bytes
        if (length == 2) {
            if (!isValidAddress(nextPC)) {
                setError(String.format("Fetch Error: Operand address out of bounds (0x%04X)", nextPC));
                return;
            }
            operand = readMemoryByte(nextPC);
            nextPC = (nextPC + 1) & 0xFFFF;
        }

        // Commit PC update
        registers.setPC(nextPC);

        // Store fetched instruction
        this.currentInstruction = new Instruction(opcode, operand, instructionAddress, length);
    }

    /**
     * 2. DECODE PHASE:
     * <ul>
     *   <li>Identifies the fetched instruction using defined opcode patterns.</li>
     *   <li>Populates human-readable mnemonic and operand representations.</li>
     *   <li>Flags unknown/unsupported opcodes with a descriptive error state.</li>
     * </ul>
     */
    public void decode() {
        if (currentInstruction == null || errorState || halted) {
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
     * <ul>
     *   <li>Executes the decoded instruction operation.</li>
     *   <li>Updates Registers and Flags (CY, AC, OV, P) as specified.</li>
     *   <li>Guarantees ACC and arithmetic results remain strictly within 8-bit unsigned range (0x00-0xFF).</li>
     *   <li>Recalculates parity whenever the Accumulator changes.</li>
     * </ul>
     */
    public void execute() {
        if (currentInstruction == null || !currentInstruction.isValid() || errorState || halted) {
            return;
        }

        int opcode = currentInstruction.getOpcode();
        int operand = currentInstruction.getOperand();

        switch (opcode) {
            case OP_NOP:
                // NOP: No Operation - registers and flags remain unchanged
                break;

            case OP_MOV_A_IMM:
                // MOV A, #immediate
                registers.setACC(operand);
                flags.updateParity(registers.getACC());
                break;

            case OP_ADD_A_IMM:
                // ADD A, #immediate
                executeAdd(registers.getACC(), operand);
                break;

            case OP_INC_A:
                // INC A: Increment accumulator with 8-bit modular wrap
                int incResult = (registers.getACC() + 1) & 0xFF;
                registers.setACC(incResult);
                flags.updateParity(incResult);
                break;

            case OP_DEC_A:
                // DEC A: Decrement accumulator with 8-bit modular wrap
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
     * Executes 8-bit unsigned addition: ACC = ACC + operand.
     * <p>
     * Updates:
     * <ul>
     *   <li><b>ACC:</b> Lower 8 bits of the sum.</li>
     *   <li><b>CY:</b> Set if sum &gt; 0xFF (carry out of bit 7).</li>
     *   <li><b>AC:</b> Set if carry out of lower nibble (bit 3 into bit 4).</li>
     *   <li><b>OV:</b> Set if two's complement signed overflow occurs.</li>
     *   <li><b>P:</b> Set according to 8051 odd parity rule on ACC.</li>
     * </ul>
     * </p>
     *
     * @param acc     current 8-bit Accumulator value
     * @param operand 8-bit immediate operand
     */
    private void executeAdd(int acc, int operand) {
        int sum = acc + operand;
        int result8 = sum & 0xFF;

        // 1. Store 8-bit result in ACC
        registers.setACC(result8);

        // 2. Carry (CY): overflow beyond 8 bits (Bit 7 carry)
        boolean carry = (sum > 0xFF);
        flags.setCarry(carry);

        // 3. Auxiliary Carry (AC): carry out of bit 3 (lower 4 bits)
        boolean ac = ((acc & 0x0F) + (operand & 0x0F)) > 0x0F;
        flags.setAuxiliaryCarry(ac);

        // 4. Signed Overflow (OV): signed operands of same sign produce opposite sign result
        boolean overflow = (((acc ^ result8) & (operand ^ result8) & 0x80) != 0);
        flags.setOverflow(overflow);

        // 5. Parity (P): 8051 odd parity rule
        flags.updateParity(result8);
    }

    // =========================================================================
    //                        CONTROL & EXECUTION FLOW
    // =========================================================================

    /**
     * Executes one complete instruction cycle (FETCH → DECODE → EXECUTE).
     *
     * @return {@code true} if cycle executed successfully, {@code false} if halted or encountered an error
     */
    public boolean step() {
        if (halted || errorState) {
            return false;
        }

        fetch();
        if (errorState || halted) return false;

        decode();
        if (errorState || halted) return false;

        execute();
        return !errorState && !halted;
    }

    /**
     * Executes instructions repeatedly up to {@code maxSteps} or until halted/error.
     *
     * @param maxSteps maximum instruction cycles to execute
     * @return number of steps actually executed
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
     * Runs the CPU repeatedly with a default safe threshold (1000 steps).
     *
     * @return number of steps actually executed
     */
    public int run() {
        return run(1000);
    }

    // =========================================================================
    //                   INSTRUCTION DEFINITION INTEGRATION POINT
    // =========================================================================

    /**
     * Obtains the instruction length in bytes based on opcode definition.
     * Designed as a clean delegation point for Student 3's instruction set definition.
     *
     * @param opcode 8-bit instruction opcode
     * @return instruction length in bytes (1, 2, or 3)
     */
    public static int getInstructionLength(int opcode) {
        switch (opcode) {
            case OP_MOV_A_IMM:
            case OP_ADD_A_IMM:
                return 2;
            case OP_NOP:
            case OP_INC_A:
            case OP_DEC_A:
            case OP_CLR_A:
            default:
                return 1;
        }
    }

    /**
     * Helper to verify if an opcode is a two-byte instruction.
     *
     * @param opcode 8-bit instruction opcode
     * @return true if 2-byte instruction, false otherwise
     */
    public static boolean isTwoByteOpcode(int opcode) {
        return getInstructionLength(opcode) == 2;
    }

    // =========================================================================
    //                     MEMORY ABSTRACTION HOOKS (STUDENT 2)
    // =========================================================================

    /**
     * Checks if a memory address is within valid 64KB addressable bounds (0x0000 - 0xFFFF).
     *
     * @param address memory address
     * @return true if valid, false otherwise
     */
    public boolean isValidAddress(int address) {
        return address >= 0 && address < MEMORY_SIZE;
    }

    /**
     * Reads a byte from memory.
     * Abstraction hook ready for connection to Student 2's ProgramMemory / Memory class.
     *
     * @param address memory address (0x0000 - 0xFFFF)
     * @return 8-bit unsigned value (0x00 - 0xFF)
     */
    public int readMemoryByte(int address) {
        if (isValidAddress(address)) {
            return memory[address] & 0xFF;
        }
        return 0;
    }

    /**
     * Writes a byte to memory.
     * Abstraction hook ready for connection to Student 2's ProgramMemory / Memory class.
     *
     * @param address memory address (0x0000 - 0xFFFF)
     * @param value   8-bit unsigned value (0x00 - 0xFF)
     */
    public void writeMemoryByte(int address, int value) {
        if (isValidAddress(address)) {
            memory[address] = value & 0xFF;
        }
    }

    /**
     * Alias for {@link #readMemoryByte(int)} for compatibility with existing tests.
     *
     * @param address memory address
     * @return 8-bit byte value
     */
    public int getMemoryByte(int address) {
        return readMemoryByte(address);
    }

    /**
     * Alias for {@link #writeMemoryByte(int, int)} for compatibility with existing tests.
     *
     * @param address memory address
     * @param value   8-bit byte value
     */
    public void setMemoryByte(int address, int value) {
        writeMemoryByte(address, value);
    }

    /**
     * Loads a program into memory at address 0x0000 and sets PC to 0x0000.
     *
     * @param program array of machine code bytes
     */
    public void loadProgram(int[] program) {
        loadProgram(0x0000, program);
    }

    /**
     * Loads a program into memory starting at {@code startAddress} and sets PC.
     *
     * @param startAddress start address offset (0x0000 - 0xFFFF)
     * @param program      array of machine code bytes
     */
    public void loadProgram(int startAddress, int[] program) {
        if (!isValidAddress(startAddress)) {
            throw new IllegalArgumentException(
                    String.format("Invalid start address: 0x%04X (Range: 0x0000 - 0x%04X)", startAddress, MEMORY_SIZE - 1));
        }
        if (program == null) {
            throw new IllegalArgumentException("Program array cannot be null.");
        }
        if (startAddress + program.length > MEMORY_SIZE) {
            throw new IllegalArgumentException(
                    String.format("Program of size %d bytes exceeds available memory starting at 0x%04X", program.length, startAddress));
        }

        for (int i = 0; i < program.length; i++) {
            writeMemoryByte(startAddress + i, program[i]);
        }

        registers.setPC(startAddress);
        this.halted = false;
        this.errorState = false;
        this.lastErrorMessage = "";
        this.currentInstruction = null;
    }

    /**
     * Returns direct reference to internal memory array.
     *
     * @return integer array representing memory
     */
    public int[] getMemory() {
        return memory;
    }

    /**
     * Returns direct reference to program memory array.
     *
     * @return integer array representing program memory
     */
    public int[] getProgramMemory() {
        return memory;
    }

    // =========================================================================
    //                        GETTERS, STATE & STATUS
    // =========================================================================

    /**
     * Sets an error condition, halting CPU execution and recording the message.
     *
     * @param message description of the error
     */
    private void setError(String message) {
        this.errorState = true;
        this.halted = true;
        this.lastErrorMessage = message;
    }

    /**
     * Gets the CPU registers set.
     *
     * @return Registers instance
     */
    public Registers getRegisters() {
        return registers;
    }

    /**
     * Gets the CPU status flags.
     *
     * @return Flags instance
     */
    public Flags getFlags() {
        return flags;
    }

    /**
     * Gets the currently fetched and decoded instruction.
     *
     * @return Instruction object or null if none
     */
    public Instruction getCurrentInstruction() {
        return currentInstruction;
    }

    /**
     * Checks if the CPU is in a halted state.
     *
     * @return true if halted, false otherwise
     */
    public boolean isHalted() {
        return halted;
    }

    /**
     * Halts CPU execution.
     */
    public void setHalted() {
        this.halted = true;
    }

    /**
     * Sets or unsets the halted state of the CPU.
     *
     * @param halted boolean indicating whether CPU is halted
     */
    public void setHalted(boolean halted) {
        this.halted = halted;
    }

    /**
     * Checks if the CPU has entered an error state.
     *
     * @return true if an error occurred, false otherwise
     */
    public boolean isErrorState() {
        return errorState;
    }

    /**
     * Gets the most recent error message, if any.
     *
     * @return error description or empty string
     */
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    /**
     * Returns formatted string representation of the current CPU state.
     *
     * @return formatted multi-line CPU status string
     */
    @Override
    public String toString() {
        return String.format(
                "CPU State:\n  %s\n  %s\n  Current Instruction: %s\n  Halted: %b, Error: %b %s",
                registers.toString(),
                flags.toString(),
                (currentInstruction != null ? currentInstruction.toString() : "None"),
                halted,
                errorState,
                (errorState ? ("(" + lastErrorMessage + ")") : ""));
    }
}
