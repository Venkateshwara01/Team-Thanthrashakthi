package memory;

public class DataMemory {
    private final int[] ram = new int[256];

    public int read(int address) {
        address &= 0xFF;
        return ram[address];
    }

    public void write(int address, int value) {
        address &= 0xFF;
        ram[address] = value & 0xFF;
    }

    public void reset() {
        for (int i = 0; i < ram.length; i++) {
            ram[i] = 0;
        }
    }

    public int[] getRamData() {
        return ram;
    }
}
