package cpu;

import shared.CommandType;
import shared.IPCMessage;
import shared.SystemStateSnapshot;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CoreProcess {
    private static final int CORE_PORT = 9001;
    private static final int LOG_PORT = 9002;

    private final CPU cpu = new CPU();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private ObjectOutputStream logStream;

    public static void main(String[] args) {
        new CoreProcess().start();
    }

    public void start() {
        System.out.println("[CORE PROCESS] Initializing STC89C52 CPU Core...");
        
        // Connect to Logging Process IPC
        connectToLogger();

        // Start UI Listener Thread
        threadPool.submit(this::listenForUICommands);
    }
    @SuppressWarnings("resource")
    private void connectToLogger() {
        try {
            Socket logSocket = new Socket("127.0.0.1", LOG_PORT);
            logStream = new ObjectOutputStream(logSocket.getOutputStream());
            sendLog("[CORE] Connected to Logging Process.");
        } catch (IOException e) {
            System.err.println("[CORE ERROR] Could not connect to Logging Process: " + e.getMessage());
        }
    }

    private void listenForUICommands() {
        try (ServerSocket serverSocket = new ServerSocket(CORE_PORT)) {
            System.out.println("[CORE PROCESS] Listening for UI commands on port " + CORE_PORT);
            Socket clientSocket = serverSocket.accept();
            ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());

            while (true) {
                IPCMessage msg = (IPCMessage) in.readObject();
                SystemStateSnapshot snap = handleUICommand(msg);
                
                out.writeObject(new IPCMessage(msg.getCommand(), null, snap, null));
                out.flush();
            }
        } catch (Exception e) {
            System.err.println("[CORE IPC ERROR] UI communication error: " + e.getMessage());
        }
    }

    private synchronized SystemStateSnapshot handleUICommand(IPCMessage msg) {
        CommandType cmd = msg.getCommand();
        String stepLog = "";

        if (cmd == CommandType.LOAD_PROGRAM) {
            cpu.loadProgram(msg.getProgramLines());
            stepLog = "Program loaded into memory.";
            sendLog("[CORE] Program loaded into CPU Program Memory.");
        } else if (cmd == CommandType.RESET) {
            cpu.reset();
            stepLog = "CPU and Data Memory Reset.";
            sendLog("[CORE] CPU Reset performed.");
        } else if (cmd == CommandType.STEP) {
            if (!cpu.isHalted()) {
                String fetch = cpu.fetch();
                String decode = cpu.decode();
                String exec = cpu.execute();
                stepLog = String.format("%s\n%s\n%s", fetch, decode, exec);
                sendLog("[CORE STEP] " + exec.replaceAll("\n", " | "));
            } else {
                stepLog = "[PROGRAM HALTED]";
            }
        } else if (cmd == CommandType.RUN) {
            StringBuilder sb = new StringBuilder();
            while (!cpu.isHalted()) {
                String fetch = cpu.fetch();
                String decode = cpu.decode();
                String exec = cpu.execute();
                sb.append(String.format("%s\n%s\n%s\n", fetch, decode, exec));
            }
            stepLog = sb.toString();
            sendLog("[CORE RUN] Full execution loop completed.");
        }

        return createSnapshot(stepLog);
    }

    private SystemStateSnapshot createSnapshot(String stepLog) {
        SystemStateSnapshot snap = new SystemStateSnapshot();
        Registers r = cpu.getRegisters();
        Flags f = cpu.getFlags();

        snap.pc = r.getPc();
        snap.acc = r.getAcc();
        snap.b = r.getB();
        snap.sp = r.getSp();
        snap.dptr = r.getDptr();
        snap.flagsStr = f.toString();
        snap.queueStr = cpu.getFifoQueue().toString();
        snap.halted = cpu.isHalted();
        snap.lastStepLog = stepLog;

        System.arraycopy(cpu.getDataMemory().getRamData(), 0, snap.ramSnippet, 0, 32);
        return snap;
    }

    private void sendLog(String logText) {
        if (logStream != null) {
            try {
                logStream.writeObject(new IPCMessage(null, null, null, logText));
                logStream.flush();
            } catch (IOException e) {
                System.err.println("[CORE] Log transmission failed: " + e.getMessage());
            }
        }
    }
}
