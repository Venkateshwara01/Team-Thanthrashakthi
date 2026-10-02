package ui;

import shared.CommandType;
import shared.IPCMessage;
import shared.SystemStateSnapshot;

import javax.swing.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;

public class SimulatorUI extends JFrame {
    private static final String CORE_HOST = "127.0.0.1";
    private static final int CORE_PORT = 9001;

    private ObjectOutputStream outToCore;
    private ObjectInputStream inFromCore;

    private JTextArea codeArea;
    private JTextArea traceArea;
    private JTextArea memoryArea;
    private JTextArea stackArea;
    private JLabel pcLabel, accLabel, bLabel, spLabel, dptrLabel, flagsLabel, queueLabel;

    // Execution Controls & Timer
    private JButton loadBtn, resetBtn, stepBtn, runBtn, clearTraceBtn;
    private JSlider speedSlider;
    private Timer autoRunTimer;
    private boolean isRunning = false;
    private Object highlightTag = null;
    
    public SimulatorUI() {
        setTitle("Team-Thanthrashakthi - STC89C52 Microcontroller Simulator");
        setSize(1250, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        initUI();
        initAutoRunTimer();
        loadDemoFile();
        connectToCoreProcess();   
    }

    @SuppressWarnings("resource")
    private void connectToCoreProcess() {
        new Thread(() -> {
            try {
                Socket socket = new Socket(CORE_HOST, CORE_PORT);
                outToCore = new ObjectOutputStream(socket.getOutputStream());
                inFromCore = new ObjectInputStream(socket.getInputStream());
                SwingUtilities.invokeLater(() -> traceArea.setText("Connected to Core Process successfully.\nPress LOAD, STEP, or RUN."));
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> traceArea.setText("Failed to connect to Core Process. Make sure CoreProcess is running first!\nError: " + e.getMessage()));
            }
        }).start();
    }
    
