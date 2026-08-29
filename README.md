# Team-ThanthrashakthiProject title: Educational 8-bit Microcontroller Simulator with Process scheduling

## Problem Objective: To create a simple simulator for the STC89C52 microcontroller that shows how  instructions are executed, how memory and peripherals works  , and how multiple programs are managed by the CPU using different scheduling methods.

## Problem Statement: Design and implement a Java-based simulator for the STC89C52 8-bit microcontroller that models instruction execution, memory ,stack, GPIO , timer,and interrupts .The simulator will also manage multiple processes using PCB,queues,context switching,and FCFS, round robin,and Priority scheduling algorithms.

## Project scope: The project covers CPU simulation, registers, memory ,stack, peripherals , process management , PCB, ready queue , scheduling , and context switching.

## Microcontroller being simulated:SCT89C52

## Team Members: Venkateshwara U D - 25190152
              Zainaba Fidha K N - 25190155
              Yathiksha U S - 25190153
              Raaif Abdul Haamid - 25190140

## Team responsibilities:
Venkateshwara U D : CPU and instruction execution
Zainaba Fidha K N : Memory and stack
Yathiksha U S : Data structures(DSA)
Raaif Abdul Haamid : User intertface and context switching

## Selected programming language: JAVA

## Initial system architecture:
mermaid
flowchart TD

    A["EDUCATIONAL MICROCONTROLLER SIMULATOR"]

    A --> PM["PROCESS MANAGEMENT"]
    A --> CPU["CPU"]
    A --> MEM["MEMORY"]
    A --> PER["PERIPHERALS"]

    PM --> PCB["PCB"]
    PCB --> RQ["Ready Queue"]
    RQ --> SCH["Scheduler"]
    SCH -->|Select Process| CPU

    CPU --> PC["Program Counter"]
    PC --> ID["Instruction Decoder"]
    ID --> ALU["ALU"]
    ALU --> REG["Registers"]

    MEM --> PMEM["Program Memory"]
    MEM --> DMEM["Data Memory"]
    MEM --> ST["Stack"]

    PER --> GPIO["GPIO"]
    PER --> TIMER["Timer"]
    PER --> INT["Interrupts"]

    CPU --> MEM
    CPU --> PER

## Initial development plan:

