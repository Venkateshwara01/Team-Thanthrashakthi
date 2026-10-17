package logging;

import shared.IPCMessage;

import java.io.FileWriter;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class LoggingProcess {
    private static final int LOG_PORT = 9002;
    private final BlockingQueue<String> logFCFSQueue = new ArrayBlockingQueue<>(200);

    public static void main(String[] args) {
        new LoggingProcess().start();
    }

    public void start() {
        System.out.println("[LOGGING PROCESS] Starting Async Log Daemon...");

        // Thread 1: Async FCFS Writer Thread
        new Thread(this::fileWriterWorker).start();

        // Thread 2: IPC Listener Thread
        try (ServerSocket serverSocket = new ServerSocket(LOG_PORT)) {
            System.out.println("[LOGGING PROCESS] Listening for log streams on port " + LOG_PORT);
            Socket socket = serverSocket.accept();
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                IPCMessage msg = (IPCMessage) in.readObject();
                if (msg.getLogText() != null) {
                    logFCFSQueue.put(msg.getLogText()); // Enforce FCFS ordering
                }
            }
        } catch (Exception e) {
            System.err.println("[LOGGER ERROR] IPC connection lost: " + e.getMessage());
        }
    }

   private void fileWriterWorker() {
        // Set to true if you want to print DEBUG step logs to console
        boolean showDebugInConsole = false; 

        try (PrintWriter writer = new PrintWriter(new FileWriter("simulation.log", true))) {
            while (true) {
                String logText = logFCFSQueue.take(); // FIFO poll
                String formattedLog = String.format("[%tF %<tT] %s", System.currentTimeMillis(), logText);
                
                // Always save every log (DEBUG, INFO, ERROR) to file
                writer.println(formattedLog);
                writer.flush();

                // Only print to terminal console if it's NOT a DEBUG log (keeps console clean)
                if (showDebugInConsole || !logText.contains("[DEBUG]")) {
                    System.out.println("[LOG] " + formattedLog);
                }
            }
        } catch (Exception e) {
            System.err.println("[LOGGER WRITE ERROR] " + e.getMessage());
        }
    } 
}
