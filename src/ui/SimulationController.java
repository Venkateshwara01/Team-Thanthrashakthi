package ui;

import cpu.CPU;
import java.util.List;

public class SimulationController {
    private final CPU cpu;

    public SimulationController(CPU cpu) {
        this.cpu = cpu;
    }

    public void loadProgram(List<String> lines) {
        cpu.loadProgram(lines);
    }

    public void reset() {
        cpu.reset();
    }

    public String step() {
        if (cpu.isHalted()) {
            return "[PROGRAM HALTED]";
        }
        String fetch = cpu.fetch();
        String decode = cpu.decode();
        String execute = cpu.execute();
        return String.format("%s\n%s\n%s\n", fetch, decode, execute);
    }

    public boolean isHalted() {
        return cpu.isHalted();
    }

    public CPU getCpu() {
        return cpu;
    }
}
