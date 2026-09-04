package cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Interactive command-line demonstration console for the 8-bit Microcontroller CPU Simulator.
 * <p>
 * Demonstrates the CPU's FETCH → DECODE → EXECUTE cycle during presentations and lab reviews.
 * </p>
 *
 * <p><b>Team Boundary (Student 1):</b></p>
 * <ul>
 *   <li>This console coordinates user interactions and invokes CPU execution APIs.</li>
 *   <li>It does NOT contain internal fetch, decode, or execute logic.</li>
 *   <li>It does NOT define a separate instruction set (delegates to CPU/Student 3).</li>
 * </ul>
 */
public class CPUConsole {

    private final CPU cpu;
    private final Scanner scanner;
    private boolean isProgramLoaded;

    /**
     * Constructs a new CPUConsole instance.
     */
    public CPUConsole() {
        this.cpu = new CPU();
        this.scanner = new Scanner(System.in);
        this.isProgramLoaded = false;
    }

    /**
     * Main entry point to launch the CPU interactive console.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        CPUConsole console = new CPUConsole();
        console.start();
    }

    /**
     * Starts the main menu loop.
     */
    public void start() {
        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Select an option (1-7): ");
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleEnterCustomProgram();
                    break;
                case "2":
                    handleLoadDemoProgram();
                    break;
                case "3":
                    handleStep();
                    break;
                case "4":
                    handleRun();
                    break;
                case "5":
                    handleReset();
                    break;
                case "6":
                    displayCPUState();
                    break;
                case "7":
                    System.out.println("\nExiting CPU Simulator Console. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid selection. Please choose an option from 1 to 7.\n");
                    break;
            }
        }
    }

    /**
     * Prints the welcome banner for the console demo.
     */
    private void printBanner() {
        System.out.println("==================================================================");
        System.out.println("     8-BIT MICROCONTROLLER CPU SIMULATOR - CONSOLE DEMO           ");
        System.out.println("           Educational 8051 / STC89C52 CPU Component              ");
        System.out.println("==================================================================");
        System.out.println("Instruction Cycle: FETCH -> DECODE -> EXECUTE\n");
    }

    /**
     * Displays available menu options.
     */
    private void printMenu() {
        System.out.println("------------------------------------------------------------------");
        System.out.println(" [1] Enter & Load Custom Machine Code Program");
        System.out.println(" [2] Load Standard Preset Demo Program");
        System.out.println(" [3] Execute Single Instruction Cycle [step()]");
        System.out.println(" [4] Run Multiple Instruction Cycles  [run(n)]");
        System.out.println(" [5] Reset CPU State                  [reset()]");
        System.out.println(" [6] Display Current CPU State & Flags");
        System.out.println(" [7] Exit");
        System.out.println("------------------------------------------------------------------");
    }

    /**
     * Allows the user to enter machine code bytes in hex or decimal.
     */
    private void handleEnterCustomProgram() {
        System.out.println("\nEnter machine code bytes separated by spaces or commas.");
        System.out.println("Prefix with '0x' for hexadecimal (e.g., 0x74 0x50 0x24 0x40 0x04 0xE4 0x00)");
        System.out.println("or enter plain hex/dec values (e.g., 74 50 24 40 04):");
        System.out.print("> ");

        String line = scanner.nextLine().trim();
        if (line.isEmpty()) {
            System.out.println("[!] No input provided. Operation cancelled.\n");
            return;
        }

        try {
            int[] bytecode = parseByteString(line);
            if (bytecode.length == 0) {
                System.out.println("[!] No valid bytes parsed.\n");
                return;
            }

            cpu.loadProgram(bytecode);
            isProgramLoaded = true;
            System.out.printf("[✓] Successfully loaded %d bytes into CPU memory starting at address 0x0000.\n\n", bytecode.length);
            displayCPUState();
        } catch (NumberFormatException e) {
            System.out.println("[!] Error parsing input: " + e.getMessage());
            System.out.println("Please provide valid byte values between 0x00 and 0xFF.\n");
        } catch (IllegalArgumentException e) {
            System.out.println("[!] Error loading program: " + e.getMessage() + "\n");
        }
    }

    /**
     * Loads a preconfigured sample demonstration program.
     */
    private void handleLoadDemoProgram() {
        // Preset assembly demonstration:
        // 1. MOV A, #0x50  (0x74, 0x50) -> ACC = 0x50
        // 2. ADD A, #0x40  (0x24, 0x40) -> ACC = 0x90, OV = 1
        // 3. INC A         (0x04)       -> ACC = 0x91, P = 1
        // 4. DEC A         (0x14)       -> ACC = 0x90, P = 0
        // 5. ADD A, #0x80  (0x24, 0x80) -> ACC = 0x10, CY = 1
        // 6. CLR A         (0xE4)       -> ACC = 0x00, P = 0
        // 7. NOP           (0x00)       -> No Operation
        int[] demoProgram = {
            CPU.OP_MOV_A_IMM, 0x50,
            CPU.OP_ADD_A_IMM, 0x40,
            CPU.OP_INC_A,
            CPU.OP_DEC_A,
            CPU.OP_ADD_A_IMM, 0x80,
            CPU.OP_CLR_A,
            CPU.OP_NOP
        };

        cpu.loadProgram(demoProgram);
        isProgramLoaded = true;

        System.out.println("\n[✓] Loaded Standard 8051 Demo Program (7 instructions, 10 bytes):");
        System.out.println("    1. MOV A, #0x50   (Set ACC = 0x50)");
        System.out.println("    2. ADD A, #0x40   (Add 0x40 -> Signed Overflow OV = 1)");
        System.out.println("    3. INC A          (Increment ACC -> ACC = 0x91, Odd Parity P = 1)");
        System.out.println("    4. DEC A          (Decrement ACC -> ACC = 0x90, Even Parity P = 0)");
        System.out.println("    5. ADD A, #0x80   (Add 0x80 -> Carry CY = 1)");
        System.out.println("    6. CLR A          (Clear ACC to 0x00)");
        System.out.println("    7. NOP            (No-op / end)\n");

        displayCPUState();
    }

    /**
     * Executes one instruction cycle using cpu.step().
     */
    private void handleStep() {
        if (!isProgramLoaded) {
            System.out.println("\n[!] Warning: No program has been loaded into memory yet.");
            System.out.println("    Please load a program first (Option 1 or 2).\n");
            return;
        }

        if (cpu.isHalted()) {
            System.out.println("\n[!] CPU is HALTED. Reset the CPU (Option 5) or load a new program to continue.\n");
            return;
        }

        if (cpu.isErrorState()) {
            System.out.println("\n[!] CPU is in ERROR state: " + cpu.getLastErrorMessage() + "\n");
            return;
        }

        int pcBefore = cpu.getRegisters().getPC();
        System.out.printf("\n>>> Executing step() at PC=0x%04X ...\n", pcBefore);

        boolean success = cpu.step();

        if (success) {
            System.out.println("[✓] Instruction Cycle Completed Successfully.");
        } else {
            if (cpu.isErrorState()) {
                System.out.println("[✗] Instruction Cycle Failed with ERROR:");
                System.out.println("    " + cpu.getLastErrorMessage());
            } else if (cpu.isHalted()) {
                System.out.println("[*] CPU Halted.");
            }
        }

        displayCPUState();
    }

    /**
     * Executes multiple instructions using cpu.run(maxSteps).
     */
    private void handleRun() {
        if (!isProgramLoaded) {
            System.out.println("\n[!] Warning: No program loaded. Please load a program first.\n");
            return;
        }

        if (cpu.isHalted() || cpu.isErrorState()) {
            System.out.println("\n[!] CPU cannot run while halted or in error state. Reset first (Option 5).\n");
            return;
        }

        System.out.print("Enter maximum number of steps to run [default=100]: ");
        String line = scanner.nextLine().trim();
        int maxSteps = 100;
        if (!line.isEmpty()) {
            try {
                maxSteps = Integer.parseInt(line);
                if (maxSteps <= 0) {
                    System.out.println("[!] Number of steps must be greater than 0. Defaulting to 100.");
                    maxSteps = 100;
                }
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number. Defaulting to 100 steps.");
                maxSteps = 100;
            }
        }

        System.out.printf("\n>>> Running up to %d instruction cycles ...\n", maxSteps);
        int stepsExecuted = cpu.run(maxSteps);
        System.out.printf("[✓] Completed %d step(s).\n", stepsExecuted);

        if (cpu.isErrorState()) {
            System.out.println("[✗] Execution stopped due to ERROR: " + cpu.getLastErrorMessage());
        } else if (cpu.isHalted()) {
            System.out.println("[*] Execution stopped: CPU is HALTED.");
        }

        displayCPUState();
    }

    /**
     * Resets the CPU state.
     */
    private void handleReset() {
        cpu.reset();
        isProgramLoaded = false;
        System.out.println("\n[✓] CPU reset to power-on defaults (PC=0x0000, ACC=0x00, B=0x00, SP=0x07, DPTR=0x0000).\n");
        displayCPUState();
    }

    /**
     * Displays a clean formatted snapshot of the CPU state.
     */
    public void displayCPUState() {
        Registers reg = cpu.getRegisters();
        Flags flags = cpu.getFlags();
        CPU.Instruction instr = cpu.getCurrentInstruction();

        System.out.println("========================== CPU STATE ==========================");
        System.out.println(" Registers:");
        System.out.printf("   PC   : 0x%04X (%d)\n", reg.getPC(), reg.getPC());
        System.out.printf("   ACC  : 0x%02X   (%d)\n", reg.getACC(), reg.getACC());
        System.out.printf("   B    : 0x%02X   (%d)\n", reg.getB(), reg.getB());
        System.out.printf("   SP   : 0x%02X   (Default: 0x07)\n", reg.getSP());
        System.out.printf("   DPTR : 0x%04X [DPH=0x%02X, DPL=0x%02X]\n", reg.getDPTR(), reg.getDPH(), reg.getDPL());

        System.out.println(" Status Flags (PSW):");
        System.out.printf("   CY (Carry)           : %d (%b)\n", flags.isCarry() ? 1 : 0, flags.isCarry());
        System.out.printf("   AC (Auxiliary Carry) : %d (%b)\n", flags.isAuxiliaryCarry() ? 1 : 0, flags.isAuxiliaryCarry());
        System.out.printf("   OV (Overflow)        : %d (%b)\n", flags.isOverflow() ? 1 : 0, flags.isOverflow());
        System.out.printf("   P  (Parity)          : %d (%b) [Odd Parity on ACC]\n", flags.isParity() ? 1 : 0, flags.isParity());

        System.out.println(" Execution State:");
        System.out.printf("   Halted       : %b\n", cpu.isHalted());
        System.out.printf("   Error State  : %b\n", cpu.isErrorState());
        if (cpu.isErrorState()) {
            System.out.printf("   Error Message: %s\n", cpu.getLastErrorMessage());
        }

        System.out.println(" Last Instruction:");
        if (instr != null) {
            System.out.printf("   Address  : 0x%04X\n", instr.getAddress());
            System.out.printf("   Opcode   : 0x%02X\n", instr.getOpcode());
            if (instr.getLength() == 2) {
                System.out.printf("   Operand  : 0x%02X\n", instr.getOperand());
            }
            System.out.printf("   Mnemonic : %s\n", instr.getMnemonic());
            System.out.printf("   Valid    : %b\n", instr.isValid());
        } else {
            System.out.println("   (No instruction executed yet)");
        }
        System.out.println("===============================================================\n");
    }

    /**
     * Parses a string of space/comma separated hex or decimal bytes.
     *
     * @param input the input string from user
     * @return array of parsed 8-bit byte integers
     */
    private int[] parseByteString(String input) {
        String[] tokens = input.split("[,\\s]+");
        List<Integer> list = new ArrayList<>();

        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) {
                continue;
            }

            int val;
            if (token.startsWith("0x") || token.startsWith("0X")) {
                val = Integer.parseInt(token.substring(2), 16);
            } else if (token.matches("^[0-9a-fA-F]{2}$") && token.matches(".*[a-fA-F].*")) {
                val = Integer.parseInt(token, 16);
            } else {
                try {
                    val = Integer.parseInt(token);
                } catch (NumberFormatException e) {
                    val = Integer.parseInt(token, 16);
                }
            }

            if (val < 0 || val > 0xFF) {
                throw new IllegalArgumentException("Byte value out of range (0x00 - 0xFF): " + token);
            }
            list.add(val);
        }

        int[] result = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            result[i] = list.get(i);
        }
        return result;
    }
}
