package memory;

public class Stack {
   private final DataMemory dataMemory;

    public Stack(DataMemory dataMemory) {
        this.dataMemory = dataMemory;
    }

    public int push(int sp, int value) {
        sp = (sp + 1) & 0xFF;
        dataMemory.write(sp, value);
        return sp;
    }

    public int pop(int sp, int[] poppedValueOut) {
        poppedValueOut[0] = dataMemory.read(sp);
        sp = (sp - 1) & 0xFF;
        return sp;
    }
}