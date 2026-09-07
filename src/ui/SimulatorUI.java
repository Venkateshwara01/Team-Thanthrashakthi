package ui;

import cpu.CPU;
import cpu.Flags;
import cpu.Registers;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class SimulatorUI extends JFrame {
    private final CPU cpu = new CPU();
    private final SimulationController controller = new SimulationController(cpu);

    private JTextArea codeArea;
    private JTextArea traceArea;
    private JLabel pcLabel, accLabel, bLabel, spLabel, dptrLabel, flagsLabel;

    public SimulatorUI() {
        setTitle("Team-Thanthrashakthi - STC89C52 Microcontroller Simulator");
        setSize(950, 650);
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
        JPanel rightPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        rightPanel.setBorder(BorderFactory.createTitledBorder("CPU Registers & Flags"));

        pcLabel = new JLabel("PC   : 0x0000");
        accLabel = new JLabel("ACC  : 0x00");
        bLabel = new JLabel("B    : 0x00");
        spLabel = new JLabel("SP   : 0x07");
        dptrLabel = new JLabel("DPTR : 0x0000");
        flagsLabel = new JLabel("FLAGS: CY:f AC:f OV:f P:f");

        for (JLabel lbl : Arrays.asList(pcLabel, accLabel, bLabel, spLabel, dptrLabel, flagsLabel)) {
            lbl.setFont(new Font("Monospaced", Font.BOLD, 12));
            rightPanel.add(lbl);
        }

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
            traceArea.setText("CPU Reset Complete.");
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
    }

    private void loadDemoFile() {
        try {
            File file = new File("programs/demo-program.txt");
            if (file.exists()) {
                codeArea.setText(new String(Files.readAllBytes(file.toPath())));
            } else {
                codeArea.setText("; Week 2 STC89C52 Demo Program\nMOV A, #50\nMOV B, #30\nADD A, B\nINC A\nANL A, B\nEND");
            }
        } catch (Exception e) {
            codeArea.setText("MOV A, #50\nMOV B, #30\nADD A, B\nEND");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulatorUI().setVisible(true));
    }
}
