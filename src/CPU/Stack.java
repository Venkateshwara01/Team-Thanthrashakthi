package memory;

public class Stack {
    private final int[] ram = new int[256];
    private int sp = 0x07;

    public void push(int value) {
        sp = (sp + 1) & 0xFF;
        ram[sp] = value & 0xFF;
    }

    public int pop() {
        int val = ram[sp];
        sp = (sp - 1) & 0xFF;
        return val;
    }

    public int getSp() { return sp; }
    public void setSp(int sp) { this.sp = sp & 0xFF; }
}