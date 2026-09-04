package cpu;

/**
 * Represents the CPU register set for an educational 8-bit microcontroller
 * simulator (8051 / STC89C52 style).
 * <p>
 * This class encapsulates the hardware registers, enforces 8-bit (0x00-0xFF)
 * and 16-bit (0x0000-0xFFFF) boundaries, validates values upon modification,
 * and provides byte-level accessors for compound registers like DPTR.
 * </p>
 */
public class Registers {

    // ==========================================
    // Register Definitions
    // ==========================================

    /**
     * Accumulator (ACC) - 8-bit Register (0x00 - 0xFF / 0 - 255).
     * Primary working register used for arithmetic, logical, data transfer,
     * and boolean operations.
     */
    private int acc;

    /**
     * B Register - 8-bit Register (0x00 - 0xFF / 0 - 255).
     * Secondary register primarily used in multiplication (MUL AB) and division (DIV AB)
     * operations, as well as general-purpose temporary storage.
     */
    private int b;

    /**
     * Program Counter (PC) - 16-bit Register (0x0000 - 0xFFFF / 0 - 65535).
     * Holds the memory address of the next instruction byte to fetch and execute.
     */
    private int pc;

    /**
     * Stack Pointer (SP) - 8-bit Register (0x00 - 0xFF / 0 - 255).
     * Holds the address of the current top of the stack in internal RAM.
     * In standard 8051 architecture, SP initializes to 0x07 upon reset.
     */
    private int sp;

    /**
     * Data Pointer (DPTR) - 16-bit Register (0x0000 - 0xFFFF / 0 - 65535).
     * Used to point to external data memory or code constants.
     * Logically consists of two 8-bit registers: DPH (high byte) and DPL (low byte).
     */
    private int dptr;

    // ==========================================
    // Constructors
    // ==========================================

    /**
     * Default constructor.
     * Initializes all registers to their standard hardware reset values:
     * ACC=0x00, B=0x00, PC=0x0000, SP=0x07, DPTR=0x0000.
     */
    public Registers() {
        reset();
    }

    /**
     * Parameterized constructor to initialize registers with specific custom values.
     *
     * @param acc  Initial Accumulator value (0x00 - 0xFF)
     * @param b    Initial B register value (0x00 - 0xFF)
     * @param pc   Initial Program Counter value (0x0000 - 0xFFFF)
     * @param sp   Initial Stack Pointer value (0x00 - 0xFF)
     * @param dptr Initial Data Pointer value (0x0000 - 0xFFFF)
     * @throws IllegalArgumentException if any value is outside its respective valid range
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
     * Resets all registers to their standard microcontroller power-on / hardware reset state:
     * <ul>
     *   <li>ACC  = 0x00</li>
     *   <li>B    = 0x00</li>
     *   <li>PC   = 0x0000 (starts execution from reset vector)</li>
     *   <li>SP   = 0x07 (8051 default; stack pushes start at RAM location 0x08)</li>
     *   <li>DPTR = 0x0000</li>
     * </ul>
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
     * Validates that a given value fits within an 8-bit unsigned integer range (0x00 to 0xFF / 0 to 255).
     *
     * @param value        The integer value to check
     * @param registerName The name of the register (used for descriptive error messages)
     * @return The validated 8-bit value
     * @throws IllegalArgumentException if the value is outside 0..255 (0x00..0xFF)
     */
    public int validate8Bit(int value, String registerName) {
        if (value < 0 || value > 0xFF) {
            throw new IllegalArgumentException(
                    registerName + " must be an 8-bit unsigned value (0x00 - 0xFF / 0 - 255). Received: " + value);
        }
        return value;
    }

    /**
     * Validates that a given value fits within a 16-bit unsigned integer range (0x0000 to 0xFFFF / 0 to 65535).
     *
     * @param value        The integer value to check
     * @param registerName The name of the register (used for descriptive error messages)
     * @return The validated 16-bit value
     * @throws IllegalArgumentException if the value is outside 0..65535 (0x0000..0xFFFF)
     */
    public int validate16Bit(int value, String registerName) {
        if (value < 0 || value > 0xFFFF) {
            throw new IllegalArgumentException(
                    registerName + " must be a 16-bit unsigned value (0x0000 - 0xFFFF / 0 - 65535). Received: " + value);
        }
        return value;
    }

    // ==========================================
    // Getters and Setters
    // ==========================================

    /**
     * Gets the Accumulator (ACC) value.
     * 
     * @return 8-bit value (0x00 - 0xFF)
     */
    public int getACC() {
        return acc;
    }

