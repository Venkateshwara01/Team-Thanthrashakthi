# WEEK-4 MEETING

**Date:30-09-2026**  
**Meeting Number: 1**

## Participants
- VENKATESHWARA U D
- RAAIF ABDUL HAAMID
- ZAINABA FIDHA K N
- YATHIKSHA U S

## Agenda
1. IPC Mechanism Selection & TCP Ports Setup
2. Multithreading & POSIX Concurrency Concepts
3. Shared Package Implementation
4. Work Distribution for Core, UI, and Logging Processes

## Discussion
In this meeting, we discussed the architectural planning for multi-process communication. We decided to use Java TCP Sockets and ObjectStreams for Inter-Process Communication (IPC) instead of native POSIX pipes. Port 9001 was selected for Core-UI IPC and Port 9002 was selected for Logging IPC.

We also discussed thread management for handling socket listeners concurrently without blocking process execution. The Team Lead introduced the initial implementation of the shared package to standardize data streams across all components.

## Decisions
1. Selected Java TCP Sockets using Port 9001 (Core-UI) and Port 9002 (Logging) for IPC.
2. Approved a multithreaded architecture for socket listening and concurrent process execution.
3. Adopted the shared package codebase written by the Team Lead for ObjectStream serialization.

## Action Items
- **VENKATESHWARA U D:** Integration, shared package implementation, TCP port selection, and threading architecture setup.
- **YATHIKSHA U S:** Implementing Core process.
- **ZAINABA FIDHA K N:** Implementing UI process.
- **RAAIF ABDUL HAAMID:** Implementing Logging process.

## Next Meeting
**Not decided probably after completing the first stage of work** 
