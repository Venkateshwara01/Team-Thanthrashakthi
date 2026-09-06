package cpu;

import instruction.Instruction;
import memory.ProgramMemory;

public class CPU {
    private final Registers registers = new Registers();
    private final Flags flags = new Flags();
    private final ProgramMemory memory = new ProgramMemory();

    private Instruction currentInstruction;
    private boolean halted = false;

    public void loadProgram(java.util.List<String> codeLines) {
        memory.load(codeLines);
        reset();
    }

    public void reset() {
        registers.reset();
        flags.reset();
        halted = false;
        currentInstruction = null;
    }

    public String fetch() {
        if (registers.getPc() >= memory.size()) {
            halted = true;
            return "FETCH: End of Program Memory reached.";
        }
        currentInstruction = memory.getInstruction(registers.getPc());
        return String.format("FETCH ✓ [PC: 0x%04X -> '%s']", registers.getPc(), currentInstruction.getRawText());
    }

    public String decode() {
        if (currentInstruction == null || halted) {
            return "DECODE: No active instruction or CPU Halted.";
        }
        return String.format("DECODE ✓ [Mnemonic: %s | Op1: %s | Op2: %s]",
                currentInstruction.getMnemonic(), currentInstruction.getOperand1(), currentInstruction.getOperand2());
    }

    public String execute() {
        if (currentInstruction == null || halted) {
            return "EXECUTE: CPU Halted.";
        }

        String stateBefore = registers.toString();
        String mnemonic = currentInstruction.getMnemonic();
        String op1 = currentInstruction.getOperand1();
        String op2 = currentInstruction.getOperand2();

        int prevPC = registers.getPc();
        boolean jumpTaken = false;

        switch (mnemonic) {
            case "MOV":
                if (op1.equals("A") && op2.startsWith("#")) {
                    int val = parseVal(op2);
                    registers.setAcc(val);
                    flags.updateParity(val);
                } else if (op1.equals("B") && op2.startsWith("#")) {
                    int val = parseVal(op2);
                    registers.setB(val);
                } else if (op1.equals("A") && op2.equals("B")) {
                    registers.setAcc(registers.getB());
                    flags.updateParity(registers.getAcc());
                } else if (op1.equals("B") && op2.equals("A")) {
                    registers.setB(registers.getAcc());
                }
                break;

            case "ADD":
                if (op1.equals("A") && op2.equals("B")) {
                    int res = registers.getAcc() + registers.getB();
                    flags.setCy(res > 0xFF);
                    flags.setAc(((registers.getAcc() & 0x0F) + (registers.getB() & 0x0F)) > 0x0F);
                    registers.setAcc(res);
                    flags.updateParity(registers.getAcc());
                }
                break;

            case "SUBB":
                if (op1.equals("A") && op2.equals("B")) {
                    int borrow = flags.isCy() ? 1 : 0;
                    int res = registers.getAcc() - registers.getB() - borrow;
                    flags.setCy(res < 0);
                    registers.setAcc(res);
                    flags.updateParity(registers.getAcc());
                }
                break;

            case "ANL":
                if (op1.equals("A") && op2.equals("B")) {
                    registers.setAcc(registers.getAcc() & registers.getB());
                    flags.updateParity(registers.getAcc());
                }
                break;

            case "INC":
                if (op1.equals("A")) {
                    registers.setAcc(registers.getAcc() + 1);
                    flags.updateParity(registers.getAcc());
                }
                break;

            case "SJMP":
                int target = parseVal(op1);
                registers.setPc(target);
                jumpTaken = true;
                break;

            case "END":
                halted = true;
                break;

            default:
                throw new IllegalArgumentException("Unsupported mnemonic: " + mnemonic);
        }

        if (!jumpTaken && !halted) {
            registers.setPc(prevPC + 1);
        }

        return String.format("EXECUTE ✓\n  Before: %s\n  After : %s\n  Flags : %s",
                stateBefore, registers.toString(), flags.toString());
    }

    private int parseVal(String str) {
        str = str.replace("#", "").trim();
        if (str.startsWith("0X")) return Integer.parseInt(str.substring(2), 16);
        if (str.endsWith("H")) return Integer.parseInt(str.substring(0, str.length() - 1), 16);
        return Integer.parseInt(str, 10);
    }

    public Registers getRegisters() { return registers; }
    public Flags getFlags() { return flags; }
    public ProgramMemory getMemory() { return memory; }
    public boolean isHalted() { return halted; }
}