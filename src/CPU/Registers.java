package cpu;

public class Registers {
    private int pc = 0x0000;
    private int acc = 0x00;
    private int b = 0x00;
    private int sp = 0x07;
    private int dptr = 0x0000;

    public void reset() {
        pc = 0x0000;
        acc = 0x00;
        b = 0x00;
        sp = 0x07;
        dptr = 0x0000;
    }

    public int getPc() { return pc; }
    public void setPc(int pc) { this.pc = pc & 0xFFFF; }

    public int getAcc() { return acc; }
    public void setAcc(int acc) { this.acc = acc & 0xFF; }

    public int getB() { return b; }
    public void setB(int b) { this.b = b & 0xFF; }

    public int getSp() { return sp; }
    public void setSp(int sp) { this.sp = sp & 0xFF; }

    public int getDptr() { return dptr; }
    public void setDptr(int dptr) { this.dptr = dptr & 0xFFFF; }

    @Override
    public String toString() {
        return String.format("PC: 0x%04X | ACC: 0x%02X | B: 0x%02X | SP: 0x%02X | DPTR: 0x%04X",
                pc, acc, b, sp, dptr);
    }
}