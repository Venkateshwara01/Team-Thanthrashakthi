# Week 4: Multi-Process Simulator & IPC Architecture

## 1. Project Overview & Objective
The objective of this milestone is to transition the monolithic single-process microcontroller simulator into a decoupled, robust multi-process architecture. By separating responsibilities across independent processes, the system models real-world operating system principles, distributed communication, and inter-process synchronization.

## 2. Team Responsibility & Component Ownership
Following the project division, system responsibilities are distributed among team members:
* **Student 1 (UI Process):** Developed `SimulatorUI` for user interaction, visual controls, and real-time simulator display.
* **Student 2 (Core Process):** Developed `CoreProcess` managing CPU execution, internal memory, stack, and queues for the STC89C52 architecture.
* **Student 3 (Logging Process):** Developed `LoggingProcess` as an asynchronous logging daemon for execution and error logging.
* **Team Leader (Integration):** Integrated all components, resolved IPC/interface boundaries, and executed system testing and benchmarking.

## 3. IPC Mechanism Investigation & Selection
* **Evaluated Mechanisms:** POSIX Shared Memory, Named Pipes (FIFOs), Message Queues, and TCP/IP Loopback Sockets (`127.0.0.1`).
* **Selected Mechanism:** TCP/IP Loopback Sockets (`127.0.0.1`).
* **Justification:** While shared memory or message queues are common on Unix-like environments, TCP loopback sockets provide cross-platform compatibility, built-in stream framing, asynchronous non-blocking connection handling, and explicit socket lifecycle management. This allows seamless inter-process communication across Windows, Linux, and macOS environments without native driver dependencies.
* **Ports Used:**
  * Port `9001`: Core Process IPC / UI Command channel.
  * Port `9002`: Logging Process data stream channel.

## 4. Shared Data Structures (`shared` Package)
To ensure type-safe and consistent communication across process boundaries, the `shared` package defines common data contracts and serialization models:
* **`CommandType` (Enum):** Defines instructions (`LOAD_PROGRAM`, `STEP`, `RUN`, `RESET`, `SHUTDOWN`).
* **`IPCMessage` (Serializable Payload):** Container encapsulating commands, raw program line lists, system state snapshots, and log text across socket streams.
* **`SystemStateSnapshot` (Serializable State):** Captures real-time CPU registers (`pc`, `acc`, `b`, `sp`, `dptr`), flags, execution queues, halt status, and RAM snippets.

## Process Architecture 
![simulator_three_process_block_diagram](/Images/simulator_three_process_block_diagram.png)

## Core Internal Arechitecture
![simulator_three_process_block_diagram](/Images/core_process_internal_block_diagram.png)

## 5. Standalone vs. Multi-Process Comparison

| Evaluation Metric | Standalone (Single-Process) | Multi-Process Architecture |
| :--- | :--- | :--- |
| **Fault Isolation** | A crash or unhandled exception in UI or logging halts the entire application. | Failures are isolated; core CPU execution runs independently of UI rendering or logging daemons. |
| **Concurrency & Threading** | Restricted by single JVM instance lock contention and shared memory space. | True asynchronous separation using background socket daemons and multi-threaded IPC. |
| **Overhead** | Minimal memory footprint; zero IPC latency. | Slight overhead due to socket serialization (`Serializable`) and loopback network handling. |
| **Scalability & Design** | Difficult to extend or refactor modularly ("Spaghetti code"). | Clean separation of concerns with dedicated shared data contracts (`shared.*`), allowing independent component updates. |

## 6. Performance Metrics & Benchmarks
* **Execution Time:** Startup overhead introduces an initial $\approx 50\text{ms}$ socket handshake delay, after which instruction execution matches standalone speeds.
* **IPC Overhead:** Sub-millisecond round-trip latency over local loopback sockets (`127.0.0.1`) using serialized `IPCMessage` objects.
* **CPU & Memory Usage:** Distributed across three separate lightweight processes, preventing memory bloat and UI freezes in the main window thread.

## 7. Managing Critical Sections
A critical section occurs when multiple threads try to access the same shared resource at the same time, leading to data corruption or crashes (race conditions). Our project handles this across two separate parts of the system:

1. **The Core Process Critical Section (CPU State & Memory):**
   * **The Shared Resource:** The Core's internal CPU registers (`pc`, `acc`, `sp`), `ProgramMemory`, and `DataMemory`.
   * **The Problem:** The Core process uses a ThreadPool (`ExecutorService`) with multiple worker threads running concurrently. If two worker threads try to read or update the Program Counter or Data RAM at the exact same millisecond, the CPU state will become corrupted.
   * **The Solution:** These execution blocks are protected using Java’s `synchronized` keyword. This acts as a lock on the door: while Worker Thread 1 updates the CPU registers, Worker Thread 2 waits outside until Thread 1 finishes.

2. **The Logging Process Critical Section (`simulation.log` File):**
   * **The Shared Resource:** The physical `simulation.log` text file on disk.
   * **The Problem:** If multiple parts of the system try to write lines of text into the exact same file simultaneously, the text lines will overlap and scramble.
   * **The Solution:** Managed using a Producer-Consumer pattern with an FCFS `BlockingQueue`:
     * *Write First:* The network thread receives incoming logs and safely drops them into the queue (`.put()`) in RAM memory. Because the queue is thread-safe, no race conditions happen here.
     * *Read & Write Second:* A single background file-writer worker thread reads the logs out of the queue sequentially (`.take()`) and writes them neatly into the text file on disk in strict chronological order.

## 8. Integration Test Results & Verification
The system is verified using an automated JUnit 5 test suite (`SystemIntegrationTest`) covering process lifecycles and port reachability:

| Test ID | Target Component | Mechanism / Interface | Test Description & Objective | Status & Verification Result |
| :--- | :--- | :--- | :--- | :--- |
| **Setup** | Backend Daemons | TCP Ports `9001` & `9002` | Automatically launches `LoggingProcess` and `CoreProcess` background threads prior to execution. | **`PASSED`** — Ports bound successfully without conflict. |
| **TC01** | `LoggingProcess` | TCP Socket (`127.0.0.1:9002`) | Verifies that the asynchronous logging daemon is active and accepting log streams. | **`PASSED`** — Connection established; port reachable. |
| **TC02** | `CoreProcess` | TCP Socket (`127.0.0.1:9001`) | Confirms the CPU core server is listening for UI commands and simulation instructions. | **`PASSED`** — Connection established; port reachable. |
| **TC03** | `SimulatorUI` | Swing EDT (`SwingUtilities.invokeAndWait`) | Safely initializes the graphical interface window and validates thread-safe UI component loading. | **`PASSED`** — Initialized on Event Dispatch Thread without exceptions. |

### Console Output Logs
```text
[LOGGING PROCESS] Starting Async Log Daemon...
[LOGGING PROCESS] Listening for log streams on port 9002
[CORE PROCESS] Initializing STC89C52 CPU Core...
[CORE PROCESS] Listening for UI commands on port 9001
[LOG] [CORE] Connected to Logging Process.

Verification: Core process port 9001 is reachable and passed.
Verification: SimulatorUI initialized successfully on EDT.
Verification: Logging process port 9002 is reachable and passed.
```
