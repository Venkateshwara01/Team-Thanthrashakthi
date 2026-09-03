package cpu;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CPUConsole {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        List<Integer> programBytes = new ArrayList<>();
        int instructionCount = 0;

        System.out.println("======================================");
        System.out.println("      8-BIT CPU CONSOLE SIMULATOR");
        System.out.println("======================================");

        System.out.println();
        System.out.println("Supported instructions:");
        System.out.println("  NOP");
        System.out.println("  MOV A,#value");
        System.out.println("  ADD A,#value");
        System.out.println("  INC A");
        System.out.println("  DEC A");
        System.out.println("  CLR A");

        System.out.println();
        System.out.println("Value formats:");
        System.out.println("  Decimal : #10");
        System.out.println("  Hex     : #0x0A");

        System.out.println();
        System.out.println("Type RUN when you finish entering instructions.");
        System.out.println("Type EXIT to quit.");
        System.out.println("--------------------------------------");

        // ==============================
        // STEP 1: GET PROGRAM FROM USER
        // ==============================

        while (true) {

            System.out.print("Enter instruction: ");

            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("EXIT")) {
                System.out.println("Program terminated.");
                scanner.close();
                return;
            }

            if (input.equalsIgnoreCase("RUN")) {
                break;
            }

            if (input.isEmpty()) {
                continue;
            }

            try {

                int[] bytes = parseInstruction(input);

                for (int b : bytes) {
                    programBytes.add(b);
                }

                instructionCount++;

                System.out.print("  Machine code: ");

                for (int b : bytes) {
                    System.out.printf("%02X ", b);
                }

                System.out.println();

            } catch (IllegalArgumentException e) {

                System.out.println("  ERROR: " + e.getMessage());
                System.out.println("  Please enter a valid instruction.");

            }
        }

        // ==============================
        // CHECK WHETHER PROGRAM EXISTS
        // ==============================

        if (programBytes.isEmpty()) {

            System.out.println();
            System.out.println("No instructions entered.");
            scanner.close();
            return;
        }

        // ==============================
        // STEP 2: CREATE CPU
        // ==============================

        CPU cpu = new CPU();

        // Convert List<Integer> to int[]
        int[] program = new int[programBytes.size()];

        for (int i = 0; i < programBytes.size(); i++) {
            program[i] = programBytes.get(i);
        }

        // ==============================
        // STEP 3: LOAD PROGRAM
        // ==============================

        cpu.loadProgram(program);

        System.out.println();
        System.out.println("======================================");
        System.out.println("          PROGRAM LOADED");
        System.out.println("======================================");

        System.out.print("Program bytes: ");

        for (int b : program) {
            System.out.printf("%02X ", b);
        }

        System.out.println();

        System.out.printf("Start address: 0x%04X%n",
                cpu.getRegisters().getPC());

        System.out.println();
        System.out.println("======================================");
        System.out.println("        EXECUTION STARTED");
        System.out.println("======================================");

        // ==============================
        // STEP 4: EXECUTE INSTRUCTIONS
        // ==============================

        for (int i = 1; i <= instructionCount; i++) {

            System.out.println();
            System.out.println("---------- Instruction " + i + " ----------");

            boolean success = cpu.step();

            if (!success) {

                System.out.println("CPU execution stopped.");

                if (cpu.isErrorState()) {
                    System.out.println(
                            "Error: " + cpu.getLastErrorMessage()
                    );
                }

                break;
            }

            CPU.Instruction instruction =
                    cpu.getCurrentInstruction();

            System.out.println("Executed: " + instruction.getMnemonic());

            printCPUState(cpu);
        }

        // ==============================
        // FINAL CPU STATE
        // ==============================

        System.out.println();
        System.out.println("======================================");
        System.out.println("          FINAL CPU STATE");
        System.out.println("======================================");

        printCPUState(cpu);

        System.out.println();
        System.out.println("Execution completed.");
        System.out.println("======================================");

        scanner.close();
    }

    // ==========================================================
    // PARSE USER INSTRUCTION
    // ==========================================================

    private static int[] parseInstruction(String input) {

        String instruction = input.trim().toUpperCase();

        // --------------------------
        // NOP
        // --------------------------

        if (instruction.equals("NOP")) {

            return new int[] {
                    CPU.OP_NOP
            };
        }

        // --------------------------
        // INC A
        // --------------------------

        if (instruction.equals("INC A")) {

            return new int[] {
                    CPU.OP_INC_A
            };
        }

        // --------------------------
        // DEC A
        // --------------------------

        if (instruction.equals("DEC A")) {

            return new int[] {
                    CPU.OP_DEC_A
            };
        }

        // --------------------------
        // CLR A
        // --------------------------

        if (instruction.equals("CLR A")) {

            return new int[] {
                    CPU.OP_CLR_A
            };
        }

        // --------------------------
        // MOV A,#value
        // --------------------------

        if (instruction.startsWith("MOV A,#")) {

            String valueText =
                    instruction.substring(7).trim();

            int value = parseValue(valueText);

            return new int[] {
                    CPU.OP_MOV_A_IMM,
                    value
            };
        }

        // --------------------------
        // ADD A,#value
        // --------------------------

        if (instruction.startsWith("ADD A,#")) {

            String valueText =
                    instruction.substring(7).trim();

            int value = parseValue(valueText);

            return new int[] {
                    CPU.OP_ADD_A_IMM,
                    value
            };
        }

        throw new IllegalArgumentException(
                "Unknown instruction: " + input
        );
    }

    // ==========================================================
    // PARSE DECIMAL / HEX VALUE
    // ==========================================================

    private static int parseValue(String valueText) {

        if (valueText.isEmpty()) {
            throw new IllegalArgumentException(
                    "Missing value."
            );
        }

        int value;

        try {

            // Hexadecimal
            if (valueText.startsWith("0X")) {

                value = Integer.parseInt(
                        valueText.substring(2),
                        16
                );

            }
            // Decimal
            else {

                value = Integer.parseInt(valueText);
            }

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid value: " + valueText
            );
        }

        // CPU is 8-bit
        if (value < 0 || value > 255) {

            throw new IllegalArgumentException(
                    "Value must be between 0 and 255."
            );
        }

        return value;
    }

    // ==========================================================
    // PRINT CPU STATE
    // ==========================================================

    private static void printCPUState(CPU cpu) {

        Registers registers = cpu.getRegisters();
        Flags flags = cpu.getFlags();

        System.out.printf(
                "ACC : 0x%02X (%d)%n",
                registers.getACC(),
                registers.getACC()
        );

        System.out.printf(
                "B   : 0x%02X (%d)%n",
                registers.getB(),
                registers.getB()
        );

        System.out.printf(
                "PC  : 0x%04X%n",
                registers.getPC()
        );

        System.out.printf(
                "SP  : 0x%02X%n",
                registers.getSP()
        );

        System.out.printf(
                "DPTR: 0x%04X%n",
                registers.getDPTR()
        );

        System.out.println();
        System.out.println("Flags:");

        System.out.println(
                "  CY = " + flags.isCarry()
        );

        System.out.println(
                "  AC = " + flags.isAuxiliaryCarry()
        );

        System.out.println(
                "  OV = " + flags.isOverflow()
        );

        System.out.println(
                "  P  = " + flags.isParity()
        );
    }
}