package memory;

import instruction.Instruction;
import java.util.ArrayList;
import java.util.List;

public class ProgramMemory {
    private final List<Instruction> memory = new ArrayList<>();

    public void load(List<String> rawLines) {
        memory.clear();
        for (String line : rawLines) {
            String trimmed = line.trim();
            if (!trimmed.isEmpty() && !trimmed.startsWith(";")) {
                memory.add(new Instruction(trimmed));
            }
        }
    }

    public Instruction getInstruction(int address) {
        if (address >= 0 && address < memory.size()) {
            return memory.get(address);
        }
        return null;
    }

    public int size() {
        return memory.size();
    }
}