Microcontroller-STC89C52
It stores two different things
The program->    microcontroller follows the instructions
Data-> values used while program is running 
                               STC89C52
                    	
              ──────┴──────
                   |	|
      Program memory                     Data memory
                      
	 
              instructions	 variables
                                                                        temporary data
                                                                           variables

program memory
mov A,   #10
Add A,   #20
Program counter:-
Address                                  instructions
0000H                                       Mov A,#10
00001H                                     Add A,#20
00002H 	Mov R0,A
DATA MEMORY:-
For example:A=10
                           B=20
                            C=A+B
Temporarily stored values called RAM
Main internal RAM is 128 bytes ,it is divided into four different areas 
 
Internal  RAM layout:-
128-byte RAM 
address
      
7FH  ┌───────────────────────────────┐
     |        General Purpose RAM    │
30H  ├───────────────────────────────┤
     │        Bit Addressable Area   │
20H  ├───────────────────────────────┤
00H     │        Register Banks         │

Register banks;-00H-1FH
4 register banks 
Each bank:-8 registers
R0,R1,R2,R3,R4,R5,R6,R7
4 banks
Bank 0->     00H to 07H
Bank 1->     08H to 0FH
Bank 2->     10H to 17H
Bank 3->     18H to 1FH
One bank active at a time 
Bank is selected using RS0 AND RS1 bits in PSW 
For example:
Psw selects bank 0
                
    R0  RAM address 00H
                 
       Psw selects bank 1
                       
                      
               R0  RAM address 08H


Bit addressable memory:-
(20H-2FH)
If we want to change one bit 
For example flag =1
It is a special  area  where individual bits is accessed 
It is used in :-
Flags,status values,on/off control,GPIO related operations
You can manipulate a single bit instead of working with 8 bits 

General purpose RAM :-
(30H-7FH)
.variables
.temporary values
.arrays
.the program data
 
RAM[30H]=10
RAM[31H]=20

SFR(special function registers):-
(80H to FFH)
Examples:
P0,P1,P2,P3->ports 
SP-stack pointer
PSW-program status word
TCON-time control
TMOD-time mode
IE-interept enable 
They control specific parts and functions of microcontroller 
SFR is kept separate from 128 byte RAM 
GPIO is the input/output functionality

Stack:-
Temporary storage area in RAM
	top 


add plate,it goes on top 
if you remove ,it removes from top 
saving return address during function calls ,saving data temporarily 

stack pointer:-
address of stack
first value is pushes
SP-> 08H
RAM[08H]
Before push:
SP = 07H
After push
SP=08H
RAM[08H]=value   (default value)

Push:
SP=07H
Push 50
The process :-
SP=07H +1 
SP=08H
RAM[08H]=50
As SP increases value gets stored 

Pop:-
SP=09H
RAM[09H]=70
When pop 
Value=RAM[09H]
Value=70
SP=08H
Difference between them is 
Push=increases SP first and then stores
Pop=reads first and then decreases SP
 
Execution of push and pop:-
SP=07H
Push 10 
SP=08H
RAM[08H]=10
Push 20
RAM[09H]=20
 Stack:
09H->20->SP
08H->10

Pop:-
Take 20 from 09H
SP=08H
08H->10     <-SP
LIFO =last in first out 
CALL:-
Start 
            
Call function 

Continue here 

Call instruction saves the return address in stack ,then the processor jumps to function 

RET:-
Going back to the call 
Takes the return address from stack and moves it to the program counter 
Call 

Save return address on stack

Go to function

Function executes
 
             RET

         Takes return address from stack
            
             Continue original program
Program counter is 16 bit ,return address needs 2 bytes

Stack overflow :-
Shares internal RAM space withb other data 
Stack grows upward ,variables also use RAM
Higher address

Stack grows here
        
     Variables here 

If stack becomes too large ,it overwrites variables
This happens when:
.Too many push operations
.Many nested cell
.Not enough pops

For simulator ,represent memory as array
RAM
│
├── 00H–1FH ->Register Banks
├── 20H–2FH ->Bit-addressable Area
└── 30H–7FH -> General RAM
Stack pointer 
SP=07H
PUSH:
SP++
RAM[SP]=value
POP:
Sp- - 
Value=RAM[SP]

Call saves a 16 bit return address,
RET reconstructs that address and return executes to it 




                 
                         STC89C52
                             │
              ┌──────────────┴──────────────┐
              │                                                            
      PROGRAM MEMORY                                              DATA MEMORY
                                                                                                 
   Stores program instructions                         Stores program data
                                           
       Program Counter (PC)                                              INTERNAL RAM
                                                                                              │             │             │
                                                      Register Banks   Bit-Addressable   General RAM
                                                                  00H–1FH        20H–2FH           RAM
                  	   
                                                                                         
                                                   	Stack 


	Stack pointer 



                                                                                                      Push                                 pop
	Sp increases 	sp decreases 
As SP increase ,it stores values
In pop ,it read value and sp decreases
