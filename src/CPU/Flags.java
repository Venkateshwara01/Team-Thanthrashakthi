package cpu;

/**
 * Flags represents the CPU status flags for an educational 8051/STC89C52-style
 * 8-bit microcontroller simulator (parts of the Program Status Word / PSW register).
 *
 * Implemented flags:
 * - CY (Carry Flag): Bit 7 of PSW
 * - AC (Auxiliary Carry Flag): Bit 6 of PSW
 * - OV (Overflow Flag): Bit 2 of PSW
 * - P  (Parity Flag): Bit 0 of PSW
 */
public class Flags {

    /**
     * Carry Flag (CY):
     * Set to true when an arithmetic operation (such as ADD) generates a carry out
     * of the most significant bit (Bit 7), or when a borrow occurs during subtraction (SUBB).
     * Also acts as a 1-bit boolean accumulator in bit-level instructions.
     */
    private boolean cy;

    /**
     * Auxiliary Carry Flag (AC):
     * Set to true when an arithmetic operation generates a carry out of the lower nibble
     * (Bit 3 into Bit 4), or a borrow from Bit 4 into Bit 3 during subtraction.
     * Used primarily for Binary Coded Decimal (BCD) operations (e.g., DA A).
     */
    private boolean ac;

    /**
     * Overflow Flag (OV):
     * Set to true when an arithmetic operation produces a result that exceeds the signed
     * 8-bit two's-complement range (-128 to +127).
     * Also set/cleared during multiplication (MUL AB) and division (DIV AB).
     */
    private boolean ov;

    /**
     * Parity Flag (P):
     * Reflects the parity of the Accumulator (ACC).
     * In 8051 architecture, P is set to true (1) if the number of '1' bits in the
     * accumulator is ODD, and false (0) if the number of '1' bits is EVEN.
     */
    private boolean p;

    /**
     * Default constructor.
     * Initializes all flags to their reset state (false / 0).
     */
    public Flags() {
        reset();
    }

    /**
     * Resets all CPU status flags to false (0).
     */
    public void reset() {
        this.cy = false;
        this.ac = false;
        this.ov = false;
        this.p = false;
    }

    /**
     * Updates the Parity flag (P) based on the current 8-bit value of the accumulator.
     * 
     * In 8051 systems:
     * - P = 1 (true) if the number of set bits ('1's) in ACC is odd (Odd Parity).
     * - P = 0 (false) if the number of set bits ('1's) in ACC is even.
     *
     * @param accumulator the current 8-bit value of the accumulator (ACC)
     */
    public void updateParity(int accumulator) {
        int acc8 = accumulator & 0xFF;
        int count = Integer.bitCount(acc8);
        this.p = (count % 2 != 0);
    }

    // ==========================================
    // Getter and Setter Methods: Carry Flag (CY)
    // ==========================================

    public boolean isCY() {
        return cy;
    }

    public boolean isCarry() {
        return cy;
    }

    public void setCY(boolean cy) {
        this.cy = cy;
    }

    public void setCarry(boolean carry) {
        this.cy = carry;
    }

    // ====================================================
    // Getter and Setter Methods: Auxiliary Carry Flag (AC)
    // ====================================================

    public boolean isAC() {
        return ac;
    }

    public boolean isAuxiliaryCarry() {
        return ac;
    }

    public void setAC(boolean ac) {
        this.ac = ac;
    }

    public void setAuxiliaryCarry(boolean ac) {
        this.ac = ac;
    }

    // ==========================================
    // Getter and Setter Methods: Overflow Flag (OV)
    // ==========================================

    public boolean isOV() {
        return ov;
    }

    public boolean isOverflow() {
        return ov;
    }

    public void setOV(boolean ov) {
        this.ov = ov;
    }

    public void setOverflow(boolean ov) {
        this.ov = ov;
    }

    // ==========================================
    // Getter and Setter Methods: Parity Flag (P)
    // ==========================================

    public boolean isP() {
        return p;
    }

    public boolean isParity() {
        return p;
    }

    public void setP(boolean p) {
        this.p = p;
    }

    public void setParity(boolean p) {
        this.p = p;
    }

    // ==========================================
    // String Representation
    // ==========================================

    @Override
    public String toString() {
        return String.format("Flags [CY=%d, AC=%d, OV=%d, P=%d]",
                cy ? 1 : 0,
                ac ? 1 : 0,
                ov ? 1 : 0,
                p ? 1 : 0);
    }
}
