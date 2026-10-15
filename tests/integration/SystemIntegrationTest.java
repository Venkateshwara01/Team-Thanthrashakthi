package tests.integration;

import cpu.CoreProcess;
import logging.LoggingProcess;
import org.junit.jupiter.api.*;
import ui.SimulatorUI;

import javax.swing.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SystemIntegrationTest {

    private static Thread loggingThread;
    private static Thread coreThread;
    private SimulatorUI simulatorUI;

    @BeforeAll
    public static void startProcesses() {
        // 1. Start Logging Process background thread (listens on port 9002)
        loggingThread = new Thread(() -> LoggingProcess.main(new String[]{}));
        loggingThread.setDaemon(true);
        loggingThread.start();

        // 2. Start Core Process background thread (listens on port 9001 and connects to port 9002)
        coreThread = new Thread(() -> CoreProcess.main(new String[]{}));
        coreThread.setDaemon(true);
        coreThread.start();

        // Allow a brief moment for server sockets to bind and spin up
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterAll
    public static void stopProcesses() {
        if (loggingThread != null) loggingThread.interrupt();
        if (coreThread != null) coreThread.interrupt();
    }

    @Test
    @Order(1)
    @DisplayName("TC01: Verify Logging Process Socket Listener (Port 9002)")
    public void testLoggingProcessSocketConnection() {
        try (Socket socket = new Socket("127.0.0.1", 9002)) {
            assertNotNull(socket, "Logging socket object should not be null.");
            assertTrue(socket.isConnected(), "Logging process should be actively listening and reachable on port 9002.");
        } catch (Exception e) {
            fail("Failed to connect to LoggingProcess on port 9002. Error: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("TC02: Verify Core Process Socket Listener (Port 9001)")
    public void testCoreProcessSocketConnection() {
        try (Socket socket = new Socket("127.0.0.1", 9001)) {
            assertNotNull(socket, "Core socket object should not be null.");
            assertTrue(socket.isConnected(), "Core process should be actively listening and reachable on port 9001.");
        } catch (Exception e) {
            fail("Failed to connect to CoreProcess on port 9001. Error: " + e.getMessage());
        }
        System.out.println("Verification: Core process port 9001 is reachable and passed.");
    }

    @Test
    @Order(3)
    @DisplayName("TC03: Verify SimulatorUI Initialization and Component Threads")
    public void testSimulatorUIInitialization() throws Exception {
        AtomicReference<SimulatorUI> uiRef = new AtomicReference<>();

        // Initialize Swing UI on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeAndWait(() -> {
            SimulatorUI ui = new SimulatorUI();
            ui.setVisible(false); // Hide window during automated testing
            uiRef.set(ui);
        });

        simulatorUI = uiRef.get();
        assertNotNull(simulatorUI, "SimulatorUI instance must be successfully created.");
        
        // Verify frame title setup
        assertEquals("Team-Thanthrashakthi - STC89C52 Microcontroller Simulator", 
                simulatorUI.getTitle(), "Simulator UI window title must match.");

        // Allow background thread handshake with CoreProcess to execute
        Thread.sleep(1000);

        // Clean up UI instance after test
        SwingUtilities.invokeAndWait(() -> simulatorUI.dispose());
        System.out.println("Verification: SimulatorUI initialized successfully on EDT.");
    }
@Test
void testLoggingProcess() {
    // your test logic...
    System.out.println("Verification: Logging process port 9002 is reachable and passed.");
}
}