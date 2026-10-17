package shared;

import java.io.Serializable;

public class SystemStateSnapshot implements Serializable {
    private static final long serialVersionUID = 1L;

    public int pc;
    public int acc;
    public int b;
    public int sp;
    public int dptr;
    public String flagsStr;
    public String queueStr;
    public boolean halted;
    public int[] ramSnippet = new int[32];
    public String lastStepLog;

    @Override
    public String toString() {
        return String.format("PC: 0x%04X | ACC: 0x%02X | B: 0x%02X | SP: 0x%02X | DPTR: 0x%04X",
                pc, acc, b, sp, dptr);
    }
}