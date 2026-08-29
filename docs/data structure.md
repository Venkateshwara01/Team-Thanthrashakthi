STC89C52-Microcontroller
Microcontroller  contains CPU , Memory, Input/Output and peripherals .

Instruction categories:
Instruction: It gives command to the CPU.
 Example: Add two numbers

Instruction set:
Type  	Purpose  	  Example

Data transfer  	Move data from one location to  another                                
                                                                           
	MOV
Arithmetic	   Perform calculations   	ADD,DIV,SUBB
Logical               	       Bitwise operations	XRL,CLR
Branch control	Where instructions execute it will change the normal order work with individual bits	CALL,RAT
Boolean         	Work with individual bits	CLR,CPL
Machine control	This understand the process of  work then memorizing the name	NOP
 
GPIO (General purpose Input/Output):
GPIO: It is the way that microcontroller communicates with external devices.
Input: Outside device → Microcontroller.
Example:   Button
                           ↓
                      GPIO
                           ↓
            Microcontroller
Output: Microcontroller → Outside device
Example:      Microcontroller
                                     ↓
                               GPIO
                                    ↓
                                LED
Pin and Port:
Pin=Individual connections
Example:   P1.0
Port= Group of pins
Example:
PORT 1
P1.0
P1.1
P1.2
P1.3
 
P1.4
P1.5
P1.6
P1.7
High and Low:
High=1
Low=0
=> P1.0=1
It has high logical state
=>P1.0=0
Then,It has low logical state
Input and Output:
Input: The microcontroller reads the pin
Example: Button → P1.0
▪ It checks whether it is high or low
Output: The microcontroller controls the pins
Example: P1.0  → LED
▪ The changes in P1.0’s value
Port state:
Values of all pins are together 
P1.7 P1.6 P1.5 P1.4 P1.3 P1.2 P1.1 P1.0
    1        0        1        0        0        1        0       1
Port value:10100101
▪ It has 8-bit value which representing the eight pins.

Data Structure:
Data structure : This is a way to organizing and storing information in a program.
1.	Array:
Stores multiple values.
▪ The array it represents the memory.
Example: Memory
[0]      [1]     [2]     [3]     [4]
  ↓        ↓       ↓       ↓      ↓
10     25    40    15    60
2.Queue:
It is a waiting line.
▪ First in  → First out
Example: Enqueue
3.Circular queue:
This will understand why it can be useful when  managing a group of processes.
Example: removal
4.PCB (Process control block)
It will think of the information record for a process.
Example:  Process ID 
                     Process state
                    Program counter 
                    Register/context
                    Priority
Process Management:
Process : Understand it as a program being managed or executed.
Process state:
                                                  New
                                                     ↓
                                          Ready
                                                     ↓
                                             Running
                                               ↓
                                        Waiting
                                                    ↓
                                          Ready                                                                                                                                 
                                              
▪ New: The process is being created.
▪ Ready: The process has everything it needs to run except the CPU itself.
▪ Running : The CPU which is executing the instructions of this process.
▪ Waiting: The process which cannot continue running right now.
OR
 ▪ Terminated : The process has finished executing its core.
Ready Queue: In this process which are ready to run but waiting for CPU time so we can be kept in a ready queue.
Example: P1→P2→P3→P4
Scheduler : This is a core component of the operating system.
Context switching: Is a process an operating system uses to switch the CPU from one process to another.
                                          Process A running
                                                          ↓
                                          Save A’s context
                                                         ↓
                                          Load B’s context
                                                        ↓
                                         Process B running

Testing :
A simulator (like a CPU) which reads machine code and execute them.
In testing 3 level:
Level 1 : Test individual components.
Example:
 Instruction: Input → ADD
                          Expected → correct result
GPIO:  Set pin → High
               Expected → pin shows high
Queue : Add P1,P2,P3
                 Expected → correct order
Level 2 : Test modulus together
Example:      





                                                   Instruction
                                                             ↓
                                                         CPU
                                                            ↓
                                                     Register
                                                           ↓
                                                       GPIO
Level 3 : Test the complete simulator
Example:
                       Given the small program and check
                 ▪ Instruction
                 ▪ Memory        
                 ▪ Register
                 ▪ GPIO
                 ▪ Process
                 ▪ Output





                                                     