    /**
     * Sets the Accumulator (ACC) value with 8-bit range validation.
     * 
     * @param acc 8-bit value (0x00 - 0xFF)
     * @throws IllegalArgumentException if {@code acc} is not within 0x00 to 0xFF
     */
    public void setACC(int acc) {
        this.acc = validate8Bit(acc, "ACC");
    }

    /**
     * Gets the B register value.
     * 
     * @return 8-bit value (0x00 - 0xFF)
     */
    public int getB() {
        return b;
    }

    /**
     * Sets the B register value with 8-bit range validation.
     * 
     * @param b 8-bit value (0x00 - 0xFF)
     * @throws IllegalArgumentException if {@code b} is not within 0x00 to 0xFF
     */
    public void setB(int b) {
        this.b = validate8Bit(b, "B");
    }

    /**
     * Gets the Program Counter (PC) value.
     * 
     * @return 16-bit address (0x0000 - 0xFFFF)
     */
    public int getPC() {
        return pc;
    }

    /**
     * Sets the Program Counter (PC) value with 16-bit range validation.
     * 
     * @param pc 16-bit address (0x0000 - 0xFFFF)
     * @throws IllegalArgumentException if {@code pc} is not within 0x0000 to 0xFFFF
     */
    public void setPC(int pc) {
        this.pc = validate16Bit(pc, "PC");
    }

    /**
     * Gets the Stack Pointer (SP) value.
     * 
     * @return 8-bit internal RAM address (0x00 - 0xFF)
     */
    public int getSP() {
        return sp;
    }

    /**
     * Sets the Stack Pointer (SP) value with 8-bit range validation.
     * 
     * @param sp 8-bit internal RAM address (0x00 - 0xFF)
     * @throws IllegalArgumentException if {@code sp} is not within 0x00 to 0xFF
     */
    public void setSP(int sp) {
        this.sp = validate8Bit(sp, "SP");
    }

    /**
     * Gets the Data Pointer (DPTR) value.
     * 
     * @return 16-bit address (0x0000 - 0xFFFF)
     */
    public int getDPTR() {
        return dptr;
    }

    /**
     * Sets the Data Pointer (DPTR) value with 16-bit range validation.
     * 
     * @param dptr 16-bit address (0x0000 - 0xFFFF)
     * @throws IllegalArgumentException if {@code dptr} is not within 0x0000 to 0xFFFF
     */
    public void setDPTR(int dptr) {
        this.dptr = validate16Bit(dptr, "DPTR");
    }

    // ==========================================
    // Convenience Helper Methods for 8051 DPTR (DPH / DPL)
    // ==========================================

    /**
     * Gets the high byte of DPTR (DPH).
     * 
     * @return 8-bit high byte (0x00 - 0xFF)
     */
    public int getDPH() {
        return (dptr >> 8) & 0xFF;
    }

    /**
     * Sets the high byte of DPTR (DPH) while preserving the lower byte (DPL).
     * 
     * @param dph 8-bit value (0x00 - 0xFF)
     * @throws IllegalArgumentException if {@code dph} is not within 0x00 to 0xFF
     */
    public void setDPH(int dph) {
        validate8Bit(dph, "DPH");
        this.dptr = ((dph & 0xFF) << 8) | (this.dptr & 0x00FF);
    }

    /**
     * Gets the low byte of DPTR (DPL).
     * 
     * @return 8-bit low byte (0x00 - 0xFF)
     */
    public int getDPL() {
        return this.dptr & 0xFF;
    }

    /**
     * Sets the low byte of DPTR (DPL) while preserving the high byte (DPH).
     * 
     * @param dpl 8-bit value (0x00 - 0xFF)
     * @throws IllegalArgumentException if {@code dpl} is not within 0x00 to 0xFF
     */
    public void setDPL(int dpl) {
        validate8Bit(dpl, "DPL");
        this.dptr = (this.dptr & 0xFF00) | (dpl & 0xFF);
    }

    // ==========================================
    // String Representation
    // ==========================================

    /**
     * Returns a formatted hexadecimal string showing all register values.
     * 
     * @return Hexadecimal representation of the register set
     */
    @Override
    public String toString() {
        return String.format(
                "Registers [ACC=0x%02X, B=0x%02X, PC=0x%04X, SP=0x%02X, DPTR=0x%04X (DPH=0x%02X, DPL=0x%02X)]",
                acc, b, pc, sp, dptr, getDPH(), getDPL());
    }
}
