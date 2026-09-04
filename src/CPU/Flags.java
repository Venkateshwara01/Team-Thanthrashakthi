package cpu;

/**
 * Represents the CPU status flags for an educational 8-bit microcontroller
 * simulator (8051 / STC89C52 style).
 * <p>
 * This class encapsulates the status condition bits typically found in the
 * Program Status Word (PSW) register:
 * <ul>
 *   <li><b>CY (Carry Flag)</b>: Bit 7 of PSW</li>
 *   <li><b>AC (Auxiliary Carry Flag)</b>: Bit 6 of PSW</li>
 *   <li><b>OV (Overflow Flag)</b>: Bit 2 of PSW</li>
 *   <li><b>P (Parity Flag)</b>: Bit 0 of PSW</li>
 * </ul>
 * Arithmetic operations calculate CY, AC, and OV in {@code CPU.java} and update
 * these flags accordingly.
 * </p>
 */
public class Flags {

    /**
     * Carry Flag (CY):
     * Set to true when an arithmetic operation (such as ADD) generates a carry out
     * of the most significant bit (Bit 7), or when a borrow occurs during subtraction (SUBB).
     * Also acts as a 1-bit boolean accumulator for bit-addressable instructions.
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
     * In 8051 architecture, P is set to true (1) if the number of set '1' bits in the
     * accumulator is ODD, and false (0) if the number of set '1' bits is EVEN.
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
     * <p>
     * Only the lower 8 bits (Bit 0 through Bit 7) of the accumulator value are evaluated:
     * <ul>
     *   <li>{@code P = true} (1) if the number of set '1' bits is odd (Odd Parity).</li>
     *   <li>{@code P = false} (0) if the number of set '1' bits is even (Even Parity).</li>
     * </ul>
     * </p>
     *
     * @param accumulator the raw integer value of the accumulator (ACC)
     */
    public void updateParity(int accumulator) {
        int acc8 = accumulator & 0xFF;
        int count = Integer.bitCount(acc8);
        this.p = (count % 2 != 0);
    }

    // ==========================================
    // Getter and Setter Methods: Carry Flag (CY)
    // ==========================================

    /**
     * Checks if the Carry flag (CY) is set.
     *
     * @return true if CY is set, false otherwise
     */
    public boolean isCY() {
        return cy;
    }

    /**
     * Descriptive alias to check if the Carry flag (CY) is set.
     *
     * @return true if Carry flag is set, false otherwise
     */
    public boolean isCarry() {
        return cy;
    }

    /**
     * Sets the Carry flag (CY).
     *
     * @param cy new value for the Carry flag
     */
    public void setCY(boolean cy) {
        this.cy = cy;
    }

    /**
     * Descriptive alias to set the Carry flag (CY).
     *
     * @param carry new value for the Carry flag
     */
    public void setCarry(boolean carry) {
        this.cy = carry;
    }

    // ====================================================
    // Getter and Setter Methods: Auxiliary Carry Flag (AC)
    // ====================================================

    /**
     * Checks if the Auxiliary Carry flag (AC) is set.
     *
     * @return true if AC is set, false otherwise
     */
    public boolean isAC() {
        return ac;
    }

    /**
     * Descriptive alias to check if the Auxiliary Carry flag (AC) is set.
     *
     * @return true if Auxiliary Carry flag is set, false otherwise
     */
    public boolean isAuxiliaryCarry() {
        return ac;
    }

    /**
     * Sets the Auxiliary Carry flag (AC).
     *
     * @param ac new value for the Auxiliary Carry flag
     */
    public void setAC(boolean ac) {
        this.ac = ac;
    }

    /**
     * Descriptive alias to set the Auxiliary Carry flag (AC).
     *
     * @param ac new value for the Auxiliary Carry flag
     */
    public void setAuxiliaryCarry(boolean ac) {
        this.ac = ac;
    }

    // ==========================================
    // Getter and Setter Methods: Overflow Flag (OV)
    // ==========================================

    /**
     * Checks if the Overflow flag (OV) is set.
     *
     * @return true if OV is set, false otherwise
     */
    public boolean isOV() {
        return ov;
    }

    /**
     * Descriptive alias to check if the Overflow flag (OV) is set.
     *
     * @return true if Overflow flag is set, false otherwise
     */
    public boolean isOverflow() {
        return ov;
    }

    /**
     * Sets the Overflow flag (OV).
     *
     * @param ov new value for the Overflow flag
     */
    public void setOV(boolean ov) {
        this.ov = ov;
    }

    /**
     * Descriptive alias to set the Overflow flag (OV).
     *
     * @param ov new value for the Overflow flag
     */
    public void setOverflow(boolean ov) {
        this.ov = ov;
    }

    // ==========================================
    // Getter and Setter Methods: Parity Flag (P)
    // ==========================================

    /**
     * Checks if the Parity flag (P) is set.
     *
     * @return true if P is set, false otherwise
     */
    public boolean isP() {
        return p;
    }

    /**
     * Descriptive alias to check if the Parity flag (P) is set.
     *
     * @return true if Parity flag is set, false otherwise
     */
    public boolean isParity() {
        return p;
    }

    /**
     * Sets the Parity flag (P).
     *
     * @param p new value for the Parity flag
     */
    public void setP(boolean p) {
        this.p = p;
    }

    /**
     * Descriptive alias to set the Parity flag (P).
     *
     * @param p new value for the Parity flag
     */
    public void setParity(boolean p) {
        this.p = p;
    }

    // ==========================================
    // String Representation
    // ==========================================

    /**
     * Returns a formatted string displaying the status of all CPU flags.
     * Example: {@code Flags [CY=0, AC=0, OV=0, P=0]}
     *
     * @return String representation of the flags
     */
    @Override
    public String toString() {
        return String.format("Flags [CY=%d, AC=%d, OV=%d, P=%d]",
                cy ? 1 : 0,
                ac ? 1 : 0,
                ov ? 1 : 0,
                p ? 1 : 0);
    }
}
