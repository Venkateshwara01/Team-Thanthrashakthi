package cpu;

/**
 * Represents the register set for an educational 8-bit microcontroller (8051 /
 * STC89C52 style).
 * 
 * This class encapsulates CPU registers, enforces 8-bit and 16-bit boundary
 * validation,
 * and provides standard accessors and reset functionality for instruction
 * execution.
 */
public class Registers {

    // ==========================================
    // Register Definitions
    // ==========================================

    /**
     * Accumulator (ACC) - 8-bit Register (0 - 255).
     * Primary working register used for arithmetic, logical, data transfer,
     * and boolean operations.
     */
    private int acc;

    /**
     * B Register - 8-bit Register (0 - 255).
     * Secondary register primarily used in multiplication (MUL AB) and division
     * (DIV AB)
     * operations, as well as general-purpose temporary storage.
     */
    private int b;

    /**
     * Program Counter (PC) - 16-bit Register (0 - 65535 / 0x0000 - 0xFFFF).
     * Holds the memory address of the next instruction to be fetched and executed.
     */
    private int pc;

    /**
     * Stack Pointer (SP) - 8-bit Register (0 - 255).
     * Holds the address of the current top of the stack in internal RAM.
     * In standard 8051 architecture, SP initializes to 0x07 upon reset.
     */
    private int sp;

    /**
     * Data Pointer (DPTR) - 16-bit Register (0 - 65535 / 0x0000 - 0xFFFF).
     * Used to point to external data memory or code constants.
     * Logically consists of two 8-bit registers: DPH (high byte) and DPL (low
     * byte).
     */
    private int dptr;

    // ==========================================
    // Constructors
    // ==========================================

    /**
     * Default constructor.
     * Initializes all registers to their default post-reset states.
     */
    public Registers() {
        reset();
    }

    /**
     * Parameterized constructor to initialize registers with specific values.
     *
     * @param acc  Initial Accumulator value (0 - 255)
     * @param b    Initial B register value (0 - 255)
     * @param pc   Initial Program Counter value (0 - 65535)
     * @param sp   Initial Stack Pointer value (0 - 255)
     * @param dptr Initial Data Pointer value (0 - 65535)
     */
    public Registers(int acc, int b, int pc, int sp, int dptr) {
        setACC(acc);
        setB(b);
        setPC(pc);
        setSP(sp);
        setDPTR(dptr);
    }

    // ==========================================
    // Core Simulator Operations
    // ==========================================

    /**
     * Resets all registers to their standard microcontroller power-on / hardware
     * reset state.
     * - ACC = 0x00
     * - B = 0x00
     * - PC = 0x0000 (starts execution from reset vector)
     * - SP = 0x07 (8051 default; stack pushes start at RAM location 0x08)
     * - DPTR = 0x0000
     */
    public void reset() {
        this.acc = 0x00;
        this.b = 0x00;
        this.pc = 0x0000;
        this.sp = 0x07;
        this.dptr = 0x0000;
    }

    // ==========================================
    // Validation Helpers
    // ==========================================

    /**
     * Validates that a given value fits within an 8-bit unsigned integer range (0
     * to 255).
     *
     * @param value        The integer value to check
     * @param registerName The name of the register (used for descriptive error
     *                     messages)
     * @return The validated value
     * @throws IllegalArgumentException if the value is outside 0..255
     */
    private int validate8Bit(int value, String registerName) {
        if (value < 0 || value > 255) {
            throw new IllegalArgumentException(
                    registerName + " must be an 8-bit unsigned value (0 - 255). Received: " + value);
        }
        return value;
    }

    /**
     * Validates that a given value fits within a 16-bit unsigned integer range (0
     * to 65535).
     *
     * @param value        The integer value to check
     * @param registerName The name of the register (used for descriptive error
     *                     messages)
     * @return The validated value
     * @throws IllegalArgumentException if the value is outside 0..65535
     */
    private int validate16Bit(int value, String registerName) {
        if (value < 0 || value > 65535) {
            throw new IllegalArgumentException(
                    registerName + " must be a 16-bit unsigned value (0 - 65535). Received: " + value);
        }
        return value;
    }

    // ==========================================
    // Getters and Setters
    // ==========================================

    /**
     * Gets the Accumulator (ACC) value.
     * 
     * @return 8-bit value (0 - 255)
     */
    public int getACC() {
        return acc;
    }

    /**
     * Sets the Accumulator (ACC) value with 8-bit range validation.
     * 
     * @param acc 8-bit value (0 - 255)
     */
    public void setACC(int acc) {
        this.acc = validate8Bit(acc, "ACC");
    }

    /**
     * Gets the B register value.
     * 
     * @return 8-bit value (0 - 255)
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the B register value with 8-bit range validation.
     * 
     * @param b 8-bit value (0 - 255)
     */
    public void setB(int b) {
        this.b = validate8Bit(b, "B");
    }

    /**
     * Gets the Program Counter (PC) value.
     * 
     * @return 16-bit address (0 - 65535)
     */
    public int getPC() {
        return pc;
    }

    /**
     * Sets the Program Counter (PC) value with 16-bit range validation.
     * 
     * @param pc 16-bit address (0 - 65535)
     */
    public void setPC(int pc) {
        this.pc = validate16Bit(pc, "PC");
    }

    /**
     * Gets the Stack Pointer (SP) value.
     * 
     * @return 8-bit internal RAM address (0 - 255)
     */
    public int getSP() {
        return sp;
    }

    /**
     * Sets the Stack Pointer (SP) value with 8-bit range validation.
     * 
     * @param sp 8-bit internal RAM address (0 - 255)
     */
    public void setSP(int sp) {
        this.sp = validate8Bit(sp, "SP");
    }

    /**
     * Gets the Data Pointer (DPTR) value.
     * 
     * @return 16-bit address (0 - 65535)
     */
    public int getDPTR() {
        return dptr;
    }

    /**
     * Sets the Data Pointer (DPTR) value with 16-bit range validation.
     * 
     * @param dptr 16-bit address (0 - 65535)
     */
    public void setDPTR(int dptr) {
        this.dptr = validate16Bit(dptr, "DPTR");
    }

    // ==========================================
    // Convenience Helper Methods for 8051 DPTR
    // ==========================================

    /**
     * Gets the high byte of DPTR (DPH).
     * 
     * @return 8-bit high byte (0 - 255)
     */
    public int getDPH() {
        return (dptr >> 8) & 0xFF;
    }

    /**
     * Sets the high byte of DPTR (DPH) while preserving DPL.
     * 
     * @param dph 8-bit value (0 - 255)
     */
    public void setDPH(int dph) {
        validate8Bit(dph, "DPH");
        this.dptr = (dph << 8) | (this.dptr & 0x00FF);
    }

    /**
     * Gets the low byte of DPTR (DPL).
     * 
     * @return 8-bit low byte (0 - 255)
     */
    public int getDPL() {
        return this.dptr & 0xFF;
    }

    /**
     * Sets the low byte of DPTR (DPL) while preserving DPH.
     * 
     * @param dpl 8-bit value (0 - 255)
     */
    public void setDPL(int dpl) {
        validate8Bit(dpl, "DPL");
        this.dptr = (this.dptr & 0xFF00) | dpl;
    }

    @Override
    public String toString() {
        return String.format(
                "Registers [ACC=0x%02X, B=0x%02X, PC=0x%04X, SP=0x%02X, DPTR=0x%04X (DPH=0x%02X, DPL=0x%02X)]",
                acc, b, pc, sp, dptr, getDPH(), getDPL());
    }
}
