package ui;

import cpu.CPU;
import cpu.Flags;
import cpu.Registers;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class SimulatorUI extends JFrame {
    private final CPU cpu = new CPU();
    private final SimulationController controller = new SimulationController(cpu);

    private JTextArea codeArea;
    private JTextArea traceArea;
    private JTextArea memoryArea;
    private JLabel pcLabel, accLabel, bLabel, spLabel, dptrLabel, flagsLabel, queueLabel;

    public SimulatorUI() {
        setTitle("Team-Thanthrashakthi - STC89C52 Microcontroller Simulator");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        loadDemoFile();
    }

    private void initUI() {
        // Left Panel: Assembly Input
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Assembly Source Editor"));
        codeArea = new JTextArea(15, 25);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        leftPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        // Control Buttons
        JPanel btnPanel = new JPanel(new GridLayout(1, 4, 5, 5));
        JButton loadBtn = new JButton("LOAD");
        JButton resetBtn = new JButton("RESET");
        JButton stepBtn = new JButton("STEP");
        JButton runBtn = new JButton("RUN");

        btnPanel.add(loadBtn); btnPanel.add(resetBtn);
        btnPanel.add(stepBtn); btnPanel.add(runBtn);
        leftPanel.add(btnPanel, BorderLayout.SOUTH);

        // Center Panel: Execution Trace
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Pipeline Trace (FETCH -> DECODE -> EXECUTE)"));
        traceArea = new JTextArea();
        traceArea.setEditable(false);
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(new JScrollPane(traceArea), BorderLayout.CENTER);

        // Right Panel: Registers & Flags State
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        JPanel regPanel = new JPanel(new GridLayout(7, 1, 3, 3));
        regPanel.setBorder(BorderFactory.createTitledBorder("CPU & Queue State"));
        
        pcLabel = new JLabel("PC   : 0x0000");
        accLabel = new JLabel("ACC  : 0x00");
        bLabel = new JLabel("B    : 0x00");
        spLabel = new JLabel("SP   : 0x07");
        dptrLabel = new JLabel("DPTR : 0x0000");
        flagsLabel = new JLabel("FLAGS: CY:f AC:f OV:f P:f");
        queueLabel = new JLabel("QUEUE: []");

        for (JLabel lbl : Arrays.asList(pcLabel, accLabel, bLabel, spLabel, dptrLabel, flagsLabel, queueLabel)) {
            lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            rightPanel.add(lbl);
        }
         rightPanel.add(regPanel, BorderLayout.NORTH);
        
        JPanel memPanel = new JPanel(new BorderLayout());
        memPanel.setBorder(BorderFactory.createTitledBorder("Data RAM & Stack (0x00 - 0x1F)"));
        memoryArea = new JTextArea(12, 25);
        memoryArea.setEditable(false);
        memoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        memPanel.add(new JScrollPane(memoryArea), BorderLayout.CENTER);
        rightPanel.add(memPanel, BorderLayout.CENTER);

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Button Listeners
        loadBtn.addActionListener(e -> {
            List<String> lines = Arrays.asList(codeArea.getText().split("\n"));
            controller.loadProgram(lines);
            traceArea.setText("Program Loaded Successfully.\nPress STEP or RUN.");
            updateDisplay();
        });

        resetBtn.addActionListener(e -> {
            controller.reset();
            traceArea.setText("CPU & Memory Reset Complete.");
            updateDisplay();
        });

        stepBtn.addActionListener(e -> executeStep());

        runBtn.addActionListener(e -> {
            while (!controller.isHalted()) {
                executeStep();
            }
        });
    }

    private void executeStep() {
        if (controller.isHalted()) {
            traceArea.append("\n[PROGRAM HALTED]");
            return;
        }

        String stepLog = controller.step();
        traceArea.append("\n==========================================\n" + stepLog);
        updateDisplay();
    }

    private void updateDisplay() {
        Registers r = cpu.getRegisters();
        Flags f = cpu.getFlags();

        pcLabel.setText(String.format("PC   : 0x%04X", r.getPc()));
        accLabel.setText(String.format("ACC  : 0x%02X", r.getAcc()));
        bLabel.setText(String.format("B    : 0x%02X", r.getB()));
        spLabel.setText(String.format("SP   : 0x%02X", r.getSp()));
        dptrLabel.setText(String.format("DPTR : 0x%04X", r.getDptr()));
        flagsLabel.setText("FLAGS: " + f.toString());
        queueLabel.setText("QUEUE: " + cpu.getFifoQueue().toString());

        StringBuilder sb = new StringBuilder();
        int[] ram = cpu.getDataMemory().getRamData();
        for (int i = 0; i < 32; i += 8) {
            sb.append(String.format("0x%02X: ", i));
            for (int j = 0; j < 8; j++) {
                sb.append(String.format("%02X ", ram[i + j]));
            }
            sb.append("\n");
        }
        memoryArea.setText(sb.toString());
    }

    private void loadDemoFile() {
        codeArea.setText(
            "; Week 3 Validation Script\n" +
            "MOV A, #10\n" +
            "ENQ A\n" +
            "MOV A, #20\n" +
            "ENQ A\n" +
            "MOV A, #30\n" +
            "ENQ A\n" +
            "DEQ\n" +
            "PUSH A\n" +
            "DEQ\n" +
            "PUSH A\n" +
            "POP B\n" +
            "POP A\n" +
            "END"
        );
    }
        
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulatorUI().setVisible(true));
    }
}