    private void initUI() {
        // Left Panel: Assembly Input
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setBorder(BorderFactory.createTitledBorder("Assembly Source Editor"));
       
        codeArea = new JTextArea(18, 22);
        codeArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        leftPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        // Control Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        loadBtn = new JButton("LOAD");
        resetBtn = new JButton("RESET");
        stepBtn = new JButton("STEP");
        runBtn = new JButton("RUN");

        btnPanel.add(loadBtn); btnPanel.add(resetBtn);
        btnPanel.add(stepBtn); btnPanel.add(runBtn);

        // Speed Control Slider
        JPanel speedPanel = new JPanel(new BorderLayout(5, 5));
        speedPanel.setBorder(BorderFactory.createTitledBorder("Auto-Step Speed (ms)"));
        speedSlider = new JSlider(100, 2000, 500);
        speedSlider.setMajorTickSpacing(500);
        speedSlider.setPaintTicks(true);
        speedSlider.setPaintLabels(true);
        speedPanel.add(speedSlider, BorderLayout.CENTER);

        JPanel controlContainer = new JPanel();
        controlContainer.setLayout(new BoxLayout(controlContainer, BoxLayout.Y_AXIS));

        
        controlContainer.add(btnPanel);
        controlContainer.add(speedPanel);
        leftPanel.add(controlContainer, BorderLayout.SOUTH);

        
        // Center Panel: Execution Trace
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));
        centerPanel.setBorder(BorderFactory.createTitledBorder("Pipeline Trace (FETCH -> DECODE -> EXECUTE)"));
        
        traceArea = new JTextArea();
        traceArea.setEditable(false);
        traceArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        centerPanel.add(new JScrollPane(traceArea), BorderLayout.CENTER);
                
        clearTraceBtn = new JButton("CLEAR TRACE");
        centerPanel.add(clearTraceBtn, BorderLayout.SOUTH);
        
        // Right Panel: Registers & Flags State
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));

        // CPU & Queue State Box 
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
            regPanel.add(lbl);
        }
         rightPanel.add(regPanel, BorderLayout.NORTH);

        // Split Memory Container (RAM View + Stack Inspector)
        JPanel memoryContainer = new JPanel(new GridLayout(2, 1, 5, 5));

        JPanel ramPanel = new JPanel(new BorderLayout());
        ramPanel.setBorder(BorderFactory.createTitledBorder("Data RAM View (0x00 - 0x1F)"));
        memoryArea = new JTextArea(6, 25);
        memoryArea.setEditable(false);
        memoryArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        ramPanel.add(new JScrollPane(memoryArea), BorderLayout.CENTER);

         JPanel stackPanel = new JPanel(new BorderLayout());
        stackPanel.setBorder(BorderFactory.createTitledBorder("Stack Inspector (SP Tracking)"));
        stackArea = new JTextArea(6, 25);
        stackArea.setEditable(false);
        stackArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        stackPanel.add(new JScrollPane(stackArea), BorderLayout.CENTER);

        memoryContainer.add(ramPanel);
        memoryContainer.add(stackPanel);
        rightPanel.add(memoryContainer, BorderLayout.CENTER);
        
        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);

        // Event Listeners
        loadBtn.addActionListener(e -> {
            stopAutoRun();
            sendCommand(CommandType.LOAD_PROGRAM, Arrays.asList(codeArea.getText().split("\n")));
        });
        resetBtn.addActionListener(e -> {
            stopAutoRun();
            sendCommand(CommandType.RESET, null);
        });
        stepBtn.addActionListener(e -> {
            stopAutoRun();
            sendCommand(CommandType.STEP, null);
        });
        runBtn.addActionListener(e -> toggleAutoRun());
        clearTraceBtn.addActionListener(e -> traceArea.setText("Trace log cleared.\n"));

        speedSlider.addChangeListener(e -> {
            if (autoRunTimer != null) {
                autoRunTimer.setDelay(speedSlider.getValue());
            }
        });
    }

    private void initAutoRunTimer() {
        autoRunTimer = new Timer(speedSlider.getValue(), e -> sendCommand(CommandType.STEP, null));
    }

    private void toggleAutoRun() {
        if (isRunning) {
            stopAutoRun();
        } else {
            isRunning = true;
            runBtn.setText("PAUSE");
            loadBtn.setEnabled(false);
            stepBtn.setEnabled(false);
            resetBtn.setEnabled(false);
            autoRunTimer.start();
        }
    }

    private void stopAutoRun() {
        isRunning = false;
        if (autoRunTimer != null) autoRunTimer.stop();
        runBtn.setText("RUN");
        loadBtn.setEnabled(true);
        stepBtn.setEnabled(true);
        resetBtn.setEnabled(true);
    }

    private void sendCommand(CommandType cmd, List<String> lines) {
        if (outToCore == null) {
            JOptionPane.showMessageDialog(this, "Not connected to Core Process!");
            stopAutoRun();
            return;
        }

        new Thread(() -> {
            try {
                IPCMessage msg = new IPCMessage(cmd, lines, null, null);
                outToCore.writeObject(msg);
                outToCore.flush();

                IPCMessage response = (IPCMessage) inFromCore.readObject();
                SwingUtilities.invokeLater(() -> updateDisplay(response.getStateSnapshot()));
            } catch (Exception ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(this::stopAutoRun);
            }
        }).start();
    }

     private void updateDisplay(SystemStateSnapshot snap) {
        if (snap == null) return;

        // Auto-stop if CPU halted
        if (snap.halted && isRunning) {
            stopAutoRun();
        }

         // Registers
        pcLabel.setText(String.format("PC   : 0x%04X", snap.pc));
        accLabel.setText(String.format("ACC  : 0x%02X", snap.acc));
        bLabel.setText(String.format("B    : 0x%02X", snap.b));
        spLabel.setText(String.format("SP   : 0x%02X", snap.sp));
        dptrLabel.setText(String.format("DPTR : 0x%04X", snap.dptr));
        flagsLabel.setText("FLAGS: " + snap.flagsStr);
        queueLabel.setText("QUEUE: " + snap.queueStr);

        if (snap.lastStepLog != null && !snap.lastStepLog.isEmpty()) {
            traceArea.append("\n==========================================\n" + snap.lastStepLog);
            traceArea.setCaretPosition(traceArea.getDocument().getLength());
        }

        // Highlight Active Code Line in Editor
        highlightCodeLine(snap.pc);

        // Render Data RAM
        StringBuilder ramSb = new StringBuilder();
        for (int i = 0; i < 32; i += 8) {
            ramSb.append(String.format("0x%02X: ", i));
            for (int j = 0; j < 8; j++) {
                ramSb.append(String.format("%02X ", snap.ramSnippet[i + j]));
            }
            ramSb.append("\n");
        }
        memoryArea.setText(ramSb.toString());

        // Render Stack Inspector
        StringBuilder stackSb = new StringBuilder();
        stackSb.append(String.format("Current SP Pointer: 0x%02X\n", snap.sp));
        stackSb.append("-----------------------------\n");
        if (snap.sp <= 0x07) {
            stackSb.append("[Stack Empty] (Base SP: 0x07)");
        } else {
            stackSb.append("Addr    Value   Status\n");
            for (int addr = snap.sp; addr > 0x07; addr--) {
                int val = (addr < 32) ? snap.ramSnippet[addr] : 0;
                String marker = (addr == snap.sp) ? " <-- SP (TOP)" : "";
                stackSb.append(String.format("0x%02X:   0x%02X   %s\n", addr, val, marker));
            }
        }
        stackArea.setText(stackSb.toString());
    }

    private void highlightCodeLine(int lineIndex) {
        Highlighter highlighter = codeArea.getHighlighter();
        if (highlightTag != null) {
            highlighter.removeHighlight(highlightTag);
            highlightTag = null;
        }

        try {
            int startPos = codeArea.getLineStartOffset(lineIndex);
            int endPos = codeArea.getLineEndOffset(lineIndex);
            DefaultHighlighter.DefaultHighlightPainter painter = 
                    new DefaultHighlighter.DefaultHighlightPainter(new Color(255, 255, 180)); // Soft Yellow Highlight
            highlightTag = highlighter.addHighlight(startPos, endPos, painter);
        } catch (Exception ignored) {
            // Line out of range or empty
        }
    } 

    private void loadDemoFile() {
        codeArea.setText(
            "; Week 4 Multi-Process Validation Script\n" +
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
